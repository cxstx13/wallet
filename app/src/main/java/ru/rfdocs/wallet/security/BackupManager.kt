package ru.rfdocs.wallet.security

import android.content.Context
import android.net.Uri
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ru.rfdocs.wallet.data.repo.DocRepository
import ru.rfdocs.wallet.data.repo.PhotoStore
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.Base64
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

data class BackupResult(val cards: Int, val bytes: Int)

@Serializable
data class BackupPayload(
    val cardsJson: String,
    val photos: Map<String, String> = emptyMap()
)

object BackupManager {

    private const val HEADER = "RFWALLET3"
    private const val SALT_SIZE = 16
    private const val TAG_BITS = 128
    private const val ITERATIONS = 120_000

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    fun exportTo(context: Context, uri: Uri, password: String): Result<BackupResult> {
        return try {
            require(password.length >= 6) { "Пароль короче 6 символов" }

            val cardsJson = String(DocRepository.exportCardsJson(), Charsets.UTF_8).trim()
            require(cardsJson.length > 2) { "Нет документов для экспорта" }

            val photos = PhotoStore.exportAll(context).mapValues { entry ->
                Base64.getEncoder().encodeToString(entry.value)
            }
            val payload = json.encodeToString(BackupPayload.serializer(), BackupPayload(cardsJson, photos))
            val plain = payload.toByteArray(Charsets.UTF_8)

            val salt = ByteArray(SALT_SIZE).also { java.security.SecureRandom().nextBytes(it) }
            val key = deriveKey(password, salt)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"))
            val iv = cipher.iv
            val encrypted = cipher.doFinal(plain)

            val buffer = ByteArrayOutputStream()
            buffer.write(HEADER.toByteArray(Charsets.US_ASCII))
            buffer.write(salt)
            buffer.write(iv.size)
            buffer.write(iv)
            buffer.write(encrypted)
            buffer.flush()
            val data = buffer.toByteArray()
            buffer.close()

            require(data.isNotEmpty()) { "Пустой файл" }
            writeAll(context, uri, data)

            Result.success(BackupResult(cards = DocRepository.cards.value.size, bytes = data.size))
        } catch (error: Throwable) {
            Result.failure(IllegalStateException(error.message ?: "Ошибка экспорта", error))
        }
    }

    fun importFrom(context: Context, uri: Uri, password: String): Result<Int> {
        return try {
            val data = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return Result.failure(IllegalStateException("Файл не читается"))
            require(data.isNotEmpty()) { "Файл пустой" }

            val header = HEADER.toByteArray(Charsets.US_ASCII)
            require(data.size > header.size + SALT_SIZE + 64) { "Файл повреждён" }
            require(data.copyOfRange(0, header.size).contentEquals(header)) {
                "Это не файл резервной копии"
            }

            var offset = header.size
            val salt = data.copyOfRange(offset, offset + SALT_SIZE)
            offset += SALT_SIZE
            val ivSize = data[offset].toInt() and 0xFF
            offset += 1
            val iv = data.copyOfRange(offset, offset + ivSize)
            offset += ivSize
            val encrypted = data.copyOfRange(offset, data.size)

            val key = deriveKey(password, salt)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(TAG_BITS, iv))

            val plain = try {
                cipher.doFinal(encrypted)
            } catch (tagError: AEADBadTagException) {
                return Result.failure(IllegalStateException("Неверный пароль или файл повреждён"))
            }

            val payload = json.decodeFromString(
                BackupPayload.serializer(),
                String(plain, Charsets.UTF_8)
            )

            val restored = DocRepository.importCardsJson(payload.cardsJson.toByteArray(Charsets.UTF_8))
            if (restored <= 0) {
                return Result.failure(IllegalStateException("В бэкапе нет документов"))
            }

            val photos = payload.photos.mapValues { entry ->
                Base64.getDecoder().decode(entry.value)
            }
            PhotoStore.importAll(context, photos)
            DocRepository.refresh()

            Result.success(restored)
        } catch (error: Throwable) {
            Result.failure(IllegalStateException(error.message ?: "Ошибка импорта", error))
        }
    }

    fun suggestedFileName(): String {
        val stamp = java.text.SimpleDateFormat("yyyyMMdd-HHmm", java.util.Locale.US)
            .format(java.util.Date())
        return "wallet-backup-$stamp.rfwallet"
    }

    private fun deriveKey(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, 256)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun writeAll(context: Context, uri: Uri, data: ByteArray) {
        context.contentResolver.openFileDescriptor(uri, "wt")?.use { descriptor ->
            FileOutputStream(descriptor.fileDescriptor).use { output ->
                output.write(data)
                output.flush()
                runCatching { output.fd.sync() }
            }
        }

        val written = context.contentResolver.openInputStream(uri)?.use { it.readBytes().size } ?: -1
        if (written != data.size) {
            throw IOException("Записано $written из ${data.size} байт")
        }
    }
}
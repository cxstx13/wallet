package ru.rfdocs.wallet.security

import android.content.Context
import android.net.Uri
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import ru.rfdocs.wallet.data.model.DocCard
import ru.rfdocs.wallet.data.repo.DocRepository
import ru.rfdocs.wallet.data.repo.PhotoStore
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@Serializable
data class BackupPayload(
    val cardsJson: String,
    val photos: Map<String, String>
)

object BackupManager {

    private const val HEADER = "RFWALLET2"
    private const val SALT_SIZE = 16
    private const val MIN_IV_SIZE = 12
    private const val MAX_IV_SIZE = 16
    private const val TAG_BITS = 128
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    private val cardsSerializer = ListSerializer(DocCard.serializer())

    fun exportTo(context: Context, uri: Uri, password: String): Result<Int> {
        return try {
            require(password.length >= 6) { "Пароль должен быть не короче 6 символов" }

            val cardsJson = String(DocRepository.exportCardsJson(), Charsets.UTF_8).trim()
            require(cardsJson.length > 2) { "Нет документов для экспорта" }

            val cardCount = runCatching {
                json.decodeFromString(cardsSerializer, cardsJson).size
            }.getOrDefault(0)
            require(cardCount > 0) { "Нет документов для экспорта" }

            val photos = PhotoStore.exportAll(context).mapValues { entry ->
                PinVault.base64Encode(entry.value)
            }
            val payload = BackupPayload(cardsJson = cardsJson, photos = photos)
            val plain = json.encodeToString(BackupPayload.serializer(), payload)
                .toByteArray(Charsets.UTF_8)

            val salt = PinVault.randomBytes(SALT_SIZE)
            val keyBytes = PinVault.deriveKey(password, salt)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(keyBytes, "AES"))
            val iv = cipher.iv
            val encrypted = cipher.doFinal(plain)

            val buffer = ByteArrayOutputStream()
            buffer.write(HEADER.toByteArray(Charsets.US_ASCII))
            buffer.write(salt)
            buffer.write(iv.size)
            buffer.write(iv)
            buffer.write(encrypted)
            buffer.flush()

            val encryptedBytes = buffer.toByteArray()
            buffer.close()

            require(encryptedBytes.isNotEmpty()) { "Нет данных для экспорта" }
            require(encryptedBytes.size > HEADER.length + SALT_SIZE + MIN_IV_SIZE) {
                "Внутренняя ошибка формирования файла"
            }

            writeBytes(context, uri, encryptedBytes)

            Result.success(cardCount)
        } catch (error: Throwable) {
            Result.failure(IllegalStateException(error.message ?: "Ошибка экспорта", error))
        }
    }

    fun importFrom(context: Context, uri: Uri, password: String): Result<Int> {
        return try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
                input.readBytes()
            } ?: return Result.failure(IllegalStateException("Не удалось прочитать файл"))

            require(bytes.isNotEmpty()) { "Файл пустой (0 байт)" }

            val header = HEADER.toByteArray(Charsets.US_ASCII)
            require(bytes.size > header.size + SALT_SIZE + 1 + MIN_IV_SIZE) {
                "Файл повреждён или слишком короткий"
            }
            require(bytes.copyOfRange(0, header.size).contentEquals(header)) {
                "Это не файл резервной копии приложения"
            }

            var offset = header.size
            val salt = bytes.copyOfRange(offset, offset + SALT_SIZE)
            offset += SALT_SIZE
            val ivSize = bytes[offset].toInt() and 0xFF
            offset += 1
            require(ivSize in MIN_IV_SIZE..MAX_IV_SIZE) { "Файл повреждён" }
            val iv = bytes.copyOfRange(offset, offset + ivSize)
            offset += ivSize
            val encrypted = bytes.copyOfRange(offset, bytes.size)

            val keyBytes = PinVault.deriveKey(password, salt)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                SecretKeySpec(keyBytes, "AES"),
                GCMParameterSpec(TAG_BITS, iv)
            )

            val plain = try {
                cipher.doFinal(encrypted)
            } catch (tagError: AEADBadTagException) {
                return Result.failure(
                    IllegalStateException("Неверный пароль или файл повреждён", tagError)
                )
            }

            val payload = json.decodeFromString(
                BackupPayload.serializer(),
                String(plain, Charsets.UTF_8)
            )

            val restored = DocRepository.importCardsJson(
                payload.cardsJson.toByteArray(Charsets.UTF_8)
            )
            if (restored <= 0) {
                return Result.failure(
                    IllegalStateException("В бэкапе нет документов или он повреждён")
                )
            }

            val photos: Map<String, ByteArray> = payload.photos.mapValues { entry ->
                PinVault.base64Decode(entry.value)
            }
            PhotoStore.importAll(context, photos)

            DocRepository.refresh()

            Result.success(restored)
        } catch (error: Throwable) {
            Result.failure(IllegalStateException(error.message ?: "Ошибка импорта", error))
        }
    }

    private fun writeBytes(context: Context, uri: Uri, bytes: ByteArray) {
        val rawOut = context.contentResolver.openOutputStream(uri, "wt")
            ?: context.contentResolver.openOutputStream(uri)
            ?: throw IOException("Не удалось открыть файл для записи")

        rawOut.use { raw ->
            BufferedOutputStream(raw).use { out ->
                out.write(bytes)
                out.flush()
                runCatching { (raw as? FileOutputStream)?.fd?.sync() }
            }
        }

        val written = context.contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes().size
        } ?: -1

        if (written != bytes.size) {
            throw IOException("Файл записался не полностью ($written из ${bytes.size} байт)")
        }
    }

    fun suggestedFileName(): String {
        val stamp = java.text.SimpleDateFormat("yyyyMMdd-HHmm", java.util.Locale.US)
            .format(java.util.Date())
        return "wallet-backup-$stamp.rfwallet"
    }
}
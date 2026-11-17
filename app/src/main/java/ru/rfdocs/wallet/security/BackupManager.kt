package ru.rfdocs.wallet.security

import android.content.Context
import android.net.Uri
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import ru.rfdocs.wallet.data.model.DocCard
import ru.rfdocs.wallet.data.repo.DocRepository
import ru.rfdocs.wallet.data.repo.PhotoStore
import java.io.ByteArrayOutputStream
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@Serializable
data class BackupPayload(
    val cardsJson: String,
    val photos: Map<String, String>
)

object BackupManager {

    private const val HEADER = "RFWALLET1"
    private const val SALT_SIZE = 16
    private const val TAG_BITS = 128
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    fun exportTo(context: Context, uri: Uri, password: String): Result<Int> = runCatching {
        require(password.length >= 6) { "Пароль должен быть не короче 6 символов" }

        val cardsJson = String(DocRepository.exportCardsJson(), Charsets.UTF_8)
        val photos = PhotoStore.exportAll(context).mapValues { entry ->
            PinVault.base64Encode(entry.value)
        }
        val payload = BackupPayload(cardsJson = cardsJson, photos = photos)
        val plain = json.encodeToString(BackupPayload.serializer(), payload).toByteArray(Charsets.UTF_8)

        val salt = PinVault.randomBytes(SALT_SIZE)
        val keyBytes = PinVault.deriveKey(password, salt)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(keyBytes, "AES"))
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plain)

        val out = ByteArrayOutputStream()
        out.write(HEADER.toByteArray(Charsets.US_ASCII))
        out.write(salt)
        out.write(iv.size)
        out.write(iv)
        out.write(encrypted)

        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(out.toByteArray())
            stream.flush()
        } ?: error("Не удалось открыть файл для записи")

        json.decodeFromString(ListSerializer(DocCard.serializer()), cardsJson).size
    }

    fun importFrom(context: Context, uri: Uri, password: String): Result<Int> = runCatching {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("Не удалось прочитать файл")

        val header = HEADER.toByteArray(Charsets.US_ASCII)
        require(bytes.size > header.size + SALT_SIZE + 1 + 12) { "Файл повреждён" }
        require(bytes.copyOfRange(0, header.size).contentEquals(header)) {
            "Это не файл резервной копии приложения"
        }

        var offset = header.size
        val salt = bytes.copyOfRange(offset, offset + SALT_SIZE)
        offset += SALT_SIZE
        val ivSize = bytes[offset].toInt() and 0xFF
        offset += 1
        require(ivSize in 12..16) { "Файл повреждён" }
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
        val plain = cipher.doFinal(encrypted)
        val payload = json.decodeFromString(
            BackupPayload.serializer(),
            String(plain, Charsets.UTF_8)
        )

        val count = DocRepository.importCardsJson(payload.cardsJson.toByteArray(Charsets.UTF_8))
        require(count >= 0) { "Неверный пароль или повреждённый файл" }

        val photos: Map<String, ByteArray> = payload.photos.mapValues { entry ->
            PinVault.base64Decode(entry.value)
        }
        PhotoStore.importAll(context, photos)
        count
    }

    fun suggestedFileName(): String {
        val stamp = java.text.SimpleDateFormat("yyyyMMdd-HHmm", java.util.Locale.US)
            .format(java.util.Date())
        return "wallet-backup-$stamp.rfwallet"
    }
}

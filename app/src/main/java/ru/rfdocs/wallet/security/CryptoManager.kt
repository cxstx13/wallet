package ru.rfdocs.wallet.security

import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties

object CryptoManager {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "wallet_master_aes_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_SIZE = 12
    private const val TAG_BITS = 128

    @Synchronized
    private fun secretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val existing = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        if (existing != null) return existing.secretKey

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setRandomizedEncryptionRequired(true)
            .build()
        generator.init(spec)
        return generator.generateKey()
    }

    fun encrypt(plain: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey())
        val iv = cipher.iv
        val cipherBytes = cipher.doFinal(plain)
        val out = ByteArray(iv.size + cipherBytes.size)
        System.arraycopy(iv, 0, out, 0, iv.size)
        System.arraycopy(cipherBytes, 0, out, iv.size, cipherBytes.size)
        return out
    }

    fun decrypt(payload: ByteArray): ByteArray {
        require(payload.size > IV_SIZE) { "Некорректный зашифрованный блок" }
        val iv = payload.copyOfRange(0, IV_SIZE)
        val cipherBytes = payload.copyOfRange(IV_SIZE, payload.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey(), GCMParameterSpec(TAG_BITS, iv))
        return cipher.doFinal(cipherBytes)
    }

    fun encryptText(plain: String): String =
        Base64.encodeToString(encrypt(plain.toByteArray(Charsets.UTF_8)), Base64.NO_WRAP)

    fun decryptText(payload: String): String =
        String(decrypt(Base64.decode(payload, Base64.NO_WRAP)), Charsets.UTF_8)

    fun encryptBytesToBase64(plain: ByteArray): String =
        Base64.encodeToString(encrypt(plain), Base64.NO_WRAP)

    fun decryptBase64ToBytes(payload: String): ByteArray =
        decrypt(Base64.decode(payload, Base64.NO_WRAP))

    fun wipeKey() {
        runCatching {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (keyStore.containsAlias(KEY_ALIAS)) keyStore.deleteEntry(KEY_ALIAS)
        }
    }
}

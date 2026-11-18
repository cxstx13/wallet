package ru.rfdocs.wallet.security

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PinVault {

    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_BYTES = 16

    fun isPinSet(context: Context): Boolean {
        val hash = SecurePrefs.getPinHash(context)
        val salt = SecurePrefs.getPinSalt(context)
        return !hash.isNullOrEmpty() && !salt.isNullOrEmpty()
    }

    fun setPin(context: Context, pin: String) {
        val salt = randomBytes(SALT_BYTES)
        SecurePrefs.setPinSalt(context, base64Encode(salt))
        SecurePrefs.setPinHash(context, hashPin(pin, salt))
        SecurePrefs.setFailedAttempts(context, 0)
    }

    fun verify(context: Context, pin: String): Boolean {
        val storedHash = SecurePrefs.getPinHash(context)
        val storedSalt = SecurePrefs.getPinSalt(context)
        if (storedHash.isNullOrEmpty() || storedSalt.isNullOrEmpty()) return false

        val salt = base64Decode(storedSalt)
        val candidate = hashPin(pin, salt)
        return MessageDigest.isEqual(
            storedHash.toByteArray(Charsets.UTF_8),
            candidate.toByteArray(Charsets.UTF_8)
        )
    }

    fun clear(context: Context) {
        SecurePrefs.setPinHash(context, "")
        SecurePrefs.setPinSalt(context, "")
        SecurePrefs.setFailedAttempts(context, 0)
    }

    private fun hashPin(pin: String, salt: ByteArray): String {
        val key = deriveKey(pin, salt, ITERATIONS)
        return base64Encode(key)
    }

    fun deriveKey(
        password: String,
        salt: ByteArray,
        iterations: Int = ITERATIONS
    ): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .encoded
        } finally {
            spec.clearPassword()
        }
    }

    fun randomBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        SecureRandom().nextBytes(bytes)
        return bytes
    }

    fun base64Encode(bytes: ByteArray): String =
        Base64.encodeToString(bytes, Base64.NO_WRAP)

    fun base64Decode(value: String): ByteArray =
        Base64.decode(value, Base64.NO_WRAP)
}
package ru.rfdocs.wallet.security

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PinVault {

    const val DEFAULT_ITERATIONS = 120_000
    const val KEY_BITS = 256

    fun isPinSet(context: Context): Boolean {
        val hash = SecurePrefs.getPinHash(context)
        val salt = SecurePrefs.getPinSalt(context)
        return !hash.isNullOrEmpty() && !salt.isNullOrEmpty()
    }

    fun setPin(context: Context, pin: String) {
        val salt = randomBytes(16)
        val hash = deriveBase64(pin, salt, DEFAULT_ITERATIONS)
        SecurePrefs.setPinSalt(context, base64Encode(salt))
        SecurePrefs.setPinHash(context, hash)
        SecurePrefs.setFailedAttempts(context, 0)
    }

    fun verify(context: Context, pin: String): Boolean {
        val storedHash = SecurePrefs.getPinHash(context)
        val storedSalt = SecurePrefs.getPinSalt(context)
        if (storedHash.isNullOrEmpty() || storedSalt.isNullOrEmpty()) return false
        val salt = base64Decode(storedSalt)
        val candidate = deriveBase64(pin, salt, DEFAULT_ITERATIONS)
        return constantTimeEquals(storedHash, candidate)
    }

    fun clear(context: Context) {
        SecurePrefs.setPinHash(context, "")
        SecurePrefs.setPinSalt(context, "")
        SecurePrefs.setFailedAttempts(context, 0)
    }

    fun deriveKey(password: String, salt: ByteArray, iterations: Int = DEFAULT_ITERATIONS): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun deriveBase64(password: String, salt: ByteArray, iterations: Int): String =
        base64Encode(deriveKey(password, salt, iterations))

    fun randomBytes(size: Int): ByteArray {
        val out = ByteArray(size)
        SecureRandom().nextBytes(out)
        return out
    }

    fun base64Encode(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)

    fun base64Decode(value: String): ByteArray = Base64.decode(value, Base64.NO_WRAP)

    private fun constantTimeEquals(a: String, b: String): Boolean {
        val left = a.toByteArray(Charsets.UTF_8)
        val right = b.toByteArray(Charsets.UTF_8)
        return MessageDigest.isEqual(left, right)
    }
}

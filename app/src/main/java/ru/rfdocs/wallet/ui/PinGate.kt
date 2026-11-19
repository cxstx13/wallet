package ru.rfdocs.wallet.ui

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

object PinGate {

    private const val FILE = "wallet_pin_gate"
    private const val KEY_HASH = "pin_hash"
    private const val KEY_SALT = "pin_salt"
    private const val ROUNDS = 1000

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun isPinSet(context: Context): Boolean {
        val p = prefs(context)
        val hash = p.getString(KEY_HASH, null)
        val salt = p.getString(KEY_SALT, null)
        return !hash.isNullOrBlank() && !salt.isNullOrBlank()
    }

    fun setPin(context: Context, pin: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        prefs(context).edit()
            .putString(KEY_SALT, encode(salt))
            .putString(KEY_HASH, encode(hash(pin, salt)))
            .apply()
    }

    fun verify(context: Context, pin: String): Boolean {
        val p = prefs(context)
        val stored = p.getString(KEY_HASH, null) ?: return false
        val salt = p.getString(KEY_SALT, null) ?: return false
        return runCatching {
            encode(hash(pin, decode(salt))) == stored
        }.getOrDefault(false)
    }

    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }

    private fun hash(pin: String, salt: ByteArray): ByteArray {
        var digest = MessageDigest.getInstance("SHA-256")
            .digest(salt + pin.toByteArray(Charsets.UTF_8))
        repeat(ROUNDS) {
            digest = MessageDigest.getInstance("SHA-256").digest(digest)
        }
        return digest
    }

    private fun encode(bytes: ByteArray): String =
        Base64.encodeToString(bytes, Base64.NO_WRAP)

    private fun decode(value: String): ByteArray =
        Base64.decode(value, Base64.NO_WRAP)
}
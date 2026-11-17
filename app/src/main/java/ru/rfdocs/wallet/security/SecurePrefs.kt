package ru.rfdocs.wallet.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecurePrefs {

    private const val FILE_NAME = "wallet_secure_prefs"
    private const val KEY_PIN_HASH = "pin_hash"
    private const val KEY_PIN_SALT = "pin_salt"
    private const val KEY_BIOMETRIC = "biometric_enabled"
    private const val KEY_DARK_THEME = "dark_theme"
    private const val KEY_FAILED = "failed_attempts"
    private const val KEY_ONBOARDED = "onboarded"

    @Volatile
    private var cached: SharedPreferences? = null

    fun prefs(context: Context): SharedPreferences {
        cached?.let { return it }
        synchronized(this) {
            cached?.let { return it }
            val masterKey = MasterKey.Builder(context.applicationContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .setUserAuthenticationRequired(false)
                .build()
            val created = EncryptedSharedPreferences.create(
                context.applicationContext,
                FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
            cached = created
            return created
        }
    }

    fun getPinHash(context: Context): String? = prefs(context).getString(KEY_PIN_HASH, null)

    fun setPinHash(context: Context, hash: String) {
        prefs(context).edit().putString(KEY_PIN_HASH, hash).apply()
    }

    fun getPinSalt(context: Context): String? = prefs(context).getString(KEY_PIN_SALT, null)

    fun setPinSalt(context: Context, salt: String) {
        prefs(context).edit().putString(KEY_PIN_SALT, salt).apply()
    }

    fun isBiometricEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_BIOMETRIC, false)

    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_BIOMETRIC, enabled).apply()
    }

    fun darkTheme(context: Context): Boolean? {
        val p = prefs(context)
        return if (p.contains(KEY_DARK_THEME)) p.getBoolean(KEY_DARK_THEME, true) else null
    }

    fun setDarkTheme(context: Context, dark: Boolean) {
        prefs(context).edit().putBoolean(KEY_DARK_THEME, dark).apply()
    }

    fun failedAttempts(context: Context): Int = prefs(context).getInt(KEY_FAILED, 0)

    fun setFailedAttempts(context: Context, count: Int) {
        prefs(context).edit().putInt(KEY_FAILED, count).apply()
    }

    fun isOnboarded(context: Context): Boolean = prefs(context).getBoolean(KEY_ONBOARDED, false)

    fun setOnboarded(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_ONBOARDED, value).apply()
    }

    fun clearAll(context: Context) {
        prefs(context).edit().clear().apply()
    }
}

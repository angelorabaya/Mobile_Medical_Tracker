package com.example.medtrack.util

import android.content.Context
import android.util.Base64
import java.security.SecureRandom

/**
 * Generates and persists a stable 256-bit key used to encrypt the Room database
 * with SQLCipher. The key is stored in app-private SharedPreferences; combining
 * this with [android:allowBackup=false] keeps health data encrypted at rest.
 */
object DbKeyManager {

    private const val PREFS = "db_key"
    private const val KEY = "sqlcipher_passphrase"

    fun getOrCreatePassphrase(context: Context): ByteArray {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val existing = prefs.getString(KEY, null)
        if (existing != null) {
            return Base64.decode(existing, Base64.NO_WRAP)
        }
        val raw = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val encoded = Base64.encodeToString(raw, Base64.NO_WRAP)
        prefs.edit().putString(KEY, encoded).apply()
        return raw
    }
}

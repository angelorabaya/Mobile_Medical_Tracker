package com.example.medtrack.util

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Generates and persists a stable 256-bit key used to encrypt the Room database
 * with SQLCipher.
 *
 * The passphrase itself is never stored in plaintext. It is wrapped with a
 * non-exportable AES-256-GCM key generated in the Android Keystore, so it can
 * only be recovered on this device (and is inaccessible to apps without the
 * Keystore entry). Existing installs that previously stored the passphrase in
 * plaintext SharedPreferences are transparently re-wrapped on first access.
 *
 * Combined with [android:allowBackup=false], this keeps health data encrypted
 * at rest and the key protected by hardware-backed Keystore where available.
 */
object DbKeyManager {

    private const val PREFS = "db_key"
    private const val KEY_ENC_PASSPHRASE = "sqlcipher_passphrase_enc"
    private const val KEY_LEGACY_PASSPHRASE = "sqlcipher_passphrase" // legacy plaintext (migration only)

    private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    private const val KEYSTORE_ALIAS = "medtrack_sqlcipher_key"
    private const val GCM_TAG_LENGTH_BITS = 128
    private const val GCM_IV_LENGTH_BYTES = 12

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
    }

    /**
     * Returns the SQLCipher passphrase bytes, creating and wrapping a fresh key
     * on first run. The same bytes are returned on every call so the existing
     * database remains decryptable.
     */
    @Synchronized
    fun getOrCreatePassphrase(context: Context): ByteArray {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        // 1) New format already present -> decrypt and return.
        prefs.getString(KEY_ENC_PASSPHRASE, null)?.let { encrypted ->
            return decrypt(encrypted)
                ?: throw IllegalStateException("Failed to decrypt the database key. Keystore key may have been invalidated.")
        }

        // 2) Legacy plaintext from a previous version -> re-wrap in the Keystore,
        //    then remove the plaintext so it is never persisted unencrypted again.
        prefs.getString(KEY_LEGACY_PASSPHRASE, null)?.let { legacyBase64 ->
            val raw = Base64.decode(legacyBase64, Base64.NO_WRAP)
            val encrypted = encrypt(raw)
            prefs.edit()
                .putString(KEY_ENC_PASSPHRASE, encrypted)
                .remove(KEY_LEGACY_PASSPHRASE)
                .apply()
            return raw
        }

        // 3) First run -> generate a fresh key, wrap it, and store it.
        val raw = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val encrypted = encrypt(raw)
        prefs.edit().putString(KEY_ENC_PASSPHRASE, encrypted).apply()
        return raw
    }

    private fun getOrCreateKey(): SecretKey {
        keyStore.getKey(KEYSTORE_ALIAS, null)?.let { return it as SecretKey }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    /** Encrypts the passphrase and stores it as Base64(iv + ciphertext). */
    private fun encrypt(plain: ByteArray): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plain)
        return Base64.encodeToString(iv + ciphertext, Base64.NO_WRAP)
    }

    private fun decrypt(encoded: String): ByteArray? {
        return try {
            val bytes = Base64.decode(encoded, Base64.NO_WRAP)
            val iv = bytes.copyOfRange(0, GCM_IV_LENGTH_BYTES)
            val ciphertext = bytes.copyOfRange(GCM_IV_LENGTH_BYTES, bytes.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))
            cipher.doFinal(ciphertext)
        } catch (_: Exception) {
            null
        }
    }
}

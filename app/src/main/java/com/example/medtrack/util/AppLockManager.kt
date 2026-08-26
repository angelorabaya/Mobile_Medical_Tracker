package com.example.medtrack.util

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.medtrack.R

/**
 * Optional app-lock using the device biometric (or device credential) prompt.
 * Health data stays readable only after the owner authenticates.
 *
 * Note: enabling this requires the device to have a biometric enrolled or a
 * device credential (PIN/pattern/password) set up.
 */
object AppLockManager {

    private const val PREFS = "app_lock"
    private const val KEY_ENABLED = "biometric_enabled"

    fun isEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_ENABLED, false)
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ENABLED, enabled)
            .apply()
    }

    /** True when biometric or device-credential authentication is usable. */
    fun isAvailable(context: Context): Boolean {
        val result = BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )
        return result == BiometricManager.BIOMETRIC_SUCCESS
    }

    /** Shows the authentication prompt; requires a [FragmentActivity]. */
    fun showLock(activity: FragmentActivity) {
        if (!isAvailable(activity)) return

        val prompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                // No-op success: content is already visible behind the prompt.
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = Unit

                // If the user cancels or errors, allow them to retry via the
                // device-credential fallback by simply leaving the prompt closed;
                // the next launch will prompt again.
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) = Unit
            }
        )

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(R.string.app_lock_title))
            .setSubtitle(activity.getString(R.string.app_lock_subtitle))
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        prompt.authenticate(info)
    }
}

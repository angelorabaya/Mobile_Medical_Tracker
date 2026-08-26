package com.example.medtrack

import android.Manifest
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.example.medtrack.notification.NotificationHelper
import com.example.medtrack.theme.MedTrackTheme
import com.example.medtrack.util.AppLockManager
import com.example.medtrack.util.Permissions

class MainActivity : FragmentActivity() {

    private var wasBackgrounded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val destination = intent.getStringExtra(NotificationHelper.EXTRA_DESTINATION)
        val prescriptionId = if (intent.hasExtra(NotificationHelper.EXTRA_PRESCRIPTION_ID)) {
            intent.getIntExtra(NotificationHelper.EXTRA_PRESCRIPTION_ID, 0)
        } else null

        // Re-lock whenever the app returns to the foreground.
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                wasBackgrounded = true
            }

            override fun onStart(owner: LifecycleOwner) {
                if (wasBackgrounded &&
                    AppLockManager.isEnabled(this@MainActivity) &&
                    AppLockManager.isAvailable(this@MainActivity)
                ) {
                    AppLockManager.showLock(this@MainActivity)
                }
                wasBackgrounded = false
            }
        })

        enableEdgeToEdge()
        setContent {
            MedTrackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation(
                        deepLinkTarget = remember(destination) { resolveDeepLink(destination) },
                        deepLinkPrescriptionId = prescriptionId
                    )
                }
            }
            RequestNotificationPermissionOnce()
        }
    }

    private fun resolveDeepLink(destination: String?): DeepLinkTarget {
        return when (destination) {
            NotificationHelper.DEST_REMINDERS -> DeepLinkTarget.REMINDERS
            NotificationHelper.DEST_LAB_TESTS -> DeepLinkTarget.LAB_TESTS
            NotificationHelper.DEST_PRESCRIPTION -> DeepLinkTarget.PRESCRIPTION
            else -> DeepLinkTarget.NONE
        }
    }
}

/**
 * Requests the Android 13+ POST_NOTIFICATIONS permission on first launch so
 * medicine and lab-test reminders can be shown.
 */
@Composable
private fun RequestNotificationPermissionOnce() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Result handled by Android system UI; notifications work after grant. */ }
    LaunchedEffect(Unit) {
        if (!Permissions.hasNotificationPermission(context)) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

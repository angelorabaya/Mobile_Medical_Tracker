package com.example.medtrack

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.medtrack.data.AppContainer
import com.example.medtrack.data.MedTrackDatabase

class MedTrackApplication : Application() {
    val database: MedTrackDatabase by lazy {
        MedTrackDatabase.getDatabase(this)
    }

    /** Manual DI container exposing repositories to ViewModels. */
    val container: AppContainer by lazy {
        AppContainer(this)
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val medicineChannel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                getString(R.string.notification_channel_med_reminders),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_med_reminders_desc)
            }
            manager.createNotificationChannel(medicineChannel)

            val labOrderChannel = NotificationChannel(
                LAB_ORDER_CHANNEL_ID,
                getString(R.string.notification_channel_lab_orders),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_lab_orders_desc)
            }
            manager.createNotificationChannel(labOrderChannel)
        }
    }

    companion object {
        const val REMINDER_CHANNEL_ID = "medicine_reminders"
        const val LAB_ORDER_CHANNEL_ID = "lab_order_reminders"
    }
}

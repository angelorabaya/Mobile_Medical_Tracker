package com.example.medtrack.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getIntExtra("reminder_id", 0)
        val medicationName = intent.getStringExtra("medication_name") ?: "Medicine"
        val reminderTime = intent.getStringExtra("reminder_time") ?: ""

        NotificationHelper.showReminderNotification(
            context, reminderId, medicationName, reminderTime
        )
    }
}

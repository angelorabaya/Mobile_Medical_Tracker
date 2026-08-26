package com.example.medtrack.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Handles the action buttons on medicine-reminder notifications:
 *  - Snooze: reschedules the reminder for [SNOOZE_MS] from now.
 *  - Mark taken: dismisses the reminder notification.
 */
class ReminderActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getIntExtra(EXTRA_REMINDER_ID, 0)
        val medicationName = intent.getStringExtra(EXTRA_MEDICATION_NAME) ?: "Medicine"
        val reminderTime = intent.getStringExtra(EXTRA_REMINDER_TIME) ?: ""

        when (intent.action) {
            ACTION_SNOOZE -> snooze(context, reminderId, medicationName, reminderTime)
            ACTION_MARK_TAKEN -> NotificationHelper.cancelReminderNotification(context, reminderId)
        }
    }

    private fun snooze(context: Context, reminderId: Int, medicationName: String, reminderTime: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            reminderIntent(context, reminderId, medicationName, reminderTime, isSnooze = true),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAt = System.currentTimeMillis() + SNOOZE_MS

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, snoozePendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, snoozePendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, snoozePendingIntent)
        }

        NotificationHelper.showSnoozedNotification(context, reminderId, medicationName, reminderTime)
    }

    companion object {
        const val ACTION_SNOOZE = "com.example.medtrack.action.SNOOZE"
        const val ACTION_MARK_TAKEN = "com.example.medtrack.action.MARK_TAKEN"
        const val ACTION_REMINDER = "com.example.medtrack.action.REMINDER"
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_MEDICATION_NAME = "medication_name"
        const val EXTRA_REMINDER_TIME = "reminder_time"

        const val SNOOZE_MS = 10 * 60 * 1000L

        fun snoozeIntent(context: Context, reminderId: Int, medicationName: String, reminderTime: String): Intent {
            return Intent(context, ReminderActionReceiver::class.java).apply {
                action = ACTION_SNOOZE
                putExtra(EXTRA_REMINDER_ID, reminderId)
                putExtra(EXTRA_MEDICATION_NAME, medicationName)
                putExtra(EXTRA_REMINDER_TIME, reminderTime)
            }
        }

        fun markTakenIntent(context: Context, reminderId: Int): Intent {
            return Intent(context, ReminderActionReceiver::class.java).apply {
                action = ACTION_MARK_TAKEN
                putExtra(EXTRA_REMINDER_ID, reminderId)
            }
        }

        fun reminderIntent(
            context: Context,
            reminderId: Int,
            medicationName: String,
            reminderTime: String,
            isSnooze: Boolean = false
        ): Intent {
            return Intent(context, ReminderReceiver::class.java).apply {
                action = if (isSnooze) ACTION_SNOOZE else ACTION_REMINDER
                putExtra("reminder_id", reminderId)
                putExtra("medication_name", medicationName)
                putExtra("reminder_time", reminderTime)
                putExtra("is_snooze", isSnooze)
            }
        }
    }
}

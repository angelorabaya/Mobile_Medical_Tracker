package com.example.medtrack.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.medtrack.MainActivity
import com.example.medtrack.MedTrackApplication
import com.example.medtrack.R

object NotificationHelper {

    // Deep-link extras used by MainActivity to route notification taps.
    const val EXTRA_DESTINATION = "com.example.medtrack.EXTRA_DESTINATION"
    const val EXTRA_PRESCRIPTION_ID = "com.example.medtrack.EXTRA_PRESCRIPTION_ID"
    const val DEST_HOME = "home"
    const val DEST_REMINDERS = "reminders"
    const val DEST_LAB_TESTS = "lab_tests"
    const val DEST_PRESCRIPTION = "prescription"

    fun showReminderNotification(
        context: Context,
        reminderId: Int,
        medicationName: String,
        reminderTime: String
    ) {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_DESTINATION, DEST_REMINDERS)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, reminderId, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            ReminderActionReceiver.snoozeIntent(context, reminderId, medicationName, reminderTime),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId + 100000,
            ReminderActionReceiver.markTakenIntent(context, reminderId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, MedTrackApplication.REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_med_title))
            .setContentText("$medicationName - $reminderTime")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .addAction(0, context.getString(R.string.notification_snooze), snoozePendingIntent)
            .addAction(0, context.getString(R.string.notification_mark_taken), takenPendingIntent)
            .setAutoCancel(true)
            // Hide medication details on the lock screen.
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(reminderId, notification)
        } catch (e: SecurityException) {
            // Notification permission not granted.
        }
    }

    fun showSnoozedNotification(
        context: Context,
        reminderId: Int,
        medicationName: String,
        reminderTime: String
    ) {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_DESTINATION, DEST_REMINDERS)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, reminderId, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, MedTrackApplication.REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_snoozed))
            .setContentText("$medicationName - $reminderTime")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            // Hide medication details on the lock screen.
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(reminderId + 1000, notification)
        } catch (e: SecurityException) {
            // Notification permission not granted.
        }
    }

    fun cancelReminderNotification(context: Context, reminderId: Int) {
        try {
            NotificationManagerCompat.from(context).cancel(reminderId)
        } catch (_: Exception) {
            // Best effort.
        }
    }

    fun showLabOrderReminderNotification(
        context: Context,
        orderId: Int,
        testName: String,
        scheduledDate: String,
        scheduledTime: String,
        facilityName: String,
        fastingInstructions: String
    ) {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_DESTINATION, DEST_LAB_TESTS)
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 20000 + orderId, launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = buildString {
            append("Scheduled tomorrow ($scheduledDate at $scheduledTime)")
            if (facilityName.isNotBlank()) append(" • $facilityName")
            if (fastingInstructions.isNotBlank()) append(" • Prep: $fastingInstructions")
        }

        val notification = NotificationCompat.Builder(context, MedTrackApplication.LAB_ORDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("${context.getString(R.string.notification_lab_title)}: $testName")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            // Hide lab-test details on the lock screen.
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(20000 + orderId, notification)
        } catch (e: SecurityException) {
            // Notification permission not granted.
        }
    }
}

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

    fun showReminderNotification(
        context: Context,
        reminderId: Int,
        medicationName: String,
        reminderTime: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, reminderId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, MedTrackApplication.REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Time to take your medicine")
            .setContentText("$medicationName - $reminderTime")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(reminderId, notification)
        } catch (e: SecurityException) {
            // Permission not granted
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
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 20000 + orderId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = buildString {
            append("Scheduled tomorrow ($scheduledDate at $scheduledTime)")
            if (facilityName.isNotBlank()) append(" • $facilityName")
            if (fastingInstructions.isNotBlank()) append(" • Prep: $fastingInstructions")
        }

        val notification = NotificationCompat.Builder(context, MedTrackApplication.LAB_ORDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("📋 Upcoming Lab Test Tomorrow: $testName")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(20000 + orderId, notification)
        } catch (e: SecurityException) {
            // Permission not granted
        }
    }
}

package com.example.medtrack.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.medtrack.data.entity.MedicineReminder
import java.util.Calendar

object ReminderScheduler {

    fun scheduleReminder(context: Context, reminder: MedicineReminder, medicationName: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("reminder_id", reminder.id)
            putExtra("medication_name", medicationName)
            putExtra("reminder_time", reminder.reminderTime)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, reminder.id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeParts = reminder.reminderTime.split(":")
        val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: return
        val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: return

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Exact alarm permission not granted
        }
    }

    fun cancelReminder(context: Context, reminderId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, reminderId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun scheduleLabOrderReminder(context: Context, order: com.example.medtrack.data.entity.PendingLabOrder) {
        if (!order.isReminderEnabled || order.isCompleted) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, LabOrderReminderReceiver::class.java).apply {
            putExtra("order_id", order.id)
            putExtra("test_name", order.testName)
            putExtra("scheduled_date", order.scheduledDate)
            putExtra("scheduled_time", order.scheduledTime)
            putExtra("facility_name", order.facilityName)
            putExtra("fasting_instructions", order.fastingInstructions)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 20000 + order.id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Parse scheduled date: YYYY-MM-DD
        val dateParts = order.scheduledDate.split("-")
        val year = dateParts.getOrNull(0)?.toIntOrNull() ?: return
        val month = dateParts.getOrNull(1)?.toIntOrNull() ?: return
        val day = dateParts.getOrNull(2)?.toIntOrNull() ?: return

        // Parse scheduled time: HH:mm
        val timeParts = order.scheduledTime.split(":")
        val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 7
        val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 30

        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // 1 day before the scheduled test
            add(Calendar.DAY_OF_YEAR, -1)
        }

        val triggerTime = calendar.timeInMillis
        val now = System.currentTimeMillis()

        // Actual test date time
        val testCalendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (testCalendar.timeInMillis <= now) {
            // Already passed
            return
        }

        val finalTriggerTime = if (triggerTime <= now) {
            // If 1 day before is in the past (e.g. test is tomorrow or later today), alert in 5 seconds
            now + 5000L
        } else {
            triggerTime
        }

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    finalTriggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    finalTriggerTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            try {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    finalTriggerTime,
                    pendingIntent
                )
            } catch (_: Exception) {}
        }
    }

    fun cancelLabOrderReminder(context: Context, orderId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, LabOrderReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 20000 + orderId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}

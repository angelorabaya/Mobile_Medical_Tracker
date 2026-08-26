package com.example.medtrack.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.medtrack.data.MedTrackDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Re-schedules medicine and lab-test reminders after events that clear
 * [android.app.AlarmManager] alarms, such as device boot, app update, time
 * change, or timezone change.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in RESCHEDULE_ACTIONS) return

        val db = MedTrackDatabase.getDatabase(context)
        CoroutineScope(Dispatchers.IO).launch {
            val reminders = db.medicineReminderDao().getEnabledReminders().first()
            for (reminder in reminders) {
                val label = if (reminder.label.isNotBlank()) {
                    reminder.label
                } else {
                    val rx = db.prescriptionDao().getPrescriptionWithMedicationsByIdOnce(reminder.prescriptionId)
                    rx?.displayTitle ?: "Medicine"
                }
                ReminderScheduler.scheduleReminder(
                    context, reminder, label
                )
            }

            val pendingLabOrders = db.pendingLabOrderDao().getAllActiveReminderOrders()
            for (order in pendingLabOrders) {
                ReminderScheduler.scheduleLabOrderReminder(context, order)
            }
        }
    }

    companion object {
        val RESCHEDULE_ACTIONS = setOf(
            // NOTE: LOCKED_BOOT_COMPLETED is intentionally NOT handled here. The
            // database and key are protected by credential-encrypted storage, so
            // they are unreadable while the device is locked. BOOT_COMPLETED is
            // delivered once the user unlocks, at which point we can safely read
            // the DB and reschedule alarms.
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            "android.intent.action.QUICKBOOT_POWERON"
        )
    }
}

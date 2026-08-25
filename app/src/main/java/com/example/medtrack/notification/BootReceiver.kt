package com.example.medtrack.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.medtrack.data.MedTrackDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
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
    }
}

package com.example.medtrack.ui.reminder

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medtrack.MedTrackApplication
import com.example.medtrack.data.entity.MedicineReminder
import com.example.medtrack.notification.ReminderScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ReminderWithMedName(
    val reminder: MedicineReminder,
    val medicationName: String
)

class ReminderListViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as MedTrackApplication
    private val db = app.database

    val remindersWithMeds: StateFlow<List<ReminderWithMedName>> = db.medicineReminderDao()
        .getAllReminders()
        .map { reminders ->
            reminders.mapNotNull { reminder ->
                val medName = if (reminder.label.isNotBlank()) {
                    reminder.label
                } else {
                    val rxWithMeds = db.prescriptionDao().getPrescriptionWithMedicationsByIdOnce(reminder.prescriptionId)
                    rxWithMeds?.displayTitle ?: "Prescription"
                }
                ReminderWithMedName(reminder, medName)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleReminder(reminder: MedicineReminder, medicationName: String) {
        viewModelScope.launch {
            val updated = reminder.copy(isEnabled = !reminder.isEnabled)
            db.medicineReminderDao().update(updated)
            if (updated.isEnabled) {
                ReminderScheduler.scheduleReminder(app, updated, medicationName)
            } else {
                ReminderScheduler.cancelReminder(app, reminder.id)
            }
        }
    }

    fun deleteReminder(reminder: MedicineReminder) {
        viewModelScope.launch {
            db.medicineReminderDao().delete(reminder)
            ReminderScheduler.cancelReminder(app, reminder.id)
        }
    }
}

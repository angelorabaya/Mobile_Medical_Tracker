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
    private val container = app.container

    val remindersWithMeds: StateFlow<List<ReminderWithMedName>> = container.reminderRepository
        .getRemindersWithMedicationName()
        .map { list ->
            // The medication name (label, else prescription title) is resolved in
            // a single SQL join instead of one DB query per reminder (N+1).
            list.map { ReminderWithMedName(it.reminder, it.medicationName) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleReminder(reminder: MedicineReminder, medicationName: String) {
        viewModelScope.launch {
            val updated = reminder.copy(isEnabled = !reminder.isEnabled)
            container.reminderRepository.update(updated)
            if (updated.isEnabled) {
                ReminderScheduler.scheduleReminder(app, updated, medicationName)
            } else {
                ReminderScheduler.cancelReminder(app, reminder.id)
            }
        }
    }

    fun deleteReminder(reminder: MedicineReminder) {
        viewModelScope.launch {
            container.reminderRepository.delete(reminder)
            ReminderScheduler.cancelReminder(app, reminder.id)
        }
    }
}

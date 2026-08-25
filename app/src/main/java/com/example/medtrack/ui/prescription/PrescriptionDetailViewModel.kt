package com.example.medtrack.ui.prescription

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.medtrack.MedTrackApplication
import com.example.medtrack.data.entity.MedicineReminder
import com.example.medtrack.data.entity.PrescriptionMedication
import com.example.medtrack.data.entity.PrescriptionWithMedications
import com.example.medtrack.notification.ReminderScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PrescriptionDetailViewModel(
    private val app: MedTrackApplication,
    private val prescriptionId: Int
) : AndroidViewModel(app) {
    private val db = app.database

    val prescriptionWithMedications: StateFlow<PrescriptionWithMedications?> = db.prescriptionDao()
        .getPrescriptionWithMedicationsById(prescriptionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val reminders: StateFlow<List<MedicineReminder>> = db.medicineReminderDao()
        .getRemindersByPrescription(prescriptionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            val current = db.prescriptionDao().getPrescriptionWithMedicationsByIdOnce(prescriptionId)
            current?.medications?.forEach { med ->
                var updatedMed = med
                if (med.medicationName.contains("Atorvasta", ignoreCase = true)) {
                    updatedMed = updatedMed.copy(
                        medicationName = "Atorvastatin",
                        frequency = if (med.frequency.contains("0-0-1") || med.frequency.contains("1-0-1")) med.frequency else "0-0-1 (Evening / Bedtime only)"
                    )
                } else if (med.medicationName.contains("Liver Prime", ignoreCase = true)) {
                    updatedMed = updatedMed.copy(
                        medicationName = "Liver Prime HD",
                        frequency = if (med.frequency.contains("-")) med.frequency else "1-0-0 (Morning only)"
                    )
                } else if (med.medicationName.contains("Febuxostat", ignoreCase = true)) {
                    updatedMed = updatedMed.copy(
                        medicationName = "Febuxostat",
                        frequency = if (med.frequency.contains("-")) med.frequency else "0-0-1 (Evening / Bedtime only)"
                    )
                }
                if (updatedMed != med) {
                    db.prescriptionDao().updateMedication(updatedMed)
                }
            }
        }
    }

    fun toggleStatus() {
        viewModelScope.launch {
            val current = db.prescriptionDao().getPrescriptionByIdOnce(prescriptionId) ?: return@launch
            db.prescriptionDao().update(current.copy(isActive = !current.isActive))
        }
    }

    fun updateMedication(medication: PrescriptionMedication) {
        viewModelScope.launch {
            db.prescriptionDao().updateMedication(medication)
        }
    }

    fun addMedication(
        name: String,
        dosage: String,
        frequency: String,
        duration: String,
        instructions: String
    ) {
        viewModelScope.launch {
            val newMed = PrescriptionMedication(
                prescriptionId = prescriptionId,
                medicationName = name,
                dosage = dosage,
                frequency = frequency,
                duration = duration,
                instructions = instructions
            )
            db.prescriptionDao().insertMedication(newMed)
        }
    }

    fun deleteMedication(medication: PrescriptionMedication) {
        viewModelScope.launch {
            db.prescriptionDao().deleteMedication(medication)
        }
    }

    fun updatePrescriptionInfo(
        title: String,
        doctorName: String,
        datePrescribed: String,
        notes: String
    ) {
        viewModelScope.launch {
            val current = db.prescriptionDao().getPrescriptionByIdOnce(prescriptionId) ?: return@launch
            db.prescriptionDao().update(
                current.copy(
                    title = title,
                    doctorName = doctorName,
                    datePrescribed = datePrescribed,
                    notes = notes
                )
            )
        }
    }

    fun updatePrescriptionImage(newImageUri: String?) {
        viewModelScope.launch {
            val current = db.prescriptionDao().getPrescriptionByIdOnce(prescriptionId) ?: return@launch
            db.prescriptionDao().update(current.copy(imageUri = newImageUri))
        }
    }

    fun addReminder(time: String, drugLabel: String = "") {
        viewModelScope.launch {
            val current = db.prescriptionDao().getPrescriptionWithMedicationsByIdOnce(prescriptionId) ?: return@launch
            val reminderLabel = if (drugLabel.isNotBlank()) drugLabel else current.displayTitle
            val reminder = MedicineReminder(
                prescriptionId = prescriptionId,
                reminderTime = time,
                isEnabled = true,
                label = reminderLabel
            )
            val id = db.medicineReminderDao().insert(reminder)
            val saved = reminder.copy(id = id.toInt())
            ReminderScheduler.scheduleReminder(app, saved, reminderLabel)
        }
    }

    fun toggleReminder(reminder: MedicineReminder) {
        viewModelScope.launch {
            val updated = reminder.copy(isEnabled = !reminder.isEnabled)
            db.medicineReminderDao().update(updated)
            val current = db.prescriptionDao().getPrescriptionWithMedicationsByIdOnce(prescriptionId)
            val medName = if (reminder.label.isNotBlank()) reminder.label else (current?.displayTitle ?: "Medicine")
            if (updated.isEnabled) {
                ReminderScheduler.scheduleReminder(app, updated, medName)
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

    companion object {
        fun factory(prescriptionId: Int): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MedTrackApplication
                return PrescriptionDetailViewModel(app, prescriptionId) as T
            }
        }
    }
}

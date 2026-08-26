package com.example.medtrack.ui.prescription

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
    private val container = app.container

    val prescriptionWithMedications: StateFlow<PrescriptionWithMedications?> = container.prescriptionRepository
        .getPrescriptionWithMedicationsById(prescriptionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val reminders: StateFlow<List<MedicineReminder>> = container.reminderRepository
        .getRemindersByPrescription(prescriptionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleStatus() {
        viewModelScope.launch {
            val current = container.prescriptionRepository.getPrescriptionByIdOnce(prescriptionId) ?: return@launch
            container.prescriptionRepository.update(current.copy(isActive = !current.isActive))
        }
    }

    fun updateMedication(medication: PrescriptionMedication) {
        viewModelScope.launch {
            container.prescriptionRepository.updateMedication(medication)
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
            container.prescriptionRepository.insertMedication(newMed)
        }
    }

    fun deleteMedication(medication: PrescriptionMedication) {
        viewModelScope.launch {
            container.prescriptionRepository.deleteMedication(medication)
        }
    }

    fun updatePrescriptionInfo(
        title: String,
        doctorName: String,
        datePrescribed: String,
        notes: String
    ) {
        viewModelScope.launch {
            val current = container.prescriptionRepository.getPrescriptionByIdOnce(prescriptionId) ?: return@launch
            container.prescriptionRepository.update(
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
            val current = container.prescriptionRepository.getPrescriptionByIdOnce(prescriptionId) ?: return@launch
            container.prescriptionRepository.update(current.copy(imageUri = newImageUri))
        }
    }

    fun addReminder(time: String, drugLabel: String = "") {
        viewModelScope.launch {
            val current = container.prescriptionRepository.getPrescriptionWithMedicationsByIdOnce(prescriptionId) ?: return@launch
            val reminderLabel = if (drugLabel.isNotBlank()) drugLabel else current.displayTitle
            val reminder = MedicineReminder(
                prescriptionId = prescriptionId,
                reminderTime = time,
                isEnabled = true,
                label = reminderLabel
            )
            val id = container.reminderRepository.insert(reminder)
            val saved = reminder.copy(id = id.toInt())
            ReminderScheduler.scheduleReminder(app, saved, reminderLabel)
        }
    }

    fun toggleReminder(reminder: MedicineReminder) {
        viewModelScope.launch {
            val updated = reminder.copy(isEnabled = !reminder.isEnabled)
            container.reminderRepository.update(updated)
            val current = container.prescriptionRepository.getPrescriptionWithMedicationsByIdOnce(prescriptionId)
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
            container.reminderRepository.delete(reminder)
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

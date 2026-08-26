package com.example.medtrack.ui.prescription

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medtrack.MedTrackApplication
import com.example.medtrack.data.entity.Prescription
import com.example.medtrack.data.entity.PrescriptionMedication
import com.example.medtrack.util.FrequencyHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MedicationInput(
    var name: String = "",
    var dosage: String = "",
    var morning: String = "1",
    var noon: String = "0",
    var night: String = "0",
    var duration: String = "",
    var instructions: String = ""
) {
    val frequency: String
        get() = FrequencyHelper.formatSchedule(morning, noon, night)
}

class AddPrescriptionViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as MedTrackApplication).container

    var title by mutableStateOf("")
    var doctorName by mutableStateOf("")
    var datePrescribed by mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    var notes by mutableStateOf("")
    var imageUri by mutableStateOf<String?>(null)
    var errorMessage by mutableStateOf<String?>(null)

    val medications = mutableStateListOf(
        MedicationInput()
    )

    fun addMedication() {
        medications.add(MedicationInput())
    }

    fun removeMedication(index: Int) {
        if (medications.size > 1) {
            medications.removeAt(index)
        }
    }

    fun updateMedicationName(index: Int, value: String) {
        if (index in medications.indices) {
            medications[index] = medications[index].copy(name = value)
        }
    }

    fun updateMedicationDosage(index: Int, value: String) {
        if (index in medications.indices) {
            medications[index] = medications[index].copy(dosage = value)
        }
    }

    fun updateMedicationMorning(index: Int, value: String) {
        if (index in medications.indices) {
            medications[index] = medications[index].copy(morning = value)
        }
    }

    fun updateMedicationNoon(index: Int, value: String) {
        if (index in medications.indices) {
            medications[index] = medications[index].copy(noon = value)
        }
    }

    fun updateMedicationNight(index: Int, value: String) {
        if (index in medications.indices) {
            medications[index] = medications[index].copy(night = value)
        }
    }

    fun updateMedicationDuration(index: Int, value: String) {
        if (index in medications.indices) {
            medications[index] = medications[index].copy(duration = value)
        }
    }

    fun updateMedicationInstructions(index: Int, value: String) {
        if (index in medications.indices) {
            medications[index] = medications[index].copy(instructions = value)
        }
    }

    fun resetState() {
        title = ""
        doctorName = ""
        datePrescribed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        notes = ""
        imageUri = null
        errorMessage = null
        medications.clear()
        medications.add(MedicationInput())
    }

    fun savePrescription(onComplete: () -> Unit) {
        val validMeds = medications.filter { it.name.isNotBlank() }
        if (validMeds.isEmpty()) {
            errorMessage = "Please enter at least one medication name"
            return
        }
        if (datePrescribed.isBlank()) {
            errorMessage = "Date prescribed is required"
            return
        }
        errorMessage = null

        viewModelScope.launch {
            val patient = container.patientRepository.getPatientOnce() ?: return@launch

            val prescription = Prescription(
                patientId = patient.id,
                title = title.trim(),
                doctorName = doctorName.trim(),
                datePrescribed = datePrescribed.trim(),
                notes = notes.trim(),
                imageUri = imageUri
            )

            val prescriptionMeds = validMeds.map { input ->
                PrescriptionMedication(
                    prescriptionId = 0,
                    medicationName = input.name.trim(),
                    dosage = input.dosage.trim(),
                    frequency = input.frequency,
                    duration = input.duration.trim(),
                    instructions = input.instructions.trim()
                )
            }

            container.prescriptionRepository.insertPrescriptionWithMedications(prescription, prescriptionMeds)
            resetState()
            onComplete()
        }
    }
}

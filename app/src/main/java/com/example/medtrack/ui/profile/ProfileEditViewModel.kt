package com.example.medtrack.ui.profile

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medtrack.MedTrackApplication
import kotlinx.coroutines.launch

class ProfileEditViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as MedTrackApplication).database

    var fullName by mutableStateOf("")
    var dateOfBirth by mutableStateOf("")
    var gender by mutableStateOf("")
    var bloodType by mutableStateOf("")
    var allergies by mutableStateOf("")
    var emergencyContact by mutableStateOf("")
    var photoUri by mutableStateOf("")
    var errorMessage by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(true)

    private var patientId: Int = 0

    val genderOptions = listOf("Male", "Female", "Other")
    val bloodTypeOptions = listOf("", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    init {
        viewModelScope.launch {
            val patient = db.patientDao().getPatientOnce()
            if (patient != null) {
                patientId = patient.id
                fullName = patient.fullName
                dateOfBirth = patient.dateOfBirth
                gender = patient.gender
                bloodType = patient.bloodType
                allergies = patient.allergies
                emergencyContact = patient.emergencyContact
                photoUri = patient.photoUri
            }
            isLoading = false
        }
    }

    fun updatePatient(onComplete: () -> Unit) {
        if (fullName.isBlank()) {
            errorMessage = "Full name is required"
            return
        }
        errorMessage = null
        viewModelScope.launch {
            db.patientDao().update(
                com.example.medtrack.data.entity.Patient(
                    id = patientId,
                    fullName = fullName.trim(),
                    dateOfBirth = dateOfBirth.trim(),
                    gender = gender,
                    bloodType = bloodType,
                    allergies = allergies.trim(),
                    emergencyContact = emergencyContact.trim(),
                    photoUri = photoUri.trim()
                )
            )
            onComplete()
        }
    }
}

package com.example.medtrack.ui.registration

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medtrack.MedTrackApplication
import com.example.medtrack.data.entity.Patient
import kotlinx.coroutines.launch

class RegistrationViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as MedTrackApplication).database

    var fullName by mutableStateOf("")
    var dateOfBirth by mutableStateOf("")
    var gender by mutableStateOf("")
    var bloodType by mutableStateOf("")
    var allergies by mutableStateOf("")
    var emergencyContact by mutableStateOf("")
    var photoUri by mutableStateOf("")
    var errorMessage by mutableStateOf<String?>(null)

    val genderOptions = listOf("Male", "Female", "Other")
    val bloodTypeOptions = listOf("", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    fun savePatient(onComplete: () -> Unit) {
        if (fullName.isBlank()) {
            errorMessage = "Full name is required"
            return
        }
        errorMessage = null
        viewModelScope.launch {
            db.patientDao().insert(
                Patient(
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

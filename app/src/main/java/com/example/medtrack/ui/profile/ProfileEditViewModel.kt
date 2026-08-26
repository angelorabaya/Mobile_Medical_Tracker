package com.example.medtrack.ui.profile

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medtrack.MedTrackApplication
import com.example.medtrack.data.entity.Patient
import com.example.medtrack.util.DataExporter
import kotlinx.coroutines.launch
import java.io.File

class ProfileEditViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as MedTrackApplication).container
    private val dataExporter = DataExporter(container)

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
            val patient = container.patientRepository.getPatientOnce()
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
            container.patientRepository.update(
                Patient(
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

    /**
     * Exports all health data to a JSON file in the cache directory and reports
     * its absolute path (or null on failure) so the UI can share it.
     */
    fun exportData(onResult: (String?) -> Unit) {
        viewModelScope.launch {
            val json = runCatching { dataExporter.exportJson() }.getOrNull()
            if (json == null) {
                onResult(null)
                return@launch
            }
            val path = runCatching {
                val file = File(
                    getApplication<Application>().cacheDir,
                    "vitalsiq_export_${System.currentTimeMillis()}.json"
                )
                file.writeText(json)
                file.absolutePath
            }.getOrNull()
            onResult(path)
        }
    }

    /**
     * Restores data from a JSON file chosen by the user. Replaces all current
     * data; the caller must confirm before invoking.
     */
    fun importData(uri: Uri, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val json = runCatching {
                getApplication<Application>().contentResolver
                    .openInputStream(uri)
                    ?.bufferedReader()
                    ?.use { it.readText() }
            }.getOrNull()
            if (json.isNullOrBlank()) {
                onComplete(false)
                return@launch
            }
            val ok = runCatching { dataExporter.importJson(json) }.isSuccess
            onComplete(ok)
        }
    }
}

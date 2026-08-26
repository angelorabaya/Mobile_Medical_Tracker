package com.example.medtrack.data.repository

import com.example.medtrack.data.dao.PatientDao
import com.example.medtrack.data.entity.Patient
import com.example.medtrack.util.ImageUtils
import kotlinx.coroutines.flow.Flow

class PatientRepository(
    private val patientDao: PatientDao
) {
    fun getPatient(): Flow<Patient?> = patientDao.getPatient()

    suspend fun getPatientOnce(): Patient? = patientDao.getPatientOnce()

    suspend fun insert(patient: Patient): Long = patientDao.insert(patient)

    /** Updates the profile, deleting the old photo file if it was replaced. */
    suspend fun update(patient: Patient) {
        val old = patientDao.getPatientOnce()
        if (old != null && old.photoUri.isNotBlank() && old.photoUri != patient.photoUri) {
            ImageUtils.deleteImageFile(old.photoUri)
        }
        patientDao.update(patient)
    }

    suspend fun getPatientCount(): Int = patientDao.getPatientCount()

    suspend fun deleteAll() = patientDao.deleteAll()
}

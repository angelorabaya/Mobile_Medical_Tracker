package com.example.medtrack.data.repository

import com.example.medtrack.data.dao.MedicineReminderDao
import com.example.medtrack.data.dao.PrescriptionDao
import com.example.medtrack.data.entity.Prescription
import com.example.medtrack.data.entity.PrescriptionMedication
import com.example.medtrack.data.entity.PrescriptionWithMedications
import com.example.medtrack.util.ImageUtils
import kotlinx.coroutines.flow.Flow

class PrescriptionRepository(
    private val prescriptionDao: PrescriptionDao,
    private val medicineReminderDao: MedicineReminderDao
) {
    fun getPrescriptionsWithMedications(patientId: Int): Flow<List<PrescriptionWithMedications>> =
        prescriptionDao.getPrescriptionsWithMedications(patientId)

    fun getActivePrescriptionsWithMedications(patientId: Int): Flow<List<PrescriptionWithMedications>> =
        prescriptionDao.getActivePrescriptionsWithMedications(patientId)

    fun getActivePrescriptions(patientId: Int): Flow<List<Prescription>> =
        prescriptionDao.getActivePrescriptions(patientId)

    fun getPrescriptionById(id: Int): Flow<Prescription?> = prescriptionDao.getPrescriptionById(id)

    suspend fun getPrescriptionByIdOnce(id: Int): Prescription? = prescriptionDao.getPrescriptionByIdOnce(id)

    fun getPrescriptionWithMedicationsById(id: Int): Flow<PrescriptionWithMedications?> =
        prescriptionDao.getPrescriptionWithMedicationsById(id)

    suspend fun getPrescriptionWithMedicationsByIdOnce(id: Int): PrescriptionWithMedications? =
        prescriptionDao.getPrescriptionWithMedicationsByIdOnce(id)

    suspend fun getAllPrescriptionsWithMedicationsOnce(): List<PrescriptionWithMedications> =
        prescriptionDao.getAllPrescriptionsWithMedicationsOnce()

    fun getActivePrescriptionCount(patientId: Int): Flow<Int> =
        prescriptionDao.getActivePrescriptionCount(patientId)

    suspend fun insertPrescriptionWithMedications(
        prescription: Prescription,
        medications: List<PrescriptionMedication>
    ): Long = prescriptionDao.insertPrescriptionWithMedications(prescription, medications)

    suspend fun updatePrescriptionWithMedications(
        prescription: Prescription,
        medications: List<PrescriptionMedication>
    ) = prescriptionDao.updatePrescriptionWithMedications(prescription, medications)

    /** Updates a prescription, deleting the previous image file when replaced. */
    suspend fun update(prescription: Prescription) {
        val old = prescriptionDao.getPrescriptionByIdOnce(prescription.id)
        if (old != null && old.imageUri != prescription.imageUri && !old.imageUri.isNullOrBlank()) {
            ImageUtils.deleteImageFile(old.imageUri)
        }
        prescriptionDao.update(prescription)
    }

    suspend fun updateMedication(medication: PrescriptionMedication) =
        prescriptionDao.updateMedication(medication)

    suspend fun insertMedication(medication: PrescriptionMedication): Long =
        prescriptionDao.insertMedication(medication)

    suspend fun deleteMedication(medication: PrescriptionMedication) =
        prescriptionDao.deleteMedication(medication)

    /** Deletes a prescription and its image file; reminders cascade via FK. */
    suspend fun delete(prescription: Prescription) {
        ImageUtils.deleteImageFile(prescription.imageUri)
        prescriptionDao.delete(prescription)
    }
}

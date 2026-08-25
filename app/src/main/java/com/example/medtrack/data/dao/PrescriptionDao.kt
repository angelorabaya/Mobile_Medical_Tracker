package com.example.medtrack.data.dao

import androidx.room.*
import com.example.medtrack.data.entity.Prescription
import com.example.medtrack.data.entity.PrescriptionMedication
import com.example.medtrack.data.entity.PrescriptionWithMedications
import kotlinx.coroutines.flow.Flow

@Dao
interface PrescriptionDao {
    @Query("SELECT * FROM prescriptions WHERE patientId = :patientId ORDER BY datePrescribed DESC, createdAt DESC")
    fun getPrescriptionsByPatient(patientId: Int): Flow<List<Prescription>>

    @Transaction
    @Query("SELECT * FROM prescriptions WHERE patientId = :patientId ORDER BY datePrescribed DESC, createdAt DESC")
    fun getPrescriptionsWithMedications(patientId: Int): Flow<List<PrescriptionWithMedications>>

    @Transaction
    @Query("SELECT * FROM prescriptions WHERE patientId = :patientId AND isActive = 1 ORDER BY datePrescribed DESC, createdAt DESC")
    fun getActivePrescriptionsWithMedications(patientId: Int): Flow<List<PrescriptionWithMedications>>

    @Query("SELECT * FROM prescriptions WHERE id = :id")
    fun getPrescriptionById(id: Int): Flow<Prescription?>

    @Query("SELECT * FROM prescriptions WHERE id = :id")
    suspend fun getPrescriptionByIdOnce(id: Int): Prescription?

    @Transaction
    @Query("SELECT * FROM prescriptions WHERE id = :id")
    fun getPrescriptionWithMedicationsById(id: Int): Flow<PrescriptionWithMedications?>

    @Transaction
    @Query("SELECT * FROM prescriptions WHERE id = :id")
    suspend fun getPrescriptionWithMedicationsByIdOnce(id: Int): PrescriptionWithMedications?

    @Query("SELECT * FROM prescriptions WHERE patientId = :patientId AND isActive = 1 ORDER BY datePrescribed DESC")
    fun getActivePrescriptions(patientId: Int): Flow<List<Prescription>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prescription: Prescription): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedications(medications: List<PrescriptionMedication>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: PrescriptionMedication): Long

    @Update
    suspend fun update(prescription: Prescription)

    @Update
    suspend fun updateMedication(medication: PrescriptionMedication)

    @Delete
    suspend fun deleteMedication(medication: PrescriptionMedication)

    @Query("DELETE FROM prescription_medications WHERE prescriptionId = :prescriptionId")
    suspend fun deleteMedicationsByPrescriptionId(prescriptionId: Int)

    @Delete
    suspend fun delete(prescription: Prescription)

    @Query("SELECT COUNT(*) FROM prescriptions WHERE patientId = :patientId AND isActive = 1")
    fun getActivePrescriptionCount(patientId: Int): Flow<Int>

    @Transaction
    suspend fun insertPrescriptionWithMedications(
        prescription: Prescription,
        medications: List<PrescriptionMedication>
    ): Long {
        val prescriptionId = insert(prescription)
        val medicationsWithId = medications.map { it.copy(prescriptionId = prescriptionId.toInt()) }
        insertMedications(medicationsWithId)
        return prescriptionId
    }

    @Transaction
    suspend fun updatePrescriptionWithMedications(
        prescription: Prescription,
        medications: List<PrescriptionMedication>
    ) {
        update(prescription)
        deleteMedicationsByPrescriptionId(prescription.id)
        val medicationsWithId = medications.map { it.copy(id = 0, prescriptionId = prescription.id) }
        insertMedications(medicationsWithId)
    }
}

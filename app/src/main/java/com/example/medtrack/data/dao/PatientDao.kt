package com.example.medtrack.data.dao

import androidx.room.*
import com.example.medtrack.data.entity.Patient
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {
    @Query("SELECT * FROM patients LIMIT 1")
    fun getPatient(): Flow<Patient?>

    @Query("SELECT * FROM patients LIMIT 1")
    suspend fun getPatientOnce(): Patient?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(patient: Patient): Long

    @Update
    suspend fun update(patient: Patient)

    @Query("SELECT COUNT(*) FROM patients")
    suspend fun getPatientCount(): Int
}

package com.example.medtrack.data.dao

import androidx.room.*
import com.example.medtrack.data.entity.BmiRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface BmiRecordDao {
    @Query("SELECT * FROM bmi_records ORDER BY calculatedAt DESC LIMIT 1")
    fun getLatestBmiRecord(): Flow<BmiRecord?>

    @Query("SELECT * FROM bmi_records WHERE patientId = :patientId ORDER BY calculatedAt DESC LIMIT 1")
    fun getLatestBmiRecordForPatient(patientId: Int): Flow<BmiRecord?>

    @Query("SELECT * FROM bmi_records WHERE patientId = :patientId ORDER BY calculatedAt DESC LIMIT 1")
    suspend fun getLatestBmiRecordOnce(patientId: Int): BmiRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: BmiRecord): Long

    @Query("DELETE FROM bmi_records WHERE patientId = :patientId")
    suspend fun deleteForPatient(patientId: Int)
}

package com.example.medtrack.data.dao

import androidx.room.*
import com.example.medtrack.data.entity.LabTestType
import kotlinx.coroutines.flow.Flow

@Dao
interface LabTestTypeDao {
    @Query("SELECT * FROM lab_test_types ORDER BY name ASC")
    fun getAllTestTypes(): Flow<List<LabTestType>>

    @Query("SELECT * FROM lab_test_types ORDER BY name ASC")
    suspend fun getAllTestTypesOnce(): List<LabTestType>

    @Query("SELECT COUNT(*) FROM lab_test_types")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(testType: LabTestType): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(testTypes: List<LabTestType>)

    @Update
    suspend fun update(testType: LabTestType)

    @Delete
    suspend fun delete(testType: LabTestType)

    @Query("DELETE FROM lab_test_types WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM lab_test_types")
    suspend fun deleteAll()
}

package com.example.medtrack.data.dao

import androidx.room.*
import com.example.medtrack.data.entity.LabTest
import com.example.medtrack.data.entity.LabTestItem
import com.example.medtrack.data.entity.LabTestWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface LabTestDao {
    @Query("SELECT * FROM lab_tests WHERE patientId = :patientId ORDER BY testDate DESC, createdAt DESC")
    fun getLabTestsByPatient(patientId: Int): Flow<List<LabTest>>

    @Transaction
    @Query("SELECT * FROM lab_tests WHERE patientId = :patientId ORDER BY testDate DESC, createdAt DESC")
    fun getLabTestsWithItems(patientId: Int): Flow<List<LabTestWithItems>>

    @Query("SELECT * FROM lab_tests WHERE id = :id")
    fun getLabTestById(id: Int): Flow<LabTest?>

    @Query("SELECT * FROM lab_tests WHERE id = :id")
    suspend fun getLabTestByIdOnce(id: Int): LabTest?

    @Transaction
    @Query("SELECT * FROM lab_tests WHERE id = :id")
    fun getLabTestWithItemsById(id: Int): Flow<LabTestWithItems?>

    @Transaction
    @Query("SELECT * FROM lab_tests WHERE id = :id")
    suspend fun getLabTestWithItemsByIdOnce(id: Int): LabTestWithItems?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(labTest: LabTest): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<LabTestItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: LabTestItem): Long

    @Update
    suspend fun update(labTest: LabTest)

    @Update
    suspend fun updateItem(item: LabTestItem)

    @Delete
    suspend fun deleteItem(item: LabTestItem)

    @Query("DELETE FROM lab_test_items WHERE labTestId = :labTestId")
    suspend fun deleteItemsByLabTestId(labTestId: Int)

    @Delete
    suspend fun delete(labTest: LabTest)

    @Query("SELECT COUNT(*) FROM lab_tests WHERE patientId = :patientId")
    fun getLabTestCount(patientId: Int): Flow<Int>

    @Transaction
    suspend fun insertLabTestWithItems(
        labTest: LabTest,
        items: List<LabTestItem>
    ): Long {
        val labTestId = insert(labTest)
        val itemsWithId = items.map { it.copy(labTestId = labTestId.toInt()) }
        insertItems(itemsWithId)
        return labTestId
    }

    @Transaction
    suspend fun updateLabTestWithItems(
        labTest: LabTest,
        items: List<LabTestItem>
    ) {
        update(labTest)
        deleteItemsByLabTestId(labTest.id)
        val itemsWithId = items.map { it.copy(id = 0, labTestId = labTest.id) }
        insertItems(itemsWithId)
    }
}

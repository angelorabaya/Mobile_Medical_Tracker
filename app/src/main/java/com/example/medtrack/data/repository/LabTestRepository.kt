package com.example.medtrack.data.repository

import android.content.Context
import com.example.medtrack.data.dao.LabTestDao
import com.example.medtrack.data.entity.LabTest
import com.example.medtrack.data.entity.LabTestItem
import com.example.medtrack.data.entity.LabTestWithItems
import com.example.medtrack.util.ImageUtils
import kotlinx.coroutines.flow.Flow

class LabTestRepository(
    private val labTestDao: LabTestDao,
    private val context: Context
) {
    fun getLabTestsWithItems(patientId: Int): Flow<List<LabTestWithItems>> =
        labTestDao.getLabTestsWithItems(patientId)

    fun getLabTestById(id: Int): Flow<LabTest?> = labTestDao.getLabTestById(id)

    suspend fun getLabTestByIdOnce(id: Int): LabTest? = labTestDao.getLabTestByIdOnce(id)

    fun getLabTestWithItemsById(id: Int): Flow<LabTestWithItems?> =
        labTestDao.getLabTestWithItemsById(id)

    suspend fun getLabTestWithItemsByIdOnce(id: Int): LabTestWithItems? =
        labTestDao.getLabTestWithItemsByIdOnce(id)

    suspend fun getAllLabTestsWithItemsOnce(): List<LabTestWithItems> =
        labTestDao.getAllLabTestsWithItemsOnce()

    fun getLabTestCount(patientId: Int): Flow<Int> = labTestDao.getLabTestCount(patientId)

    suspend fun insertLabTestWithItems(labTest: LabTest, items: List<LabTestItem>): Long =
        labTestDao.insertLabTestWithItems(labTest, items)

    suspend fun updateLabTestWithItems(labTest: LabTest, items: List<LabTestItem>) =
        labTestDao.updateLabTestWithItems(labTest, items)

    /** Updates a lab test, deleting the previous image file when replaced. */
    suspend fun update(labTest: LabTest) {
        val old = labTestDao.getLabTestByIdOnce(labTest.id)
        if (old != null && old.imageUri != labTest.imageUri && !old.imageUri.isNullOrBlank()) {
            ImageUtils.deleteImageFile(old.imageUri)
        }
        labTestDao.update(labTest)
    }

    suspend fun updateItem(item: LabTestItem) = labTestDao.updateItem(item)

    suspend fun insertItem(item: LabTestItem): Long = labTestDao.insertItem(item)

    suspend fun deleteItem(item: LabTestItem) = labTestDao.deleteItem(item)

    /** Deletes a lab test, removing its attached image file to avoid orphans. */
    suspend fun delete(labTest: LabTest) {
        ImageUtils.deleteImageFile(labTest.imageUri)
        labTestDao.delete(labTest)
    }
}

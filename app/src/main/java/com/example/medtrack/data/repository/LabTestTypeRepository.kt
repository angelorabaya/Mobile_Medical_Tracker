package com.example.medtrack.data.repository

import com.example.medtrack.data.dao.LabTestTypeDao
import com.example.medtrack.data.entity.LabTestType
import kotlinx.coroutines.flow.Flow

class LabTestTypeRepository(
    private val labTestTypeDao: LabTestTypeDao
) {
    fun getAllTestTypes(): Flow<List<LabTestType>> = labTestTypeDao.getAllTestTypes()

    suspend fun getAllTestTypesOnce(): List<LabTestType> = labTestTypeDao.getAllTestTypesOnce()

    suspend fun insert(type: LabTestType): Long = labTestTypeDao.insert(type)

    suspend fun insertAll(types: List<LabTestType>) = labTestTypeDao.insertAll(types)

    suspend fun update(type: LabTestType) = labTestTypeDao.update(type)

    suspend fun delete(type: LabTestType) = labTestTypeDao.delete(type)

    suspend fun deleteAll() = labTestTypeDao.deleteAll()
}

package com.example.medtrack.data.repository

import com.example.medtrack.data.dao.BmiRecordDao
import com.example.medtrack.data.entity.BmiRecord
import kotlinx.coroutines.flow.Flow

class BmiRepository(
    private val bmiRecordDao: BmiRecordDao
) {
    fun getLatestBmiRecordForPatient(patientId: Int): Flow<BmiRecord?> =
        bmiRecordDao.getLatestBmiRecordForPatient(patientId)

    suspend fun insert(record: BmiRecord): Long = bmiRecordDao.insert(record)

    suspend fun getAllBmiRecordsOnce(): List<BmiRecord> = bmiRecordDao.getAllBmiRecordsOnce()
}

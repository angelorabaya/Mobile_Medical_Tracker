package com.example.medtrack.data.repository

import com.example.medtrack.data.dao.PendingLabOrderDao
import com.example.medtrack.data.entity.PendingLabOrder
import kotlinx.coroutines.flow.Flow

class PendingLabOrderRepository(
    private val pendingLabOrderDao: PendingLabOrderDao
) {
    fun getPendingOrdersForPatient(patientId: Int): Flow<List<PendingLabOrder>> =
        pendingLabOrderDao.getPendingOrdersForPatient(patientId)

    fun getAllOrdersForPatient(patientId: Int): Flow<List<PendingLabOrder>> =
        pendingLabOrderDao.getAllOrdersForPatient(patientId)

    suspend fun getOrderById(id: Int): PendingLabOrder? = pendingLabOrderDao.getOrderById(id)

    suspend fun getAllActiveReminderOrders(): List<PendingLabOrder> =
        pendingLabOrderDao.getAllActiveReminderOrders()

    suspend fun getAllOrdersOnce(): List<PendingLabOrder> =
        pendingLabOrderDao.getAllOrdersOnce()

    suspend fun insert(order: PendingLabOrder): Long = pendingLabOrderDao.insert(order)

    suspend fun update(order: PendingLabOrder) = pendingLabOrderDao.update(order)

    suspend fun delete(order: PendingLabOrder) = pendingLabOrderDao.delete(order)

    suspend fun markAsCompleted(id: Int) = pendingLabOrderDao.markAsCompleted(id)
}

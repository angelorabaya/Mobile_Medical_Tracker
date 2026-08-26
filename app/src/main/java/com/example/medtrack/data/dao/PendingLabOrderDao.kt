package com.example.medtrack.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.medtrack.data.entity.PendingLabOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingLabOrderDao {
    @Query("SELECT * FROM pending_lab_orders WHERE patientId = :patientId AND isCompleted = 0 ORDER BY scheduledDate ASC, scheduledTime ASC")
    fun getPendingOrdersForPatient(patientId: Int): Flow<List<PendingLabOrder>>

    @Query("SELECT * FROM pending_lab_orders WHERE patientId = :patientId ORDER BY scheduledDate ASC, scheduledTime ASC")
    fun getAllOrdersForPatient(patientId: Int): Flow<List<PendingLabOrder>>

    @Query("SELECT * FROM pending_lab_orders WHERE id = :id")
    suspend fun getOrderById(id: Int): PendingLabOrder?

    @Query("SELECT * FROM pending_lab_orders WHERE isCompleted = 0 AND isReminderEnabled = 1")
    suspend fun getAllActiveReminderOrders(): List<PendingLabOrder>

    @Query("SELECT * FROM pending_lab_orders")
    suspend fun getAllOrdersOnce(): List<PendingLabOrder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(order: PendingLabOrder): Long

    @Update
    suspend fun update(order: PendingLabOrder)

    @Delete
    suspend fun delete(order: PendingLabOrder)

    @Query("UPDATE pending_lab_orders SET isCompleted = 1 WHERE id = :id")
    suspend fun markAsCompleted(id: Int)
}

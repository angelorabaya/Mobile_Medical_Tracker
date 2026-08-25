package com.example.medtrack.data.dao

import androidx.room.*
import com.example.medtrack.data.entity.MedicineReminder
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineReminderDao {
    @Query("SELECT * FROM medicine_reminders WHERE prescriptionId = :prescriptionId ORDER BY reminderTime ASC")
    fun getRemindersByPrescription(prescriptionId: Int): Flow<List<MedicineReminder>>

    @Query("SELECT * FROM medicine_reminders WHERE id = :id")
    suspend fun getReminderById(id: Int): MedicineReminder?

    @Query("SELECT * FROM medicine_reminders WHERE isEnabled = 1 ORDER BY reminderTime ASC")
    fun getEnabledReminders(): Flow<List<MedicineReminder>>

    @Query("SELECT * FROM medicine_reminders ORDER BY reminderTime ASC")
    fun getAllReminders(): Flow<List<MedicineReminder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: MedicineReminder): Long

    @Update
    suspend fun update(reminder: MedicineReminder)

    @Delete
    suspend fun delete(reminder: MedicineReminder)

    @Query("DELETE FROM medicine_reminders WHERE prescriptionId = :prescriptionId")
    suspend fun deleteByPrescription(prescriptionId: Int)
}

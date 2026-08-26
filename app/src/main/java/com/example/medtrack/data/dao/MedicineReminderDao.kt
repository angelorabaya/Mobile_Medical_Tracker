package com.example.medtrack.data.dao

import androidx.room.*
import com.example.medtrack.data.entity.MedicineReminder
import kotlinx.coroutines.flow.Flow

/** A reminder paired with the medication name to display (label, else prescription title). */
data class ReminderWithMedicationName(
    @Embedded val reminder: MedicineReminder,
    @ColumnInfo(name = "medication_name") val medicationName: String
)

@Dao
interface MedicineReminderDao {
    @Query("SELECT * FROM medicine_reminders WHERE prescriptionId = :prescriptionId ORDER BY reminderTime ASC")
    fun getRemindersByPrescription(prescriptionId: Int): Flow<List<MedicineReminder>>

    @Query(
        """
        SELECT r.*,
               COALESCE(NULLIF(r.label, ''), p.title, 'Prescription') AS medication_name
        FROM medicine_reminders r
        LEFT JOIN prescriptions p ON r.prescriptionId = p.id
        ORDER BY r.reminderTime ASC
        """
    )
    fun getRemindersWithMedicationName(): Flow<List<ReminderWithMedicationName>>

    @Query("SELECT * FROM medicine_reminders WHERE id = :id")
    suspend fun getReminderById(id: Int): MedicineReminder?

    @Query("SELECT * FROM medicine_reminders WHERE isEnabled = 1 ORDER BY reminderTime ASC")
    fun getEnabledReminders(): Flow<List<MedicineReminder>>

    @Query("SELECT * FROM medicine_reminders ORDER BY reminderTime ASC")
    fun getAllReminders(): Flow<List<MedicineReminder>>

    @Query("SELECT * FROM medicine_reminders")
    suspend fun getAllRemindersOnce(): List<MedicineReminder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: MedicineReminder): Long

    @Update
    suspend fun update(reminder: MedicineReminder)

    @Delete
    suspend fun delete(reminder: MedicineReminder)

    @Query("DELETE FROM medicine_reminders WHERE prescriptionId = :prescriptionId")
    suspend fun deleteByPrescription(prescriptionId: Int)
}

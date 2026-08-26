package com.example.medtrack.data.repository

import com.example.medtrack.data.dao.MedicineReminderDao
import com.example.medtrack.data.dao.ReminderWithMedicationName
import com.example.medtrack.data.entity.MedicineReminder
import kotlinx.coroutines.flow.Flow

class ReminderRepository(
    private val medicineReminderDao: MedicineReminderDao
) {
    fun getRemindersByPrescription(prescriptionId: Int): Flow<List<MedicineReminder>> =
        medicineReminderDao.getRemindersByPrescription(prescriptionId)

    /** All reminders with their display medication name, resolved in a single join. */
    fun getRemindersWithMedicationName(): Flow<List<ReminderWithMedicationName>> =
        medicineReminderDao.getRemindersWithMedicationName()

    suspend fun getReminderById(id: Int): MedicineReminder? =
        medicineReminderDao.getReminderById(id)

    fun getEnabledReminders(): Flow<List<MedicineReminder>> =
        medicineReminderDao.getEnabledReminders()

    fun getAllReminders(): Flow<List<MedicineReminder>> =
        medicineReminderDao.getAllReminders()

    suspend fun getAllRemindersOnce(): List<MedicineReminder> =
        medicineReminderDao.getAllRemindersOnce()

    suspend fun insert(reminder: MedicineReminder): Long =
        medicineReminderDao.insert(reminder)

    suspend fun update(reminder: MedicineReminder) = medicineReminderDao.update(reminder)

    suspend fun delete(reminder: MedicineReminder) = medicineReminderDao.delete(reminder)

    suspend fun deleteByPrescription(prescriptionId: Int) =
        medicineReminderDao.deleteByPrescription(prescriptionId)
}

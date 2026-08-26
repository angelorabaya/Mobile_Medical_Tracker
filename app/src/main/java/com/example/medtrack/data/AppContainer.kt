package com.example.medtrack.data

import android.content.Context
import com.example.medtrack.data.repository.BmiRepository
import com.example.medtrack.data.repository.LabTestRepository
import com.example.medtrack.data.repository.LabTestTypeRepository
import com.example.medtrack.data.repository.PatientRepository
import com.example.medtrack.data.repository.PendingLabOrderRepository
import com.example.medtrack.data.repository.PrescriptionRepository
import com.example.medtrack.data.repository.ReminderRepository

/**
 * Lightweight manual dependency container (service locator) exposing the data
 * layer to ViewModels. Kept intentionally simple; can be replaced by Hilt once
 * the project pins a Kotlin toolchain version compatible with the Hilt KSP
 * processor (the project currently uses Kotlin 2.3.x).
 */
class AppContainer(context: Context) {
    private val database = MedTrackDatabase.getDatabase(context)

    val patientRepository = PatientRepository(database.patientDao(), context)
    val labTestRepository = LabTestRepository(database.labTestDao(), context)
    val prescriptionRepository = PrescriptionRepository(
        database.prescriptionDao(),
        database.medicineReminderDao(),
        context
    )
    val reminderRepository = ReminderRepository(database.medicineReminderDao())
    val labTestTypeRepository = LabTestTypeRepository(database.labTestTypeDao())
    val bmiRepository = BmiRepository(database.bmiRecordDao())
    val pendingLabOrderRepository = PendingLabOrderRepository(database.pendingLabOrderDao())
}

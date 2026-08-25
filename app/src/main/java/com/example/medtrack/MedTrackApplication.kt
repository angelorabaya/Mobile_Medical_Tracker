package com.example.medtrack

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.medtrack.data.MedTrackDatabase
import com.example.medtrack.data.entity.Patient
import com.example.medtrack.data.entity.Prescription
import com.example.medtrack.data.entity.PrescriptionMedication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MedTrackApplication : Application() {
    val database: MedTrackDatabase by lazy {
        MedTrackDatabase.getDatabase(this)
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        seedInitialDataIfNeeded()
    }

    private fun seedInitialDataIfNeeded() {
        CoroutineScope(Dispatchers.IO).launch {
            val patientDao = database.patientDao()
            if (patientDao.getPatientOnce() == null) {
                val patient = Patient(
                    fullName = "Angelo Rabaya",
                    dateOfBirth = "1975-03-08",
                    gender = "Male",
                    bloodType = "B+",
                    allergies = "dust",
                    emergencyContact = "+63 917 123 4567"
                )
                val patientId = patientDao.insert(patient).toInt()

                val prescription = Prescription(
                    patientId = patientId,
                    doctorName = "Dr. Ma Michaela Isabel N. Luayon Feril",
                    datePrescribed = "2026-08-22",
                    title = "Blood Chem",
                    isActive = true
                )
                val rxId = database.prescriptionDao().insert(prescription).toInt()

                val meds = listOf(
                    PrescriptionMedication(
                        prescriptionId = rxId,
                        medicationName = "Atorvastatin",
                        dosage = "40",
                        frequency = "0-0-1 (Evening / Bedtime only)",
                        duration = "90",
                        instructions = "Take after evening meal"
                    ),
                    PrescriptionMedication(
                        prescriptionId = rxId,
                        medicationName = "Liver Prime HD",
                        dosage = "1 cap",
                        frequency = "1-0-0 (Morning only)",
                        duration = "15",
                        instructions = "Take after breakfast"
                    ),
                    PrescriptionMedication(
                        prescriptionId = rxId,
                        medicationName = "Febuxostat",
                        dosage = "40",
                        frequency = "0-0-1 (Evening / Bedtime only)",
                        duration = "30",
                        instructions = "Take with water at bedtime"
                    )
                )
                database.prescriptionDao().insertMedications(meds)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                "Medicine Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders to take your medicine"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val REMINDER_CHANNEL_ID = "medicine_reminders"
    }
}

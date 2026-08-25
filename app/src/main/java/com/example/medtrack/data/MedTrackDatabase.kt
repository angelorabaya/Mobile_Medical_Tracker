package com.example.medtrack.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.medtrack.data.dao.*
import com.example.medtrack.data.entity.*

@Database(
    entities = [
        Patient::class,
        LabTest::class,
        LabTestItem::class,
        Prescription::class,
        PrescriptionMedication::class,
        MedicineReminder::class,
        LabTestType::class,
        BmiRecord::class
    ],
    version = 7,
    exportSchema = false
)
abstract class MedTrackDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao
    abstract fun labTestDao(): LabTestDao
    abstract fun prescriptionDao(): PrescriptionDao
    abstract fun medicineReminderDao(): MedicineReminderDao
    abstract fun labTestTypeDao(): LabTestTypeDao
    abstract fun bmiRecordDao(): BmiRecordDao

    companion object {
        @Volatile
        private var INSTANCE: MedTrackDatabase? = null

        fun getDatabase(context: Context): MedTrackDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MedTrackDatabase::class.java,
                    "medtrack_database"
                )
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

package com.example.medtrack.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.medtrack.data.entity.LabTest
import com.example.medtrack.data.entity.LabTestItem
import com.example.medtrack.data.entity.MedicineReminder
import com.example.medtrack.data.entity.Patient
import com.example.medtrack.data.entity.Prescription
import com.example.medtrack.data.entity.PrescriptionMedication
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MedTrackDatabaseDaoTest {

    private lateinit var db: MedTrackDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, MedTrackDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun patientDao_insertAndRead() = runBlocking {
        db.patientDao().insert(
            Patient(fullName = "Test Patient", dateOfBirth = "1990-01-01", gender = "Male")
        )
        val patient = db.patientDao().getPatientOnce()
        assertNotNull(patient)
        assertEquals("Test Patient", patient?.fullName)
        assertEquals(1, db.patientDao().getPatientCount())
    }

    @Test
    fun labTestDao_insertWithItems_andCascadeDelete() = runBlocking {
        val patientId = db.patientDao()
            .insert(Patient(fullName = "P", dateOfBirth = "1990-01-01", gender = "Male"))
            .toInt()

        val testId = db.labTestDao().insertLabTestWithItems(
            LabTest(patientId = patientId, testDate = "2026-01-01", title = "CBC"),
            listOf(LabTestItem(labTestId = 0, testName = "WBC"))
        )

        val withItems = db.labTestDao().getLabTestWithItemsByIdOnce(testId.toInt())
        assertEquals(1, withItems?.items?.size)

        db.labTestDao().delete(withItems!!.labTest)
        assertNull(db.labTestDao().getLabTestByIdOnce(testId.toInt()))
    }

    @Test
    fun prescriptionDao_cascadeToMedicationsAndReminders() = runBlocking {
        val patientId = db.patientDao()
            .insert(Patient(fullName = "P", dateOfBirth = "1990-01-01", gender = "Male"))
            .toInt()

        val rxId = db.prescriptionDao().insertPrescriptionWithMedications(
            Prescription(patientId = patientId, datePrescribed = "2026-01-01"),
            listOf(PrescriptionMedication(prescriptionId = 0, medicationName = "Metformin"))
        ).toInt()

        db.medicineReminderDao().insert(
            MedicineReminder(prescriptionId = rxId, reminderTime = "08:00")
        )

        db.prescriptionDao().delete(db.prescriptionDao().getPrescriptionByIdOnce(rxId)!!)

        assertNull(db.prescriptionDao().getPrescriptionByIdOnce(rxId))
        assertTrue(db.medicineReminderDao().getAllRemindersOnce().isEmpty())
    }
}

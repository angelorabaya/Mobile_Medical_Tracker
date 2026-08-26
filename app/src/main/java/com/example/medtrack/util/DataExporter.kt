package com.example.medtrack.util

import com.example.medtrack.data.AppContainer
import com.example.medtrack.data.entity.BmiRecord
import com.example.medtrack.data.entity.LabTest
import com.example.medtrack.data.entity.LabTestItem
import com.example.medtrack.data.entity.LabTestType
import com.example.medtrack.data.entity.MedicineReminder
import com.example.medtrack.data.entity.Patient
import com.example.medtrack.data.entity.PendingLabOrder
import com.example.medtrack.data.entity.Prescription
import com.example.medtrack.data.entity.PrescriptionMedication
import com.example.medtrack.data.entity.PrescriptionWithMedications
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Exports the entire local health record to (and restores it from) a JSON
 * file. Attachments (photos) are not embedded; their paths are exported but the
 * binary files are not (re-linking images after import is a future feature).
 *
 * Import is a full restore: it replaces all current data after a confirmation.
 */
class DataExporter(private val container: AppContainer) {

    // ------------------------------------------------------------------
    // Export
    // ------------------------------------------------------------------

    suspend fun exportJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("format", "vitalsiq")
        root.put("version", 1)

        container.patientRepository.getPatientOnce()?.let { p ->
            root.put("patient", patientJson(p))
        }

        val labTests = JSONArray()
        container.labTestRepository.getAllLabTestsWithItemsOnce().forEach { t ->
            labTests.put(labTestJson(t))
        }
        root.put("labTests", labTests)

        val prescriptions = JSONArray()
        container.prescriptionRepository.getAllPrescriptionsWithMedicationsOnce().forEach { p ->
            prescriptions.put(prescriptionJson(p))
        }
        root.put("prescriptions", prescriptions)

        val bmiRecords = JSONArray()
        container.bmiRepository.getAllBmiRecordsOnce().forEach { b ->
            bmiRecords.put(bmiJson(b))
        }
        root.put("bmiRecords", bmiRecords)

        val orders = JSONArray()
        container.pendingLabOrderRepository.getAllOrdersOnce().forEach { o ->
            orders.put(pendingOrderJson(o))
        }
        root.put("pendingLabOrders", orders)

        val types = JSONArray()
        container.labTestTypeRepository.getAllTestTypesOnce().forEach { t ->
            types.put(testTypeJson(t))
        }
        root.put("labTestTypes", types)

        root.toString(2)
    }

    private fun patientJson(p: Patient) = JSONObject().apply {
        put("fullName", p.fullName)
        put("dateOfBirth", p.dateOfBirth)
        put("gender", p.gender)
        put("bloodType", p.bloodType)
        put("allergies", p.allergies)
        put("emergencyContact", p.emergencyContact)
        put("photoUri", p.photoUri)
        put("createdAt", p.createdAt)
    }

    private fun labTestJson(t: com.example.medtrack.data.entity.LabTestWithItems) = JSONObject().apply {
        put("title", t.labTest.title)
        put("testDate", t.labTest.testDate)
        put("labName", t.labTest.labName)
        put("doctorName", t.labTest.doctorName)
        put("notes", t.labTest.notes)
        put("imageUri", t.labTest.imageUri)
        put("createdAt", t.labTest.createdAt)
        val items = JSONArray()
        t.items.forEach { it2 ->
            items.put(JSONObject().apply {
                put("testName", it2.testName)
                put("category", it2.category)
                put("results", it2.results)
                put("normalRange", it2.normalRange)
                put("notes", it2.notes)
            })
        }
        put("items", items)
    }

    private suspend fun prescriptionJson(p: PrescriptionWithMedications) = JSONObject().apply {
        put("title", p.prescription.title)
        put("doctorName", p.prescription.doctorName)
        put("datePrescribed", p.prescription.datePrescribed)
        put("notes", p.prescription.notes)
        put("imageUri", p.prescription.imageUri)
        put("isActive", p.prescription.isActive)
        put("createdAt", p.prescription.createdAt)

        val meds = JSONArray()
        p.medications.forEach { m ->
            meds.put(JSONObject().apply {
                put("medicationName", m.medicationName)
                put("dosage", m.dosage)
                put("frequency", m.frequency)
                put("duration", m.duration)
                put("instructions", m.instructions)
            })
        }
        put("medications", meds)

        // Nest reminders so prescription->reminder mapping survives a re-import.
        val reminders = JSONArray()
        container.reminderRepository.getRemindersByPrescription(p.prescription.id)
            .first()
            .forEach { r ->
                reminders.put(reminderJson(r))
            }
        put("reminders", reminders)
    }

    private fun reminderJson(r: MedicineReminder) = JSONObject().apply {
        put("reminderTime", r.reminderTime)
        put("isEnabled", r.isEnabled)
        put("label", r.label)
    }

    private fun bmiJson(b: BmiRecord) = JSONObject().apply {
        put("weightKg", b.weightKg)
        put("heightCm", b.heightCm)
        put("bmi", b.bmi)
        put("category", b.category)
        put("calculatedAt", b.calculatedAt)
    }

    private fun pendingOrderJson(o: PendingLabOrder) = JSONObject().apply {
        put("testName", o.testName)
        put("scheduledDate", o.scheduledDate)
        put("scheduledTime", o.scheduledTime)
        put("facilityName", o.facilityName)
        put("fastingInstructions", o.fastingInstructions)
        put("notes", o.notes)
        put("isReminderEnabled", o.isReminderEnabled)
        put("isCompleted", o.isCompleted)
        put("createdAt", o.createdAt)
    }

    private fun testTypeJson(t: LabTestType) = JSONObject().apply {
        put("name", t.name)
        put("defaultCategory", t.defaultCategory)
        put("defaultNormalRange", t.defaultNormalRange)
        put("isDefault", t.isDefault)
        put("createdAt", t.createdAt)
    }

    // ------------------------------------------------------------------
    // Import
    // ------------------------------------------------------------------

    suspend fun importJson(json: String) = withContext(Dispatchers.IO) {
        val root = JSONObject(json)

        // Full restore: clear current data first (patient FK cascade removes
        // dependent lab tests, prescriptions, medications, reminders, BMI and
        // pending orders; lab test types are independent).
        container.patientRepository.deleteAll()
        container.labTestTypeRepository.deleteAll()

        // Patient
        var patientId = 0
        if (root.has("patient") && !root.isNull("patient")) {
            val p = root.getJSONObject("patient")
            val id = container.patientRepository.insert(
                Patient(
                    fullName = p.optString("fullName"),
                    dateOfBirth = p.optString("dateOfBirth"),
                    gender = p.optString("gender"),
                    bloodType = p.optString("bloodType"),
                    allergies = p.optString("allergies"),
                    emergencyContact = p.optString("emergencyContact"),
                    photoUri = p.optString("photoUri"),
                    createdAt = p.optLong("createdAt", System.currentTimeMillis())
                )
            )
            patientId = id.toInt()
        }

        // Lab tests (with items)
        val labTests = root.optJSONArray("labTests") ?: JSONArray()
        for (i in 0 until labTests.length()) {
            val t = labTests.getJSONObject(i)
            val test = LabTest(
                patientId = patientId,
                title = t.optString("title"),
                testDate = t.optString("testDate"),
                labName = t.optString("labName"),
                doctorName = t.optString("doctorName"),
                notes = t.optString("notes"),
                imageUri = t.optString("imageUri").ifBlank { null },
                createdAt = t.optLong("createdAt", System.currentTimeMillis())
            )
            val itemsJson = t.optJSONArray("items") ?: JSONArray()
            val items = mutableListOf<LabTestItem>()
            for (j in 0 until itemsJson.length()) {
                val it2 = itemsJson.getJSONObject(j)
                items.add(
                    LabTestItem(
                        labTestId = 0,
                        testName = it2.optString("testName"),
                        category = it2.optString("category", "Blood Work"),
                        results = it2.optString("results"),
                        normalRange = it2.optString("normalRange"),
                        notes = it2.optString("notes")
                    )
                )
            }
            container.labTestRepository.insertLabTestWithItems(test, items)
        }

        // Prescriptions (with medications and nested reminders)
        val prescriptions = root.optJSONArray("prescriptions") ?: JSONArray()
        for (i in 0 until prescriptions.length()) {
            val p = prescriptions.getJSONObject(i)
            val prescription = Prescription(
                patientId = patientId,
                title = p.optString("title"),
                doctorName = p.optString("doctorName"),
                datePrescribed = p.optString("datePrescribed"),
                notes = p.optString("notes"),
                imageUri = p.optString("imageUri").ifBlank { null },
                isActive = p.optBoolean("isActive", true),
                createdAt = p.optLong("createdAt", System.currentTimeMillis())
            )
            val medsJson = p.optJSONArray("medications") ?: JSONArray()
            val meds = mutableListOf<PrescriptionMedication>()
            for (j in 0 until medsJson.length()) {
                val m = medsJson.getJSONObject(j)
                meds.add(
                    PrescriptionMedication(
                        prescriptionId = 0,
                        medicationName = m.optString("medicationName"),
                        dosage = m.optString("dosage"),
                        frequency = m.optString("frequency"),
                        duration = m.optString("duration"),
                        instructions = m.optString("instructions")
                    )
                )
            }
            val newPrescriptionId = container.prescriptionRepository
                .insertPrescriptionWithMedications(prescription, meds)
                .toInt()

            val remindersJson = p.optJSONArray("reminders") ?: JSONArray()
            for (j in 0 until remindersJson.length()) {
                val r = remindersJson.getJSONObject(j)
                container.reminderRepository.insert(
                    MedicineReminder(
                        prescriptionId = newPrescriptionId,
                        reminderTime = r.optString("reminderTime"),
                        isEnabled = r.optBoolean("isEnabled", true),
                        label = r.optString("label")
                    )
                )
            }
        }

        // BMI records
        val bmiRecords = root.optJSONArray("bmiRecords") ?: JSONArray()
        for (i in 0 until bmiRecords.length()) {
            val b = bmiRecords.getJSONObject(i)
            container.bmiRepository.insert(
                BmiRecord(
                    patientId = patientId,
                    weightKg = b.optDouble("weightKg", 0.0),
                    heightCm = b.optDouble("heightCm", 0.0),
                    bmi = b.optDouble("bmi", 0.0),
                    category = b.optString("category"),
                    calculatedAt = b.optLong("calculatedAt", System.currentTimeMillis())
                )
            )
        }

        // Pending lab orders
        val orders = root.optJSONArray("pendingLabOrders") ?: JSONArray()
        for (i in 0 until orders.length()) {
            val o = orders.getJSONObject(i)
            container.pendingLabOrderRepository.insert(
                PendingLabOrder(
                    patientId = patientId,
                    testName = o.optString("testName"),
                    scheduledDate = o.optString("scheduledDate"),
                    scheduledTime = o.optString("scheduledTime", "07:30"),
                    facilityName = o.optString("facilityName"),
                    fastingInstructions = o.optString("fastingInstructions"),
                    notes = o.optString("notes"),
                    isReminderEnabled = o.optBoolean("isReminderEnabled", true),
                    isCompleted = o.optBoolean("isCompleted", false),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        // Lab test types
        val types = root.optJSONArray("labTestTypes") ?: JSONArray()
        if (types.length() > 0) {
            val typeList = mutableListOf<LabTestType>()
            for (i in 0 until types.length()) {
                val t = types.getJSONObject(i)
                typeList.add(
                    LabTestType(
                        name = t.optString("name"),
                        defaultCategory = t.optString("defaultCategory", "Blood Work"),
                        defaultNormalRange = t.optString("defaultNormalRange"),
                        isDefault = t.optBoolean("isDefault", false),
                        createdAt = t.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
            container.labTestTypeRepository.insertAll(typeList)
        }
    }
}

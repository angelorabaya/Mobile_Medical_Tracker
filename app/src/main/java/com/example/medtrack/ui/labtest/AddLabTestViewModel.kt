package com.example.medtrack.ui.labtest

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medtrack.MedTrackApplication
import com.example.medtrack.data.entity.LabTest
import com.example.medtrack.data.entity.LabTestItem
import com.example.medtrack.data.entity.LabTestType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class LabTestItemInput(
    var testName: String = "",
    var category: String = "Blood Work",
    var results: String = "",
    var normalRange: String = "",
    var notes: String = ""
)

class AddLabTestViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as MedTrackApplication).database

    val categoryOptions = listOf(
        "Blood Work",
        "Imaging",
        "Urinalysis",
        "Biopsy / Pathology",
        "Cardiac",
        "Other"
    )

    val testTypes: StateFlow<List<LabTestType>> = db.labTestTypeDao()
        .getAllTestTypes()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    var title by mutableStateOf("")
    var testDate by mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
    var labName by mutableStateOf("")
    var doctorName by mutableStateOf("")
    var notes by mutableStateOf("")
    var imageUri by mutableStateOf<String?>(null)
    var errorMessage by mutableStateOf<String?>(null)

    val items = mutableStateListOf(
        LabTestItemInput()
    )

    init {
        seedStandardTestTypesIfEmpty()
    }

    private fun seedStandardTestTypesIfEmpty() {
        viewModelScope.launch {
            val defaults = listOf(
                LabTestType(name = "Complete Blood Count (CBC)", defaultCategory = "Blood Work", defaultNormalRange = "WBC: 4.5-11.0, RBC: 4.3-5.9, Hgb: 13.5-17.5", isDefault = true),
                LabTestType(name = "Fasting Blood Sugar (FBS)", defaultCategory = "Blood Work", defaultNormalRange = "70 - 99 mg/dL", isDefault = true),
                LabTestType(name = "HbA1c (Glycated Hemoglobin)", defaultCategory = "Blood Work", defaultNormalRange = "< 5.7%", isDefault = true),
                LabTestType(name = "Lipid Profile - Total Cholesterol", defaultCategory = "Blood Work", defaultNormalRange = "< 200 mg/dL", isDefault = true),
                LabTestType(name = "Lipid Profile - Triglycerides", defaultCategory = "Blood Work", defaultNormalRange = "< 150 mg/dL", isDefault = true),
                LabTestType(name = "Lipid Profile - HDL Cholesterol (Good)", defaultCategory = "Blood Work", defaultNormalRange = "> 40 mg/dL (Male), > 50 mg/dL (Female)", isDefault = true),
                LabTestType(name = "Lipid Profile - LDL Cholesterol (Bad)", defaultCategory = "Blood Work", defaultNormalRange = "< 100 mg/dL", isDefault = true),
                LabTestType(name = "Lipid Profile - VLDL Cholesterol", defaultCategory = "Blood Work", defaultNormalRange = "2 - 30 mg/dL", isDefault = true),
                LabTestType(name = "Liver Function Test (SGPT / ALT, SGOT / AST)", defaultCategory = "Blood Work", defaultNormalRange = "ALT: 7-56 U/L, AST: 10-40 U/L", isDefault = true),
                LabTestType(name = "Serum Creatinine & eGFR (Kidney)", defaultCategory = "Blood Work", defaultNormalRange = "0.7 - 1.3 mg/dL, eGFR > 90", isDefault = true),
                LabTestType(name = "Blood Uric Acid (BUA)", defaultCategory = "Blood Work", defaultNormalRange = "3.5 - 7.2 mg/dL", isDefault = true),
                LabTestType(name = "Routine Urinalysis", defaultCategory = "Urinalysis", defaultNormalRange = "Color: Straw/Yellow, Protein: Neg, Sugar: Neg", isDefault = true),
                LabTestType(name = "Chest X-Ray (PA View)", defaultCategory = "Imaging", defaultNormalRange = "Normal heart size and lung fields", isDefault = true),
                LabTestType(name = "12-Lead Electrocardiogram (ECG)", defaultCategory = "Cardiac", defaultNormalRange = "Normal Sinus Rhythm", isDefault = true),
                LabTestType(name = "Thyroid Function (TSH, FT3, FT4)", defaultCategory = "Blood Work", defaultNormalRange = "TSH: 0.4-4.0 uIU/mL", isDefault = true),
                LabTestType(name = "Serum Electrolytes (Na, K, Cl)", defaultCategory = "Blood Work", defaultNormalRange = "Na: 135-145, K: 3.5-5.0, Cl: 96-106 mEq/L", isDefault = true)
            )

            val existing = db.labTestTypeDao().getAllTestTypesOnce()
            if (existing.isEmpty()) {
                db.labTestTypeDao().insertAll(defaults)
            } else {
                // Delete legacy combined Lipid Profile entry if present
                val oldCombined = existing.filter {
                    it.name.contains("Lipid Profile (", ignoreCase = true) || it.name.equals("Lipid Profile", ignoreCase = true)
                }
                for (old in oldCombined) {
                    db.labTestTypeDao().delete(old)
                }
                // Insert any missing itemized lipid profile options
                val lipidItems = defaults.filter { it.name.startsWith("Lipid Profile -") }
                val toInsert = lipidItems.filter { item ->
                    existing.none { it.name.equals(item.name, ignoreCase = true) }
                }
                if (toInsert.isNotEmpty()) {
                    db.labTestTypeDao().insertAll(toInsert)
                }
            }
        }
    }

    fun addItem() {
        items.add(LabTestItemInput())
    }

    fun removeItem(index: Int) {
        if (items.size > 1 && index in items.indices) {
            items.removeAt(index)
        }
    }

    fun updateItemTestName(index: Int, name: String) {
        if (index in items.indices) {
            items[index] = items[index].copy(testName = name)
        }
    }

    fun updateItemCategory(index: Int, category: String) {
        if (index in items.indices) {
            items[index] = items[index].copy(category = category)
        }
    }

    fun updateItemResults(index: Int, results: String) {
        if (index in items.indices) {
            items[index] = items[index].copy(results = results)
        }
    }

    fun updateItemNormalRange(index: Int, range: String) {
        if (index in items.indices) {
            items[index] = items[index].copy(normalRange = range)
        }
    }

    fun updateItemNotes(index: Int, notes: String) {
        if (index in items.indices) {
            items[index] = items[index].copy(notes = notes)
        }
    }

    fun selectTestTypeForItem(index: Int, type: LabTestType) {
        if (index in items.indices) {
            items[index] = items[index].copy(
                testName = type.name,
                category = type.defaultCategory,
                normalRange = if (type.defaultNormalRange.isNotBlank()) type.defaultNormalRange else items[index].normalRange
            )
        }
    }

    fun addTestType(name: String, defaultCategory: String, defaultRange: String, onComplete: ((LabTestType) -> Unit)? = null) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val newType = LabTestType(
                name = name.trim(),
                defaultCategory = defaultCategory,
                defaultNormalRange = defaultRange.trim(),
                isDefault = false
            )
            val id = db.labTestTypeDao().insert(newType)
            val createdType = newType.copy(id = id.toInt())
            onComplete?.invoke(createdType)
        }
    }

    fun updateTestType(type: LabTestType, onComplete: (() -> Unit)? = null) {
        if (type.name.isBlank()) return
        viewModelScope.launch {
            db.labTestTypeDao().update(type.copy(name = type.name.trim(), defaultNormalRange = type.defaultNormalRange.trim()))
            onComplete?.invoke()
        }
    }

    fun deleteTestType(type: LabTestType, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            db.labTestTypeDao().delete(type)
            onComplete?.invoke()
        }
    }

    fun resetState() {
        title = ""
        testDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        labName = ""
        doctorName = ""
        notes = ""
        imageUri = null
        errorMessage = null
        items.clear()
        items.add(LabTestItemInput())
    }

    fun saveLabTest(onComplete: () -> Unit) {
        val validItems = items.filter { it.testName.isNotBlank() }
        if (validItems.isEmpty()) {
            errorMessage = "Please enter at least one test description/name"
            return
        }
        if (testDate.isBlank()) {
            errorMessage = "Test date is required"
            return
        }
        errorMessage = null
        viewModelScope.launch {
            val patient = db.patientDao().getPatientOnce() ?: return@launch
            val labTest = LabTest(
                patientId = patient.id,
                title = title.trim(),
                testDate = testDate.trim(),
                labName = labName.trim(),
                doctorName = doctorName.trim(),
                notes = notes.trim(),
                imageUri = imageUri
            )

            val labTestItems = validItems.map { input ->
                LabTestItem(
                    labTestId = 0,
                    testName = input.testName.trim(),
                    category = input.category,
                    results = input.results.trim(),
                    normalRange = input.normalRange.trim(),
                    notes = input.notes.trim()
                )
            }

            db.labTestDao().insertLabTestWithItems(labTest, labTestItems)
            withContext(Dispatchers.Main) {
                resetState()
                onComplete()
            }
        }
    }
}

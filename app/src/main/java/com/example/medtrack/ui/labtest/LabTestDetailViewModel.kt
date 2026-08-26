package com.example.medtrack.ui.labtest

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.medtrack.MedTrackApplication
import com.example.medtrack.data.entity.LabTestItem
import com.example.medtrack.data.entity.LabTestType
import com.example.medtrack.data.entity.LabTestWithItems
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LabTestDetailViewModel(
    application: Application,
    private val testId: Int
) : AndroidViewModel(application) {
    private val container = (application as MedTrackApplication).container

    val categoryOptions = listOf(
        "Blood Work",
        "Imaging",
        "Urinalysis",
        "Biopsy / Pathology",
        "Cardiac",
        "Other"
    )

    val labTestWithItems: StateFlow<LabTestWithItems?> = container.labTestRepository
        .getLabTestWithItemsById(testId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val testTypes: StateFlow<List<LabTestType>> = container.labTestTypeRepository
        .getAllTestTypes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

            val existing = container.labTestTypeRepository.getAllTestTypesOnce()
            if (existing.isEmpty()) {
                container.labTestTypeRepository.insertAll(defaults)
            } else {
                val oldCombined = existing.filter {
                    it.name.contains("Lipid Profile (", ignoreCase = true) || it.name.equals("Lipid Profile", ignoreCase = true)
                }
                for (old in oldCombined) {
                    container.labTestTypeRepository.delete(old)
                }
                val lipidItems = defaults.filter { it.name.startsWith("Lipid Profile -") }
                val toInsert = lipidItems.filter { item ->
                    existing.none { it.name.equals(item.name, ignoreCase = true) }
                }
                if (toInsert.isNotEmpty()) {
                    container.labTestTypeRepository.insertAll(toInsert)
                }
            }
        }
    }

    fun updateLabTestInfo(
        title: String,
        testDate: String,
        labName: String,
        doctorName: String,
        notes: String
    ) {
        viewModelScope.launch {
            val current = labTestWithItems.value?.labTest ?: return@launch
            container.labTestRepository.update(
                current.copy(
                    title = title,
                    testDate = testDate,
                    labName = labName,
                    doctorName = doctorName,
                    notes = notes
                )
            )
        }
    }

    fun updateLabTestImage(imageUri: String?) {
        viewModelScope.launch {
            val current = labTestWithItems.value?.labTest ?: return@launch
            container.labTestRepository.update(current.copy(imageUri = imageUri))
        }
    }

    fun addLabTestItem(
        testName: String,
        category: String,
        results: String,
        normalRange: String,
        notes: String
    ) {
        viewModelScope.launch {
            val item = LabTestItem(
                labTestId = testId,
                testName = testName.trim(),
                category = category,
                results = results.trim(),
                normalRange = normalRange.trim(),
                notes = notes.trim()
            )
            container.labTestRepository.insertItem(item)
        }
    }

    fun updateLabTestItem(item: LabTestItem) {
        viewModelScope.launch {
            container.labTestRepository.updateItem(item)
        }
    }

    fun deleteLabTestItem(item: LabTestItem) {
        viewModelScope.launch {
            container.labTestRepository.deleteItem(item)
        }
    }

    fun deleteLabTest(onComplete: () -> Unit) {
        viewModelScope.launch {
            val current = labTestWithItems.value?.labTest ?: return@launch
            container.labTestRepository.delete(current)
            onComplete()
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
            val id = container.labTestTypeRepository.insert(newType)
            val createdType = newType.copy(id = id.toInt())
            onComplete?.invoke(createdType)
        }
    }

    fun updateTestType(type: LabTestType, onComplete: (() -> Unit)? = null) {
        if (type.name.isBlank()) return
        viewModelScope.launch {
            container.labTestTypeRepository.update(type.copy(name = type.name.trim(), defaultNormalRange = type.defaultNormalRange.trim()))
            onComplete?.invoke()
        }
    }

    fun deleteTestType(type: LabTestType, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            container.labTestTypeRepository.delete(type)
            onComplete?.invoke()
        }
    }

    companion object {
        fun factory(testId: Int): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MedTrackApplication
                return LabTestDetailViewModel(app, testId) as T
            }
        }
    }
}

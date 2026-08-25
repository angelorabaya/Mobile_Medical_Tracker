package com.example.medtrack.ui.prescription

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medtrack.MedTrackApplication
import com.example.medtrack.data.entity.Prescription
import com.example.medtrack.data.entity.PrescriptionWithMedications
import com.example.medtrack.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PrescriptionListViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as MedTrackApplication).database

    val showActiveOnly = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val prescriptionsWithMedications: StateFlow<List<PrescriptionWithMedications>> = db.patientDao().getPatient()
        .flatMapLatest { patient ->
            if (patient != null) {
                showActiveOnly.flatMapLatest { activeOnly ->
                    if (activeOnly) db.prescriptionDao().getActivePrescriptionsWithMedications(patient.id)
                    else db.prescriptionDao().getPrescriptionsWithMedications(patient.id)
                }
            } else flowOf(emptyList())
        }
        .map { list ->
            list.sortedWith { a, b ->
                val dateComparison = DateUtils.newestFirstComparator.compare(a.prescription.datePrescribed, b.prescription.datePrescribed)
                if (dateComparison != 0) {
                    dateComparison
                } else {
                    val createdComparison = b.prescription.createdAt.compareTo(a.prescription.createdAt)
                    if (createdComparison != 0) createdComparison else b.prescription.id.compareTo(a.prescription.id)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleFilter() {
        showActiveOnly.value = !showActiveOnly.value
    }

    fun togglePrescriptionStatus(prescription: Prescription) {
        viewModelScope.launch {
            db.prescriptionDao().update(prescription.copy(isActive = !prescription.isActive))
        }
    }

    fun deletePrescription(prescription: Prescription) {
        viewModelScope.launch {
            db.prescriptionDao().delete(prescription)
        }
    }
}

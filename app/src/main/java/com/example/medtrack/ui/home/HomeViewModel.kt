package com.example.medtrack.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medtrack.MedTrackApplication
import com.example.medtrack.data.entity.Patient
import com.example.medtrack.util.LabComparisonHelper
import com.example.medtrack.util.LabTestComparison
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val db = (application as MedTrackApplication).database

    val patient: StateFlow<Patient?> = db.patientDao().getPatient()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val labTestCount: StateFlow<Int> = patient
        .flatMapLatest { p ->
            if (p != null) db.labTestDao().getLabTestCount(p.id) else flowOf(0)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val activePrescriptionCount: StateFlow<Int> = patient
        .flatMapLatest { p ->
            if (p != null) db.prescriptionDao().getActivePrescriptionCount(p.id) else flowOf(0)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val comparativeLabPanels: StateFlow<List<LabTestComparison>> = patient
        .flatMapLatest { p ->
            if (p != null) db.labTestDao().getLabTestsWithItems(p.id) else flowOf(emptyList())
        }
        .map { testsWithItems ->
            LabComparisonHelper.generateComparativePanels(testsWithItems)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

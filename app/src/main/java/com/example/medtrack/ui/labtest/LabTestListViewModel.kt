package com.example.medtrack.ui.labtest

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medtrack.MedTrackApplication
import com.example.medtrack.data.entity.LabTest
import com.example.medtrack.data.entity.LabTestWithItems
import com.example.medtrack.util.DateUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LabTestListViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as MedTrackApplication).container

    @OptIn(ExperimentalCoroutinesApi::class)
    val labTestsWithItems: StateFlow<List<LabTestWithItems>> = container.patientRepository.getPatient()
        .flatMapLatest { patient ->
            if (patient != null) container.labTestRepository.getLabTestsWithItems(patient.id)
            else flowOf(emptyList())
        }
        .map { list ->
            list.sortedWith { a, b ->
                val dateComparison = DateUtils.newestFirstComparator.compare(a.labTest.testDate, b.labTest.testDate)
                if (dateComparison != 0) {
                    dateComparison
                } else {
                    val createdComparison = b.labTest.createdAt.compareTo(a.labTest.createdAt)
                    if (createdComparison != 0) createdComparison else b.labTest.id.compareTo(a.labTest.id)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteLabTest(labTest: LabTest) {
        viewModelScope.launch {
            container.labTestRepository.delete(labTest)
        }
    }
}

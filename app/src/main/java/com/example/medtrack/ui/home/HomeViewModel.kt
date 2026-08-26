package com.example.medtrack.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medtrack.MedTrackApplication
import com.example.medtrack.data.entity.BmiRecord
import com.example.medtrack.data.entity.Patient
import com.example.medtrack.data.entity.PendingLabOrder
import com.example.medtrack.notification.ReminderScheduler
import com.example.medtrack.util.LabComparisonHelper
import com.example.medtrack.util.LabTestComparison
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as MedTrackApplication).container

    val patient: StateFlow<Patient?> = container.patientRepository.getPatient()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val labTestCount: StateFlow<Int> = patient
        .flatMapLatest { p ->
            if (p != null) container.labTestRepository.getLabTestCount(p.id) else flowOf(0)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val activePrescriptionCount: StateFlow<Int> = patient
        .flatMapLatest { p ->
            if (p != null) container.prescriptionRepository.getActivePrescriptionCount(p.id) else flowOf(0)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val comparativeLabPanels: StateFlow<List<LabTestComparison>> = patient
        .flatMapLatest { p ->
            if (p != null) container.labTestRepository.getLabTestsWithItems(p.id) else flowOf(emptyList())
        }
        .map { testsWithItems ->
            LabComparisonHelper.generateComparativePanels(testsWithItems)
        }
        // Comparison generation is CPU/regex heavy; keep it off the main thread.
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val latestBmiRecord: StateFlow<BmiRecord?> = patient
        .flatMapLatest { p ->
            if (p != null) container.bmiRepository.getLatestBmiRecordForPatient(p.id) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val pendingLabOrders: StateFlow<List<PendingLabOrder>> = patient
        .flatMapLatest { p ->
            if (p != null) container.pendingLabOrderRepository.getPendingOrdersForPatient(p.id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableLabTestTypes: StateFlow<List<String>> = container.labTestTypeRepository.getAllTestTypes()
        .map { list -> list.map { it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveBmiRecord(weightKg: Double, heightCm: Double, bmi: Double, category: String) {
        val currentPatientId = patient.value?.id ?: 0
        viewModelScope.launch {
            container.bmiRepository.insert(
                BmiRecord(
                    patientId = currentPatientId,
                    weightKg = weightKg,
                    heightCm = heightCm,
                    bmi = bmi,
                    category = category,
                    calculatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun savePendingLabOrder(
        testName: String,
        scheduledDate: String,
        scheduledTime: String,
        facilityName: String,
        fastingInstructions: String,
        notes: String,
        isReminderEnabled: Boolean,
        imageUri: String? = null
    ) {
        val currentPatientId = patient.value?.id ?: return
        viewModelScope.launch {
            val order = PendingLabOrder(
                patientId = currentPatientId,
                testName = testName.trim(),
                scheduledDate = scheduledDate.trim(),
                scheduledTime = scheduledTime.trim(),
                facilityName = facilityName.trim(),
                fastingInstructions = fastingInstructions.trim(),
                notes = notes.trim(),
                imageUri = imageUri,
                isReminderEnabled = isReminderEnabled
            )
            val id = container.pendingLabOrderRepository.insert(order).toInt()
            val insertedOrder = order.copy(id = id)
            if (isReminderEnabled) {
                ReminderScheduler.scheduleLabOrderReminder(getApplication(), insertedOrder)
            }
        }
    }

    fun updatePendingLabOrder(order: PendingLabOrder) {
        viewModelScope.launch {
            container.pendingLabOrderRepository.update(order)
            ReminderScheduler.cancelLabOrderReminder(getApplication(), order.id)
            if (order.isReminderEnabled && !order.isCompleted) {
                ReminderScheduler.scheduleLabOrderReminder(getApplication(), order)
            }
        }
    }

    fun markLabOrderCompleted(order: PendingLabOrder) {
        viewModelScope.launch {
            container.pendingLabOrderRepository.markAsCompleted(order.id)
            ReminderScheduler.cancelLabOrderReminder(getApplication(), order.id)
        }
    }

    fun deletePendingLabOrder(order: PendingLabOrder) {
        viewModelScope.launch {
            container.pendingLabOrderRepository.delete(order)
            ReminderScheduler.cancelLabOrderReminder(getApplication(), order.id)
        }
    }
}

package com.example.medtrack

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Home : NavKey
@Serializable data object Registration : NavKey
@Serializable data object ProfileEdit : NavKey
@Serializable data object LabTestList : NavKey
@Serializable data object AddLabTest : NavKey
@Serializable data class LabTestDetail(val testId: Int) : NavKey
@Serializable data object PrescriptionList : NavKey
@Serializable data object AddPrescription : NavKey
@Serializable data class PrescriptionDetail(val prescriptionId: Int) : NavKey
@Serializable data object ReminderList : NavKey

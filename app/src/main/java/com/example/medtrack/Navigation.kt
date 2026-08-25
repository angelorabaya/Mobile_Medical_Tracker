package com.example.medtrack

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.medtrack.ui.home.HomeScreen
import com.example.medtrack.ui.registration.RegistrationScreen
import com.example.medtrack.ui.profile.ProfileEditScreen
import com.example.medtrack.ui.labtest.LabTestListScreen
import com.example.medtrack.ui.labtest.AddLabTestScreen
import com.example.medtrack.ui.labtest.LabTestDetailScreen
import com.example.medtrack.ui.prescription.PrescriptionListScreen
import com.example.medtrack.ui.prescription.AddPrescriptionScreen
import com.example.medtrack.ui.prescription.PrescriptionDetailScreen
import com.example.medtrack.ui.reminder.ReminderListScreen

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(Home)
    val defaultModifier = Modifier.safeDrawingPadding()

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Home> {
                HomeScreen(
                    onNavigateToRegistration = { backStack.add(Registration) },
                    onNavigateToProfile = { backStack.add(ProfileEdit) },
                    onNavigateToLabTests = { backStack.add(LabTestList) },
                    onNavigateToPrescriptions = { backStack.add(PrescriptionList) },
                    onNavigateToReminders = { backStack.add(ReminderList) },
                    modifier = defaultModifier
                )
            }
            entry<Registration> {
                RegistrationScreen(
                    onRegistrationComplete = { backStack.removeLastOrNull() },
                    modifier = defaultModifier
                )
            }
            entry<ProfileEdit> {
                ProfileEditScreen(
                    onBack = { backStack.removeLastOrNull() },
                    modifier = defaultModifier
                )
            }
            entry<LabTestList> {
                LabTestListScreen(
                    onBack = { backStack.removeLastOrNull() },
                    onNavigateToAddLabTest = { backStack.add(AddLabTest) },
                    onNavigateToLabTestDetail = { testId -> backStack.add(LabTestDetail(testId)) },
                    modifier = defaultModifier
                )
            }
            entry<AddLabTest> {
                AddLabTestScreen(
                    onBack = { backStack.removeLastOrNull() },
                    modifier = defaultModifier
                )
            }
            entry<LabTestDetail> { key ->
                LabTestDetailScreen(
                    testId = key.testId,
                    onBack = { backStack.removeLastOrNull() },
                    modifier = defaultModifier
                )
            }
            entry<PrescriptionList> {
                PrescriptionListScreen(
                    onBack = { backStack.removeLastOrNull() },
                    onNavigateToAddPrescription = { backStack.add(AddPrescription) },
                    onNavigateToPrescriptionDetail = { id -> backStack.add(PrescriptionDetail(id)) },
                    modifier = defaultModifier
                )
            }
            entry<AddPrescription> {
                AddPrescriptionScreen(
                    onBack = { backStack.removeLastOrNull() },
                    modifier = defaultModifier
                )
            }
            entry<PrescriptionDetail> { key ->
                PrescriptionDetailScreen(
                    prescriptionId = key.prescriptionId,
                    onBack = { backStack.removeLastOrNull() },
                    modifier = defaultModifier
                )
            }
            entry<ReminderList> {
                ReminderListScreen(
                    onBack = { backStack.removeLastOrNull() },
                    modifier = defaultModifier
                )
            }
        }
    )
}

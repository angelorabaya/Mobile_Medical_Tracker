package com.example.medtrack.ui.prescription

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medtrack.ui.components.DosageScheduleInput
import com.example.medtrack.ui.components.ImageAttachmentPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPrescriptionScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddPrescriptionViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Doctor's Prescription", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // General Info Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Visit / Prescription Info",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = viewModel.title,
                        onValueChange = { viewModel.title = it },
                        label = { Text("Prescription Title / Diagnosis") },
                        placeholder = { Text("e.g. Hypertension Maintenance, Flu Follow-up") },
                        leadingIcon = { Icon(Icons.Default.MedicalInformation, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = viewModel.doctorName,
                        onValueChange = { viewModel.doctorName = it },
                        label = { Text("Doctor's Name") },
                        placeholder = { Text("e.g. Dr. John Smith") },
                        leadingIcon = { Icon(Icons.Default.AssignmentInd, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = viewModel.datePrescribed,
                        onValueChange = { viewModel.datePrescribed = it },
                        label = { Text("Date Prescribed (YYYY-MM-DD) *") },
                        leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = viewModel.notes,
                        onValueChange = { viewModel.notes = it },
                        label = { Text("Doctor's Advice / Notes") },
                        placeholder = { Text("e.g. Low salt diet, follow-up in 2 weeks") },
                        leadingIcon = { Icon(Icons.Default.StickyNote2, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 2
                    )
                }
            }

            // Prescribed Medications List
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Prescribed Drugs (${viewModel.medications.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    FilledTonalButton(
                        onClick = { viewModel.addMedication() },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Another Drug")
                    }
                }

                viewModel.medications.forEachIndexed { index, med ->
                    MedicationInputCard(
                        index = index,
                        total = viewModel.medications.size,
                        medication = med,
                        onNameChange = { viewModel.updateMedicationName(index, it) },
                        onDosageChange = { viewModel.updateMedicationDosage(index, it) },
                        onMorningChange = { viewModel.updateMedicationMorning(index, it) },
                        onNoonChange = { viewModel.updateMedicationNoon(index, it) },
                        onNightChange = { viewModel.updateMedicationNight(index, it) },
                        onDurationChange = { viewModel.updateMedicationDuration(index, it) },
                        onInstructionsChange = { viewModel.updateMedicationInstructions(index, it) },
                        onRemove = { viewModel.removeMedication(index) }
                    )
                }

                // Prominent Add Another Drug Button at bottom of list
                OutlinedButton(
                    onClick = { viewModel.addMedication() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                    )
                ) {
                    Icon(Icons.Default.PostAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Add Another Drug",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Photo / Rx Scan Attachment Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Prescription Scan / Photo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    ImageAttachmentPicker(
                        imageUri = viewModel.imageUri,
                        onImageSelected = { viewModel.imageUri = it },
                        label = "Attach photo of doctor's prescription pad or packaging"
                    )
                }
            }

            if (viewModel.errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        viewModel.errorMessage!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Button(
                onClick = { viewModel.savePrescription(onBack) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Save Prescription", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MedicationInputCard(
    index: Int,
    total: Int,
    medication: MedicationInput,
    onNameChange: (String) -> Unit,
    onDosageChange: (String) -> Unit,
    onMorningChange: (String) -> Unit,
    onNoonChange: (String) -> Unit,
    onNightChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onInstructionsChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                "#${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        if (medication.name.isNotBlank()) medication.name else "Medication #${index + 1}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (total > 1) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Remove Drug",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = medication.name,
                onValueChange = onNameChange,
                label = { Text("Drug Name * (e.g. Amoxicillin)") },
                leadingIcon = { Icon(Icons.Default.Medication, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = medication.dosage,
                    onValueChange = onDosageChange,
                    label = { Text("Dosage (e.g. 500mg)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = medication.duration,
                    onValueChange = onDurationChange,
                    label = { Text("Duration (e.g. 7 days)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // 3-box Dosage Schedule (Morning - Noon - Night)
            DosageScheduleInput(
                morning = medication.morning,
                noon = medication.noon,
                night = medication.night,
                onMorningChange = onMorningChange,
                onNoonChange = onNoonChange,
                onNightChange = onNightChange
            )

            OutlinedTextField(
                value = medication.instructions,
                onValueChange = onInstructionsChange,
                label = { Text("Instructions (e.g. Take after meals, with water)") },
                leadingIcon = { Icon(Icons.Default.StickyNote2, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }
    }
}

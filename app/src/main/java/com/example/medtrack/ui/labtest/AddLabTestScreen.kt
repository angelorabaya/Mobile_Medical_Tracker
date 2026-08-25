package com.example.medtrack.ui.labtest

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medtrack.data.entity.LabTestType
import com.example.medtrack.ui.components.CategoryBadge
import com.example.medtrack.ui.components.ImageAttachmentPicker
import com.example.medtrack.ui.labtest.components.ManageLabTestTypesDialog
import com.example.medtrack.util.LabResultEvaluator
import com.example.medtrack.util.LabResultStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLabTestScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddLabTestViewModel = viewModel()
) {
    val testTypes by viewModel.testTypes.collectAsStateWithLifecycle()
    var showManageTypesDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Lab Test", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showManageTypesDialog = true }) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Manage Test Types",
                            tint = MaterialTheme.colorScheme.primary
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
                        "Visit / Lab Request Info",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = viewModel.title,
                        onValueChange = { viewModel.title = it },
                        label = { Text("Lab Test Title / Purpose (Optional)") },
                        placeholder = { Text("e.g. Annual Executive Checkup, Blood Chemistry") },
                        leadingIcon = { Icon(Icons.Default.MedicalInformation, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = viewModel.testDate,
                        onValueChange = { viewModel.testDate = it },
                        label = { Text("Test Date (YYYY-MM-DD) *") },
                        leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        isError = viewModel.errorMessage != null && viewModel.testDate.isBlank()
                    )

                    OutlinedTextField(
                        value = viewModel.labName,
                        onValueChange = { viewModel.labName = it },
                        label = { Text("Laboratory / Diagnostic Center") },
                        placeholder = { Text("e.g. St. Luke's Medical Center, Quest Diagnostics") },
                        leadingIcon = { Icon(Icons.Default.LocalHospital, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = viewModel.doctorName,
                        onValueChange = { viewModel.doctorName = it },
                        label = { Text("Doctor / Ordering Physician") },
                        placeholder = { Text("e.g. Dr. Jane Smith") },
                        leadingIcon = { Icon(Icons.Default.AssignmentInd, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = viewModel.notes,
                        onValueChange = { viewModel.notes = it },
                        label = { Text("General Notes / Doctor's Advice") },
                        placeholder = { Text("e.g. Fasting 10-12 hours prior, drink water") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 2
                    )
                }
            }

            // Multiple Lab Test Items List
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Lab Tests (${viewModel.items.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    TextButton(
                        onClick = { showManageTypesDialog = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Manage Types", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }

                viewModel.items.forEachIndexed { index, item ->
                    LabTestItemInputCard(
                        index = index,
                        total = viewModel.items.size,
                        item = item,
                        testTypes = testTypes,
                        categoryOptions = viewModel.categoryOptions,
                        onNameChange = { viewModel.updateItemTestName(index, it) },
                        onCategoryChange = { viewModel.updateItemCategory(index, it) },
                        onResultsChange = { viewModel.updateItemResults(index, it) },
                        onNormalRangeChange = { viewModel.updateItemNormalRange(index, it) },
                        onNotesChange = { viewModel.updateItemNotes(index, it) },
                        onSelectTestType = { viewModel.selectTestTypeForItem(index, it) },
                        onSaveNewTestType = { name, cat, range ->
                            viewModel.addTestType(name, cat, range)
                        },
                        onOpenManageTypes = { showManageTypesDialog = true },
                        onRemove = { viewModel.removeItem(index) }
                    )
                }

                // Prominent Add Test / Description Button at the bottom of the last test
                OutlinedButton(
                    onClick = { viewModel.addItem() },
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
                        "Add Another Test / Description",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Photo / Result Scan Attachment Card
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
                        "Lab Report Scan / Document Attachment",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    ImageAttachmentPicker(
                        imageUri = viewModel.imageUri,
                        onImageSelected = { viewModel.imageUri = it },
                        label = "Attach photo of lab report, scan, or requisition sheet"
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
                onClick = { viewModel.saveLabTest(onBack) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Save Lab Test", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }

    // Manage Lab Test Types Dialog (Add, Edit, Delete, Search)
    if (showManageTypesDialog) {
        ManageLabTestTypesDialog(
            testTypes = testTypes,
            categoryOptions = viewModel.categoryOptions,
            onAddType = { name, cat, range ->
                viewModel.addTestType(name, cat, range)
            },
            onUpdateType = { type ->
                viewModel.updateTestType(type)
            },
            onDeleteType = { type ->
                viewModel.deleteTestType(type)
            },
            onSelectType = { type ->
                if (viewModel.items.isNotEmpty()) {
                    viewModel.selectTestTypeForItem(viewModel.items.size - 1, type)
                }
            },
            onDismiss = { showManageTypesDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LabTestItemInputCard(
    index: Int,
    total: Int,
    item: LabTestItemInput,
    testTypes: List<LabTestType>,
    categoryOptions: List<String>,
    onNameChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onResultsChange: (String) -> Unit,
    onNormalRangeChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSelectTestType: (LabTestType) -> Unit,
    onSaveNewTestType: (String, String, String) -> Unit,
    onOpenManageTypes: () -> Unit,
    onRemove: () -> Unit
) {
    var testTypeDropdownExpanded by remember { mutableStateOf(false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    val filteredDropdownTypes = remember(testTypes, item.testName) {
        if (item.testName.isBlank()) testTypes
        else {
            val matches = testTypes.filter { it.name.contains(item.testName, ignoreCase = true) }
            if (matches.isNotEmpty()) matches else testTypes
        }
    }

    val isExistingType = remember(testTypes, item.testName) {
        testTypes.any { it.name.equals(item.testName.trim(), ignoreCase = true) }
    }

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
                        if (item.testName.isNotBlank()) item.testName else "Lab Test #${index + 1}",
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
                            contentDescription = "Remove Test",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Test Name ExposedDropdownMenuBox with autocomplete & search
            ExposedDropdownMenuBox(
                expanded = testTypeDropdownExpanded,
                onExpandedChange = { testTypeDropdownExpanded = !testTypeDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = item.testName,
                    onValueChange = {
                        onNameChange(it)
                        testTypeDropdownExpanded = true
                    },
                    label = { Text("Select or Type Lab Test *") },
                    placeholder = { Text("e.g. Complete Blood Count (CBC)") },
                    leadingIcon = { Icon(Icons.Default.Science, contentDescription = null) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = testTypeDropdownExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                ExposedDropdownMenu(
                    expanded = testTypeDropdownExpanded,
                    onDismissRequest = { testTypeDropdownExpanded = false }
                ) {
                    // Top item: Manage / Add Test Types
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.AddCircleOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    "Manage / Add New Test Types...",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        onClick = {
                            testTypeDropdownExpanded = false
                            onOpenManageTypes()
                        }
                    )

                    HorizontalDivider()

                    filteredDropdownTypes.forEach { type ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        type.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (item.testName == type.name) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    CategoryBadge(category = type.defaultCategory)
                                }
                            },
                            onClick = {
                                onSelectTestType(type)
                                testTypeDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Save custom typed test to list chip
            if (item.testName.isNotBlank() && !isExistingType) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.BookmarkAdd,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                "Save \"${item.testName}\" to standard list?",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        TextButton(
                            onClick = {
                                onSaveNewTestType(item.testName, item.category, item.normalRange)
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Save to List", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Category selector dropdown
            ExposedDropdownMenuBox(
                expanded = categoryDropdownExpanded,
                onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = item.category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = categoryDropdownExpanded,
                    onDismissRequest = { categoryDropdownExpanded = false }
                ) {
                    categoryOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onCategoryChange(option)
                                categoryDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Results & Findings with live evaluation indicator
            val liveEvalStatus = remember(item.results, item.normalRange) {
                LabResultEvaluator.evaluate(item.results, item.normalRange)
            }
            val liveStatusColors = LabResultEvaluator.getStatusColors(
                status = liveEvalStatus,
                defaultTextColor = MaterialTheme.colorScheme.onSurface
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Results / Findings",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (liveEvalStatus != LabResultStatus.UNSPECIFIED) liveStatusColors.textColor else MaterialTheme.colorScheme.onSurface
                    )

                    if (liveStatusColors.badgeText != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = liveStatusColors.badgeBg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = if (liveEvalStatus == LabResultStatus.NORMAL) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = liveStatusColors.badgeColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    liveStatusColors.badgeText,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = liveStatusColors.badgeColor
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = item.results,
                    onValueChange = onResultsChange,
                    placeholder = { Text("e.g. Hemoglobin: 14.2 g/dL, WBC: 6.5") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )
            }

            // Normal Reference Range
            OutlinedTextField(
                value = item.normalRange,
                onValueChange = onNormalRangeChange,
                label = { Text("Normal Reference Range") },
                placeholder = { Text("e.g. 13.5 - 17.5 g/dL") },
                leadingIcon = { Icon(Icons.Default.Rule, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Test-specific notes / remarks
            OutlinedTextField(
                value = item.notes,
                onValueChange = onNotesChange,
                label = { Text("Test Remarks / Notes") },
                placeholder = { Text("e.g. Fasting 10 hours prior, repeated test") },
                leadingIcon = { Icon(Icons.Default.StickyNote2, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 1
            )
        }
    }
}

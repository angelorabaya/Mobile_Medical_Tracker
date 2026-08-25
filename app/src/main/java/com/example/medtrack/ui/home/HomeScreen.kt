package com.example.medtrack.ui.home

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.medtrack.data.entity.BmiRecord
import com.example.medtrack.data.entity.Patient
import com.example.medtrack.data.entity.PendingLabOrder
import com.example.medtrack.theme.*
import com.example.medtrack.ui.components.CategoryBadge
import com.example.medtrack.util.AsianBmiCalculator
import com.example.medtrack.util.AsianBmiCategory
import com.example.medtrack.util.ComparisonTrend
import com.example.medtrack.util.LabResultEvaluator
import com.example.medtrack.util.LabTestComparison
import com.example.medtrack.util.PendingLabOrderHelper
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToRegistration: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToLabTests: () -> Unit,
    onNavigateToPrescriptions: () -> Unit,
    onNavigateToReminders: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val patient by viewModel.patient.collectAsStateWithLifecycle()
    val labTestCount by viewModel.labTestCount.collectAsStateWithLifecycle()
    val activePrescriptionCount by viewModel.activePrescriptionCount.collectAsStateWithLifecycle()
    val comparativeLabPanels by viewModel.comparativeLabPanels.collectAsStateWithLifecycle()
    val latestBmiRecord by viewModel.latestBmiRecord.collectAsStateWithLifecycle()
    val pendingLabOrders by viewModel.pendingLabOrders.collectAsStateWithLifecycle()
    val availableLabTestTypes by viewModel.availableLabTestTypes.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = PrimaryContainerLight,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.HealthAndSafety,
                                    contentDescription = null,
                                    tint = PrimaryTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "VitalsIQ",
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    if (patient != null) {
                        IconButton(onClick = onNavigateToProfile) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                if (patient?.photoUri?.isNotBlank() == true) {
                                    AsyncImage(
                                        model = File(patient!!.photoUri),
                                        contentDescription = "Profile",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.AccountCircle,
                                            contentDescription = "Profile",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }
                        }
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            if (patient == null) {
                WelcomeHeroCard(onGetStarted = onNavigateToRegistration)
            } else {
                PatientHeroCard(patient = patient!!, onEdit = onNavigateToProfile)

                Text(
                    "Health Dashboard",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Quick stats & features
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureCard(
                        title = "Lab Tests",
                        subtitle = "$labTestCount recorded",
                        count = labTestCount,
                        icon = Icons.Default.Biotech,
                        iconBg = CategoryBloodBg,
                        iconTint = CategoryBlood,
                        onClick = onNavigateToLabTests,
                        modifier = Modifier.weight(1f)
                    )

                    FeatureCard(
                        title = "Prescriptions",
                        subtitle = "$activePrescriptionCount active",
                        count = activePrescriptionCount,
                        icon = Icons.Default.Medication,
                        iconBg = SecondaryContainerLight,
                        iconTint = SecondaryMint,
                        onClick = onNavigateToPrescriptions,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Full width Reminders action card
                RemindersBannerCard(onClick = onNavigateToReminders)

                // Pending Lab Order Card (Upcoming schedules & 1-day alerts)
                PendingLabOrderCard(
                    orders = pendingLabOrders,
                    availableTestTypes = availableLabTestTypes,
                    onSaveOrder = { testName, date, time, facility, prep, notes, reminder ->
                        viewModel.savePendingLabOrder(testName, date, time, facility, prep, notes, reminder)
                    },
                    onCompleteOrder = { order ->
                        viewModel.markLabOrderCompleted(order)
                    },
                    onDeleteOrder = { order ->
                        viewModel.deletePendingLabOrder(order)
                    }
                )

                // Asian Body Mass Index (BMI) Calculator
                AsianBmiCalculatorCard(
                    latestRecord = latestBmiRecord,
                    onSaveRecord = { weight, height, bmi, category ->
                        viewModel.saveBmiRecord(weight, height, bmi, category)
                    }
                )

                // Comparative Lab Panel Review
                ComparativeLabPanelReviewCard(
                    comparisons = comparativeLabPanels,
                    onNavigateToLabTests = onNavigateToLabTests
                )
            }
        }
    }
}

@Composable
private fun WelcomeHeroCard(onGetStarted: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            PrimaryTealDark,
                            PrimaryTeal
                        )
                    )
                )
                .padding(28.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.HealthAndSafety,
                            contentDescription = null,
                            modifier = Modifier.size(44.dp),
                            tint = Color.White
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Welcome to VitalsIQ",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Your private healthcare companion. Track lab tests, doctor prescriptions, photo attachments, and medicine reminders.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                Button(
                    onClick = onGetStarted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = PrimaryTealDark
                    )
                ) {
                    Text(
                        "Get Started",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PatientHeroCard(patient: Patient, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            PrimaryTealDark,
                            PrimaryTeal
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top Header Row: Avatar + Name on left (with weight) + Edit Button on right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(52.dp)
                        ) {
                            if (patient.photoUri.isNotBlank()) {
                                AsyncImage(
                                    model = File(patient.photoUri),
                                    contentDescription = "Patient Photo",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(34.dp)
                                    )
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Patient Profile",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                patient.fullName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 2
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    FilledTonalIconButton(
                        onClick = onEdit,
                        shape = RoundedCornerShape(12.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.22f),
                            contentColor = Color.White
                        ),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.EditNote,
                            contentDescription = "Edit Profile",
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                // Info tags with FlowRow so items wrap cleanly on any screen width
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (patient.bloodType.isNotBlank()) {
                        HeroPill(label = "Blood Type", value = patient.bloodType, icon = Icons.Default.Bloodtype)
                    }
                    if (patient.dateOfBirth.isNotBlank()) {
                        HeroPill(label = "DOB", value = patient.dateOfBirth, icon = Icons.Default.Cake)
                    }
                    if (patient.gender.isNotBlank()) {
                        HeroPill(label = "Gender", value = patient.gender, icon = Icons.Default.Wc)
                    }
                    if (patient.emergencyContact.isNotBlank()) {
                        HeroPill(label = "Emergency", value = patient.emergencyContact, icon = Icons.Default.ContactEmergency)
                    }
                }

                // Allergies banner
                if (patient.allergies.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.22f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = Color(0xFFFFD166),
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    "Known Allergies",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD166)
                                )
                                Text(
                                    patient.allergies,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroPill(label: String, value: String, icon: ImageVector) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(14.dp)
            )
            Column {
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.75f)
                )
                Text(
                    value,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    subtitle: String,
    count: Int,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = iconBg,
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            icon,
                            contentDescription = title,
                            modifier = Modifier.size(22.dp),
                            tint = iconTint
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = PrimaryContainerLight,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "$count",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryTealDark
                        )
                    }
                }
            }

            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RemindersBannerCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = TertiaryContainerLight,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.AlarmOn,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = TertiaryCoral
                        )
                    }
                }
                Column {
                    Text(
                        "Medicine Reminders",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Configure daily dosage alarm alerts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = "Open",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ComparativeLabPanelReviewCard(
    comparisons: List<LabTestComparison>,
    onNavigateToLabTests: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var isExpanded by remember { mutableStateOf(false) }

    val elevatedCount = remember(comparisons) { comparisons.count { it.trend == ComparisonTrend.ELEVATED } }
    val decreasedCount = remember(comparisons) { comparisons.count { it.trend == ComparisonTrend.DECREASED } }
    val unchangedCount = remember(comparisons) { comparisons.count { it.trend == ComparisonTrend.UNCHANGED } }
    val baselineCount = remember(comparisons) { comparisons.count { it.trend == ComparisonTrend.NO_PREVIOUS } }

    val filteredList = remember(comparisons, searchQuery, selectedFilter) {
        comparisons.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                item.testName.contains(searchQuery, ignoreCase = true) ||
                item.category.contains(searchQuery, ignoreCase = true) ||
                item.recentResult.contains(searchQuery, ignoreCase = true) ||
                (item.previousResult?.contains(searchQuery, ignoreCase = true) == true)

            val matchesFilter = when (selectedFilter) {
                "Elevated" -> item.trend == ComparisonTrend.ELEVATED
                "Decreased" -> item.trend == ComparisonTrend.DECREASED
                "Stable" -> item.trend == ComparisonTrend.UNCHANGED
                "Baseline" -> item.trend == ComparisonTrend.NO_PREVIOUS
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.QueryStats,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Column {
                        Text(
                            "Comparative Lab Panel Review",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            if (!isExpanded && comparisons.isNotEmpty()) {
                                if (elevatedCount > 0) "$elevatedCount Elevated • ${comparisons.size} Tests Analyzed (Tap to expand)"
                                else "${comparisons.size} Tests Analyzed (Tap to expand)"
                            } else "Recent vs Previous Findings & Trends",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (!isExpanded && elevatedCount > 0) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (comparisons.isEmpty()) {
                        // Empty State
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Biotech,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                                Text(
                                    "No Lab Test History Yet",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "When you record lab tests across multiple dates, this panel automatically computes elevated vs decreased changes and clinical trend shifts.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(Modifier.height(4.dp))
                                Button(
                                    onClick = onNavigateToLabTests,
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Add Lab Tests")
                                }
                            }
                        }
                    } else {
                        // Summary Stats Bar / Quick Filters
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedFilter == "All",
                                onClick = { selectedFilter = "All" },
                                label = { Text("All (${comparisons.size})") },
                                shape = RoundedCornerShape(10.dp)
                            )

                            if (elevatedCount > 0) {
                                FilterChip(
                                    selected = selectedFilter == "Elevated",
                                    onClick = { selectedFilter = "Elevated" },
                                    label = { Text("▲ Elevated ($elevatedCount)") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFFFEBEE),
                                        selectedLabelColor = Color(0xFFD32F2F)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            if (decreasedCount > 0) {
                                FilterChip(
                                    selected = selectedFilter == "Decreased",
                                    onClick = { selectedFilter = "Decreased" },
                                    label = { Text("▼ Decreased ($decreasedCount)") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFE0F2F1),
                                        selectedLabelColor = Color(0xFF00897B)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            if (unchangedCount > 0) {
                                FilterChip(
                                    selected = selectedFilter == "Stable",
                                    onClick = { selectedFilter = "Stable" },
                                    label = { Text("— Stable ($unchangedCount)") },
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            if (baselineCount > 0) {
                                FilterChip(
                                    selected = selectedFilter == "Baseline",
                                    onClick = { selectedFilter = "Baseline" },
                                    label = { Text("Baseline ($baselineCount)") },
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }

                        // Search box if more than 3 tests
                        if (comparisons.size > 3) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search test description...") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotBlank()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(
                                                Icons.Default.Clear,
                                                contentDescription = "Clear search",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // Comparison cards
                        if (filteredList.isEmpty()) {
                            Text(
                                "No lab test descriptions match this filter.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            filteredList.forEach { item ->
                                ComparativeLabItemCard(item = item)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ComparativeLabItemCard(item: LabTestComparison) {
    var expandedHistory by remember { mutableStateOf(false) }

    val (trendLabel, trendColor, trendBg, trendIcon) = when (item.trend) {
        ComparisonTrend.ELEVATED -> ComparativeQuadruple("▲ Elevated", Color(0xFFD32F2F), Color(0xFFFFEBEE), Icons.Default.TrendingUp)
        ComparisonTrend.DECREASED -> ComparativeQuadruple("▼ Decreased", Color(0xFF00897B), Color(0xFFE0F2F1), Icons.Default.TrendingDown)
        ComparisonTrend.UNCHANGED -> ComparativeQuadruple("— Stable", Color(0xFF455A64), Color(0xFFECEFF1), Icons.Default.TrendingFlat)
        ComparisonTrend.NO_PREVIOUS -> ComparativeQuadruple("Baseline (1st)", Color(0xFF5E35B1), Color(0xFFEDE7F6), Icons.Default.Bookmark)
    }

    val recentColors = LabResultEvaluator.getStatusColors(item.recentStatus, MaterialTheme.colorScheme.onSurface)
    val prevColors = if (item.previousStatus != null) {
        LabResultEvaluator.getStatusColors(item.previousStatus, MaterialTheme.colorScheme.onSurface)
    } else null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Test Name (Top) followed by Category Badge and Trend Badge on the exact same horizontal line
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    item.testName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryBadge(category = item.category)

                    // Trend badge (e.g. "Baseline (1st)", "▲ Elevated", "▼ Decreased", "— Stable")
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = trendBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                trendIcon,
                                contentDescription = null,
                                tint = trendColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = trendLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = trendColor
                            )
                        }
                    }
                }
            }

            // Delta summary text if available
            if (item.trend != ComparisonTrend.NO_PREVIOUS && item.deltaSummary.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = trendBg.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Delta Difference:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            item.deltaSummary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = trendColor
                        )
                    }
                }
            }

            // Side-by-Side Comparison: Recent vs Previous (Equal intrinsic height & balanced alignments)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Recent Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "Recent",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                item.recentDate,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                item.recentResult,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = recentColors.textColor
                            )

                            if (recentColors.badgeText != null) {
                                Text(
                                    recentColors.badgeText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = recentColors.badgeColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Previous Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (item.previousResult != null) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    "Previous",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    item.previousDate ?: "",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(Modifier.height(4.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    item.previousResult,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = prevColors?.textColor ?: MaterialTheme.colorScheme.onSurface
                                )

                                if (prevColors?.badgeText != null) {
                                    Text(
                                        prevColors.badgeText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = prevColors.badgeColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    "Previous",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "—",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(Modifier.height(4.dp))

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    "No prior record",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Initial baseline",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Normal Range & Status Assessment Section (Range on top, Status underneath)
            if (item.recentNormalRange.isNotBlank() || item.statusTransition.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 1. Normal Range (rendered first on top)
                        if (item.recentNormalRange.isNotBlank()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    "Normal Range:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    item.recentNormalRange,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // Divider between range and status if both exist
                        if (item.recentNormalRange.isNotBlank() && item.statusTransition.isNotBlank()) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                thickness = 0.5.dp
                            )
                        }

                        // 2. Status / Assessment (rendered cleanly under the range)
                        if (item.statusTransition.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Status / Finding:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when {
                                        item.statusTransition.contains("Out of Range") -> Color(0xFFFFEBEE)
                                        item.statusTransition.contains("Normal") || item.statusTransition.contains("Improved") -> Color(0xFFE8F5E9)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ) {
                                    Text(
                                        text = item.statusTransition,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            item.statusTransition.contains("Out of Range") -> Color(0xFFD32F2F)
                                            item.statusTransition.contains("Normal") || item.statusTransition.contains("Improved") -> Color(0xFF2E7D32)
                                            else -> MaterialTheme.colorScheme.onSurface
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Historical timeline expander if > 2 records
            if (item.totalHistoricalRecords > 2) {
                TextButton(
                    onClick = { expandedHistory = !expandedHistory },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (expandedHistory) "Hide Full History (${item.totalHistoricalRecords})"
                        else "View All ${item.totalHistoricalRecords} Historical Findings",
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                AnimatedVisibility(visible = expandedHistory) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item.history.forEachIndexed { idx, h ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "${h.date} ${if (idx == 0) "(Latest)" else if (idx == 1) "(Previous)" else ""}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (idx == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (h.labTitle.isNotBlank()) {
                                            Text(
                                                h.labTitle,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    val colors = LabResultEvaluator.getStatusColors(h.status, MaterialTheme.colorScheme.onSurface)
                                    Text(
                                        h.result,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class ComparativeQuadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun AsianBmiCalculatorCard(
    latestRecord: BmiRecord?,
    onSaveRecord: (Double, Double, Double, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var weightInput by remember { mutableStateOf("") }
    var feetInput by remember { mutableStateOf("") }
    var inchesInput by remember { mutableStateOf("") }
    var calculatedBmi by remember { mutableStateOf<Double?>(null) }
    var calculatedCategory by remember { mutableStateOf<AsianBmiCategory?>(null) }
    var showSavedNotification by remember { mutableStateOf(false) }

    // Synchronize inputs with latest record when loaded
    LaunchedEffect(latestRecord) {
        if (latestRecord != null && weightInput.isEmpty() && feetInput.isEmpty() && inchesInput.isEmpty()) {
            weightInput = AsianBmiCalculator.formatWeight(latestRecord.weightKg)
            val (ft, inc) = AsianBmiCalculator.cmToFeetInches(latestRecord.heightCm)
            feetInput = if (ft > 0) ft.toString() else ""
            inchesInput = if (inc > 0.0) {
                if (inc % 1.0 == 0.0) inc.toInt().toString() else String.format(Locale.US, "%.1f", inc)
            } else if (ft > 0) "0" else ""
            calculatedBmi = latestRecord.bmi
            calculatedCategory = AsianBmiCategory.fromBmi(latestRecord.bmi)
        }
    }

    val recalculate = { wStr: String, ftStr: String, inStr: String ->
        val w = wStr.toDoubleOrNull()
        val ft = ftStr.toIntOrNull()
        val inc = inStr.toDoubleOrNull() ?: 0.0
        if (w != null && ft != null && w > 0 && ft > 0 && inc >= 0 && inc < 12.0) {
            val totalCm = AsianBmiCalculator.feetInchesToCm(ft, inc)
            val bmi = AsianBmiCalculator.calculateBmi(w, totalCm)
            if (bmi != null) {
                calculatedBmi = bmi
                calculatedCategory = AsianBmiCategory.fromBmi(bmi)
            }
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Calculate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Text(
                        "Asian BMI",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )

                    if (calculatedBmi != null && calculatedCategory != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = calculatedCategory!!.containerColor
                        ) {
                            Text(
                                "${AsianBmiCalculator.formatBmi(calculatedBmi!!)} • ${calculatedCategory!!.label}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = calculatedCategory!!.textColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                maxLines = 1
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            "WHO Asian",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            maxLines = 1
                        )
                    }
                }

                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Weight Input Row
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d*$"""))) {
                                weightInput = input
                                showSavedNotification = false
                                recalculate(input, feetInput, inchesInput)
                            }
                        },
                        label = { Text("Weight (kg)") },
                        placeholder = { Text("e.g. 65") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Height Input: Feet and Inches Row
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Height (Feet & Inches)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = feetInput,
                                onValueChange = { input ->
                                    if (input.isEmpty() || (input.matches(Regex("""^\d+$""")) && input.toInt() <= 9)) {
                                        feetInput = input
                                        showSavedNotification = false
                                        recalculate(weightInput, input, inchesInput)
                                    }
                                },
                                label = { Text("Feet (ft)") },
                                placeholder = { Text("e.g. 5") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = inchesInput,
                                onValueChange = { input ->
                                    if (input.isEmpty() || input.matches(Regex("""^\d*\.?\d*$"""))) {
                                        val num = input.toDoubleOrNull()
                                        if (num == null || num < 12.0) {
                                            inchesInput = input
                                            showSavedNotification = false
                                            recalculate(weightInput, feetInput, input)
                                        }
                                    }
                                },
                                label = { Text("Inches (in)") },
                                placeholder = { Text("e.g. 7") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Converted metric height indicator
                        val ftVal = feetInput.toIntOrNull()
                        val incVal = inchesInput.toDoubleOrNull() ?: 0.0
                        if (ftVal != null && ftVal > 0) {
                            val totalCm = AsianBmiCalculator.feetInchesToCm(ftVal, incVal)
                            Text(
                                "Total Height: ${AsianBmiCalculator.formatFeetInches(ftVal, incVal)} (${AsianBmiCalculator.formatHeight(totalCm)} cm)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Action buttons (Calculate & Save / Clear)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                val w = weightInput.toDoubleOrNull()
                                val ft = feetInput.toIntOrNull()
                                val inc = inchesInput.toDoubleOrNull() ?: 0.0
                                if (w != null && ft != null && w > 0 && ft > 0 && inc >= 0) {
                                    val totalCm = AsianBmiCalculator.feetInchesToCm(ft, inc)
                                    val bmi = AsianBmiCalculator.calculateBmi(w, totalCm)
                                    if (bmi != null) {
                                        val cat = AsianBmiCategory.fromBmi(bmi)
                                        calculatedBmi = bmi
                                        calculatedCategory = cat
                                        onSaveRecord(w, totalCm, bmi, cat.label)
                                        showSavedNotification = true
                                    }
                                }
                            },
                            enabled = weightInput.toDoubleOrNull() != null && feetInput.toIntOrNull() != null,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Calculate & Record", fontWeight = FontWeight.Bold)
                        }

                        if (weightInput.isNotEmpty() || feetInput.isNotEmpty() || inchesInput.isNotEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    weightInput = ""
                                    feetInput = ""
                                    inchesInput = ""
                                    calculatedBmi = null
                                    calculatedCategory = null
                                    showSavedNotification = false
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Clear")
                            }
                        }
                    }

                    if (showSavedNotification) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryContainerLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = PrimaryTeal,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    "Recorded latest measurements successfully!",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PrimaryTealDark,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Outcomes Section
                    if (calculatedBmi != null && calculatedCategory != null) {
                        val category = calculatedCategory!!
                        val bmi = calculatedBmi!!
                        val ftVal = feetInput.toIntOrNull()
                        val incVal = inchesInput.toDoubleOrNull() ?: 0.0
                        val heightVal = if (ftVal != null && ftVal > 0) {
                            AsianBmiCalculator.feetInchesToCm(ftVal, incVal)
                        } else {
                            latestRecord?.heightCm
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = category.containerColor,
                            border = BorderStroke(1.dp, category.primaryColor.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Result Header Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "Calculated Asian BMI",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Row(
                                            verticalAlignment = Alignment.Bottom,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                AsianBmiCalculator.formatBmi(bmi),
                                                style = MaterialTheme.typography.headlineLarge,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = category.textColor
                                            )
                                            Text(
                                                "kg/m²",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(bottom = 4.dp)
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = category.primaryColor,
                                        shadowElevation = 2.dp
                                    ) {
                                        Text(
                                            category.label,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }

                                HorizontalDivider(color = category.primaryColor.copy(alpha = 0.2f))

                                // Asian BMI 5-Zone Spectrum Bar
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        "Asian Classification Spectrum",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    AsianBmiSpectrumBar(currentBmi = bmi)
                                }

                                // Healthy Weight Target info
                                if (heightVal != null && heightVal > 0) {
                                    val range = AsianBmiCalculator.getHealthyWeightRange(heightVal)
                                    if (range != null) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Info,
                                                    contentDescription = null,
                                                    tint = category.primaryColor,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Column {
                                                    Text(
                                                        "Healthy Weight for ${AsianBmiCalculator.formatHeightWithFeetInches(heightVal)} (Asian Std):",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        "${AsianBmiCalculator.formatWeight(range.first)} kg – ${AsianBmiCalculator.formatWeight(range.second)} kg",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFF2E7D32)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Clinical Insight Box
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.HealthAndSafety,
                                        contentDescription = null,
                                        tint = category.primaryColor,
                                        modifier = Modifier.size(18.dp).padding(top = 2.dp)
                                    )
                                    Text(
                                        category.healthRisk,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = category.textColor,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Last recorded timestamp if available
                                if (latestRecord != null) {
                                    val dateStr = remember(latestRecord.calculatedAt) {
                                        SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
                                            .format(Date(latestRecord.calculatedAt))
                                    }
                                    Text(
                                        "Last recorded: $dateStr (${AsianBmiCalculator.formatWeight(latestRecord.weightKg)} kg, ${AsianBmiCalculator.formatHeightWithFeetInches(latestRecord.heightCm)})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    // Expandable Asian BMI Cutoff Reference Table
                    var showReferenceTable by remember { mutableStateOf(false) }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showReferenceTable = !showReferenceTable },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Analytics,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        "WHO Asian BMI Criteria Reference",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Icon(
                                    if (showReferenceTable) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            AnimatedVisibility(visible = showReferenceTable) {
                                Column(
                                    modifier = Modifier.padding(top = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        "Asian populations generally exhibit higher body fat percentages and cardiovascular risks at lower BMI thresholds compared to Western standards.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    AsianBmiCategory.entries.forEach { cat ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = cat.containerColor,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    cat.label,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = cat.textColor
                                                )
                                                Text(
                                                    cat.rangeDescription,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = cat.textColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AsianBmiSpectrumBar(currentBmi: Double) {
    val categories = AsianBmiCategory.entries

    Column(modifier = Modifier.fillMaxWidth()) {
        // Multi-segment colored bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
        ) {
            categories.forEach { cat ->
                Box(
                    modifier = Modifier
                        .weight(if (cat == AsianBmiCategory.NORMAL || cat == AsianBmiCategory.OBESE_CLASS_1) 1.2f else 1f)
                        .fillMaxHeight()
                        .background(cat.primaryColor)
                )
            }
        }

        // Zone threshold labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("< 18.5", style = MaterialTheme.typography.labelSmall, color = Color(0xFF0288D1))
            Text("18.5", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
            Text("23.0", style = MaterialTheme.typography.labelSmall, color = Color(0xFFF57F17))
            Text("25.0", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE65100))
            Text("≥ 30.0", style = MaterialTheme.typography.labelSmall, color = Color(0xFFC62828))
        }
    }
}

@Composable
fun PendingLabOrderCard(
    orders: List<PendingLabOrder>,
    availableTestTypes: List<String>,
    onSaveOrder: (String, String, String, String, String, String, Boolean) -> Unit,
    onCompleteOrder: (PendingLabOrder) -> Unit,
    onDeleteOrder: (PendingLabOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }
    var showScheduleDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.EventNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "Pending Lab Order",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (orders.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        "${orders.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            if (orders.isNotEmpty()) "Upcoming test schedules & 1-day alerts" else "Schedule next tests with 1-day reminder",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { showScheduleDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.AddCircle,
                            contentDescription = "Add Lab Order",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (orders.isEmpty()) {
                        // Empty state
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    "No Scheduled Lab Orders",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "Input your next scheduled lab test (e.g. FBS, Lipid Profile, HbA1c). VitalsIQ will automatically alarm you 1 day before the scheduled test so you're ready with fasting or preparations.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(Modifier.height(4.dp))
                                Button(
                                    onClick = { showScheduleDialog = true },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Schedule Next Lab Test", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // Orders list
                        orders.forEach { order ->
                            PendingLabOrderItemCard(
                                order = order,
                                onComplete = { onCompleteOrder(order) },
                                onDelete = { onDeleteOrder(order) }
                            )
                        }

                        OutlinedButton(
                            onClick = { showScheduleDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Schedule Another Lab Test", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    if (showScheduleDialog) {
        ScheduleLabOrderDialog(
            availableTestTypes = availableTestTypes,
            onDismiss = { showScheduleDialog = false },
            onSave = { testName, date, time, facility, prep, notes, reminder ->
                onSaveOrder(testName, date, time, facility, prep, notes, reminder)
                showScheduleDialog = false
            }
        )
    }
}

@Composable
private fun PendingLabOrderItemCard(
    order: PendingLabOrder,
    onComplete: () -> Unit,
    onDelete: () -> Unit
) {
    val countdown = remember(order.scheduledDate) {
        PendingLabOrderHelper.getCountdownStatus(order.scheduledDate)
    }
    val displayDate = remember(order.scheduledDate) {
        PendingLabOrderHelper.formatDisplayDate(order.scheduledDate)
    }
    val displayTime = remember(order.scheduledTime) {
        PendingLabOrderHelper.formatDisplayTime(order.scheduledTime)
    }
    val alarmInfo = remember(order.scheduledDate, order.scheduledTime) {
        PendingLabOrderHelper.getAlarmInfoText(order.scheduledDate, order.scheduledTime)
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Row: Test Title & Countdown Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    order.testName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = countdown.second.copy(alpha = 0.15f)
                ) {
                    Text(
                        countdown.first,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = countdown.second,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Schedule Date & Time Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    "$displayDate • $displayTime",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Facility / Lab Name
            if (order.facilityName.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.LocalHospital,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        order.facilityName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Fasting / Prep Note
            if (order.fastingInstructions.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFF8E1),
                    border = BorderStroke(1.dp, Color(0xFFFFE082)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.FreeBreakfast,
                            contentDescription = null,
                            tint = Color(0xFFF57F17),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            order.fastingInstructions,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }

            // Notes if any
            if (order.notes.isNotBlank()) {
                Text(
                    "Note: ${order.notes}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 1-Day Alarm Badge
            if (order.isReminderEnabled) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Alarm,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        "1-Day Prior Alarm: $alarmInfo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onComplete,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Mark Completed", style = MaterialTheme.typography.labelMedium)
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleLabOrderDialog(
    availableTestTypes: List<String>,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String, Boolean) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 1)
    }

    val defaultDate = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }

    var testName by remember { mutableStateOf("") }
    var scheduledDate by remember { mutableStateOf(defaultDate) }
    var scheduledTime by remember { mutableStateOf("07:30") }
    var facilityName by remember { mutableStateOf("") }
    var fastingInstructions by remember { mutableStateOf("10-12 hours fasting required (water only)") }
    var notes by remember { mutableStateOf("") }
    var isReminderEnabled by remember { mutableStateOf(true) }

    val commonSuggestions = listOf(
        "Fasting Blood Sugar (FBS)",
        "Lipid Profile (Cholesterol/Triglycerides)",
        "HbA1c (Glycated Hemoglobin)",
        "Complete Blood Count (CBC)",
        "Liver Function (SGPT / ALT)",
        "Kidney Function (Creatinine / BUN)",
        "Uric Acid (Serum)",
        "Thyroid Panel (TSH / FT4)",
        "Routine Urinalysis"
    )

    val allSuggestions = remember(availableTestTypes) {
        val list = mutableListOf<String>()
        list.addAll(commonSuggestions)
        availableTestTypes.forEach { type ->
            if (!list.contains(type)) list.add(type)
        }
        list
    }

    val commonPrepOptions = listOf(
        "10-12 hrs Fasting (Water only)",
        "8 hrs Fasting (Water only)",
        "No Morning Meds",
        "Water Allowed Only",
        "No Special Prep"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.AddAlert,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text("Schedule Pending Lab Order", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Set your next lab test date & time. You will be alarmed 1 day before the scheduled test.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Test Name
                OutlinedTextField(
                    value = testName,
                    onValueChange = { testName = it },
                    label = { Text("Lab Test / Procedure Name *") },
                    placeholder = { Text("e.g. Fasting Blood Sugar, Lipid Panel") },
                    leadingIcon = { Icon(Icons.Default.Biotech, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick suggestions chips
                Text(
                    "Quick Suggestions:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    allSuggestions.forEach { suggestion ->
                        SuggestionChip(
                            onClick = { testName = suggestion },
                            label = { Text(suggestion, style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                // Date and Time Pickers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedCard(
                        onClick = {
                            val dateParts = scheduledDate.split("-")
                            val y = dateParts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.YEAR)
                            val m = (dateParts.getOrNull(1)?.toIntOrNull() ?: (calendar.get(Calendar.MONTH) + 1)) - 1
                            val d = dateParts.getOrNull(2)?.toIntOrNull() ?: calendar.get(Calendar.DAY_OF_MONTH)

                            val datePicker = DatePickerDialog(
                                context,
                                { _, selectedYear, selectedMonth, selectedDay ->
                                    scheduledDate = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                                },
                                y, m, d
                            )
                            datePicker.show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Column {
                                Text("Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(scheduledDate, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    OutlinedCard(
                        onClick = {
                            val timeParts = scheduledTime.split(":")
                            val h = timeParts.getOrNull(0)?.toIntOrNull() ?: 7
                            val min = timeParts.getOrNull(1)?.toIntOrNull() ?: 30

                            val timePicker = TimePickerDialog(
                                context,
                                { _, selectedHour, selectedMinute ->
                                    scheduledTime = String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute)
                                },
                                h, min, false
                            )
                            timePicker.show()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Column {
                                Text("Time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(PendingLabOrderHelper.formatDisplayTime(scheduledTime), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Facility
                OutlinedTextField(
                    value = facilityName,
                    onValueChange = { facilityName = it },
                    label = { Text("Laboratory / Clinic (Optional)") },
                    placeholder = { Text("e.g. St. Luke's, Hi-Precision") },
                    leadingIcon = { Icon(Icons.Default.LocalHospital, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Fasting / Prep Instructions
                OutlinedTextField(
                    value = fastingInstructions,
                    onValueChange = { fastingInstructions = it },
                    label = { Text("Fasting & Preparation Note") },
                    placeholder = { Text("e.g. 10-12 hours fasting, water only") },
                    leadingIcon = { Icon(Icons.Default.FreeBreakfast, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick Prep Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    commonPrepOptions.forEach { option ->
                        FilterChip(
                            selected = fastingInstructions == option,
                            onClick = { fastingInstructions = option },
                            label = { Text(option, style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Additional Notes (Optional)") },
                    placeholder = { Text("e.g. Bring doctor's referral letter") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                // 1-Day Prior Alarm Switch
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Column {
                                Text("Alarm 1 Day Before", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Text("Alerts you 24 hrs prior to fast/prepare", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = isReminderEnabled,
                            onCheckedChange = { isReminderEnabled = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (testName.isNotBlank() && scheduledDate.isNotBlank()) {
                        onSave(testName, scheduledDate, scheduledTime, facilityName, fastingInstructions, notes, isReminderEnabled)
                    }
                },
                enabled = testName.isNotBlank() && scheduledDate.isNotBlank(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Schedule & Set Alarm", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

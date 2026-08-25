package com.example.medtrack.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medtrack.data.entity.Patient
import com.example.medtrack.theme.*
import com.example.medtrack.ui.components.CategoryBadge
import com.example.medtrack.util.ComparisonTrend
import com.example.medtrack.util.LabResultEvaluator
import com.example.medtrack.util.LabTestComparison

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
                            "MedTrack",
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
                        "Welcome to MedTrack",
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
                            modifier = Modifier.size(50.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
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
            // Test Name, Category & Trend Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.testName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    CategoryBadge(category = item.category)
                }

                // Trend badge
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

            // Side-by-Side Comparison: Recent vs Previous
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Recent Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Column {
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

                // Previous Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (item.previousResult != null) {
                            Column {
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
                        } else {
                            Column {
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

                            Text(
                                "Baseline entry",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                "No prior test",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // Reference Range and Transition Note
            if (item.recentNormalRange.isNotBlank() || item.statusTransition.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.recentNormalRange.isNotBlank()) {
                        Text(
                            "Range: ${item.recentNormalRange}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (item.statusTransition.isNotBlank()) {
                        Text(
                            item.statusTransition,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
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

package com.example.medtrack.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.medtrack.theme.*

@Composable
fun CategoryBadge(category: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor, icon) = when (category) {
        "Blood Work" -> Triple(CategoryBloodBg, CategoryBlood, Icons.Default.Bloodtype)
        "Urinalysis" -> Triple(CategoryUrineBg, CategoryUrine, Icons.Default.WaterDrop)
        "Imaging" -> Triple(CategoryImagingBg, CategoryImaging, Icons.Default.PermMedia)
        "Cardiac", "Cardiology" -> Triple(CategoryCardiacBg, CategoryCardiac, Icons.Default.MonitorHeart)
        "Pathology" -> Triple(CategoryPathologyBg, CategoryPathology, Icons.Default.Biotech)
        "Pulmonary", "Respiratory" -> Triple(CategoryOtherBg, CategoryOther, Icons.Default.Air)
        "Endocrine" -> Triple(CategoryOtherBg, CategoryOther, Icons.Default.DeviceThermostat)
        "Neurology" -> Triple(CategoryOtherBg, CategoryOther, Icons.Default.Psychology)
        else -> Triple(CategoryOtherBg, CategoryOther, Icons.Default.HealthAndSafety)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                category.ifBlank { "General" },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
        }
    }
}

@Composable
fun StatusBadge(isActive: Boolean, modifier: Modifier = Modifier) {
    val bgColor = if (isActive) StatusActiveBg else StatusCompletedBg
    val textColor = if (isActive) StatusActive else StatusCompleted
    val text = if (isActive) "Active" else "Completed"
    val icon = if (isActive) Icons.Default.CheckCircle else Icons.Default.TaskAlt

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

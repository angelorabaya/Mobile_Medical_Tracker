package com.example.medtrack.util

import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object PendingLabOrderHelper {

    fun formatDisplayDate(dateStr: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = parser.parse(dateStr) ?: return dateStr
            val formatter = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())
            formatter.format(date)
        } catch (_: Exception) {
            dateStr
        }
    }

    fun formatDisplayTime(timeStr: String): String {
        return try {
            val parts = timeStr.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: return timeStr
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
            }
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time)
        } catch (_: Exception) {
            timeStr
        }
    }

    fun getDaysDifference(dateStr: String): Long {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val targetDate = parser.parse(dateStr) ?: return 0
            val todayStr = parser.format(Date())
            val todayDate = parser.parse(todayStr) ?: return 0
            val diffMs = targetDate.time - todayDate.time
            diffMs / (1000 * 60 * 60 * 24)
        } catch (_: Exception) {
            0
        }
    }

    fun getCountdownStatus(dateStr: String): Pair<String, Color> {
        val days = getDaysDifference(dateStr)
        return when {
            days < 0 -> Pair("Overdue (${-days}d ago)", Color(0xFFC62828))
            days == 0L -> Pair("Scheduled Today", Color(0xFFE65100))
            days == 1L -> Pair("Tomorrow", Color(0xFF00897B))
            days in 2L..7L -> Pair("In $days days", Color(0xFF1976D2))
            else -> Pair("In $days days", Color(0xFF5E35B1))
        }
    }

    fun getAlarmInfoText(dateStr: String, timeStr: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = parser.parse(dateStr) ?: return "1 day before"
            val calendar = Calendar.getInstance().apply {
                time = date
                add(Calendar.DAY_OF_YEAR, -1)
            }
            val alarmDateStr = SimpleDateFormat("MMM dd", Locale.getDefault()).format(calendar.time)
            val displayTime = formatDisplayTime(timeStr)
            "Alert on $alarmDateStr at $displayTime"
        } catch (_: Exception) {
            "Alert 1 day before"
        }
    }
}

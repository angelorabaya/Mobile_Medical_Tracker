package com.example.medtrack.util

object FrequencyHelper {

    fun formatSchedule(morning: String, noon: String, night: String): String {
        val m = if (morning.trim() == "1") "1" else "0"
        val n = if (noon.trim() == "1") "1" else "0"
        val e = if (night.trim() == "1") "1" else "0"
        val code = "$m-$n-$e"

        val desc = when {
            m == "1" && n == "0" && e == "0" -> "Morning only"
            m == "0" && n == "1" && e == "0" -> "Noon / Lunch only"
            m == "0" && n == "0" && e == "1" -> "Evening / Bedtime only"
            m == "1" && n == "0" && e == "1" -> "Morning & Evening"
            m == "1" && n == "1" && e == "0" -> "Morning & Noon"
            m == "0" && n == "1" && e == "1" -> "Noon & Evening"
            m == "1" && n == "1" && e == "1" -> "Morning, Noon & Evening"
            else -> "As needed / As prescribed"
        }
        return "$code ($desc)"
    }

    fun parseSchedule(frequency: String): Triple<String, String, String> {
        val regex = Regex("""([0-9]+)\s*-\s*([0-9]+)\s*-\s*([0-9]+)""")
        val match = regex.find(frequency)
        if (match != null) {
            val m = if (match.groupValues[1] == "1") "1" else "0"
            val n = if (match.groupValues[2] == "1") "1" else "0"
            val e = if (match.groupValues[3] == "1") "1" else "0"
            return Triple(m, n, e)
        }

        // Fallback for legacy text values
        val lower = frequency.lowercase()
        return when {
            lower.contains("bedtime") || lower.contains("night") -> Triple("0", "0", "1")
            lower.contains("twice") || lower.contains("morning & evening") -> Triple("1", "0", "1")
            lower.contains("3 times") || lower.contains("three") -> Triple("1", "1", "1")
            lower.contains("noon") || lower.contains("lunch") -> Triple("0", "1", "0")
            lower.contains("morning") -> Triple("1", "0", "0")
            else -> Triple("1", "0", "0")
        }
    }

    data class ReminderPreset(
        val icon: String,
        val label: String,
        val hour: Int,
        val minute: Int
    )

    val REMINDER_TIME_PRESETS = listOf(
        ReminderPreset("🌅", "Morning (08:00)", 8, 0),
        ReminderPreset("☀️", "Noon / Lunch (12:30)", 12, 30),
        ReminderPreset("🌙", "Evening (18:30)", 18, 30),
        ReminderPreset("🛌", "Bedtime (21:30)", 21, 30)
    )
}

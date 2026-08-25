package com.example.medtrack.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateUtils {
    private val formatters = listOf(
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ofPattern("yyyy-M-d"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd"),
        DateTimeFormatter.ofPattern("yyyy/M/d"),
        DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        DateTimeFormatter.ofPattern("M/d/yyyy"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("d/M/yyyy"),
        DateTimeFormatter.ofPattern("MMM d, yyyy"),
        DateTimeFormatter.ofPattern("MMMM d, yyyy"),
        DateTimeFormatter.ofPattern("d MMM yyyy"),
        DateTimeFormatter.ofPattern("d MMMM yyyy")
    )

    fun parseDateOrNull(dateString: String?): LocalDate? {
        if (dateString.isNullOrBlank()) return null
        val trimmed = dateString.trim()
        for (formatter in formatters) {
            try {
                return LocalDate.parse(trimmed, formatter)
            } catch (_: Exception) {}
        }
        return null
    }

    val newestFirstComparator: Comparator<String?> = Comparator { d1, d2 ->
        val date1 = parseDateOrNull(d1)
        val date2 = parseDateOrNull(d2)
        when {
            date1 != null && date2 != null -> date2.compareTo(date1) // descending (newest first)
            date1 != null -> -1
            date2 != null -> 1
            else -> (d2 ?: "").compareTo(d1 ?: "")
        }
    }
}

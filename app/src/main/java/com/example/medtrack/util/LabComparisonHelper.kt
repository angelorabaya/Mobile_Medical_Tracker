package com.example.medtrack.util

import com.example.medtrack.data.entity.LabTestItem
import com.example.medtrack.data.entity.LabTestWithItems
import java.util.Locale

enum class ComparisonTrend {
    ELEVATED,      // Value increased or worsened
    DECREASED,     // Value decreased or improved
    UNCHANGED,     // Value stable / unchanged
    NO_PREVIOUS    // First recorded baseline
}

data class HistoricalLabEntry(
    val date: String,
    val labTitle: String,
    val result: String,
    val normalRange: String,
    val notes: String,
    val status: LabResultStatus,
    val numericValue: Double?
)

data class LabTestComparison(
    val testName: String,
    val category: String,
    val recentResult: String,
    val recentDate: String,
    val recentNormalRange: String,
    val recentNotes: String,
    val recentStatus: LabResultStatus,
    val recentNumericValue: Double?,
    val previousResult: String?,
    val previousDate: String?,
    val previousNormalRange: String?,
    val previousNotes: String?,
    val previousStatus: LabResultStatus?,
    val previousNumericValue: Double?,
    val trend: ComparisonTrend,
    val deltaSummary: String,
    val deltaValue: Double?,
    val unit: String,
    val statusTransition: String,
    val totalHistoricalRecords: Int,
    val history: List<HistoricalLabEntry>
)

object LabComparisonHelper {
    private val UNIT_REGEX = Regex("""(?i)\b(mg/dL|g/dL|mmol/L|uIU/mL|mEq/L|U/L|IU/L|%|pg/mL|ng/mL|pmol/L|mcg/dL|cells/mcL|x10\^3/uL|x10\^6/uL|fl|fL|mm/hr|mL/min)\b""")
    private val NUMBER_REGEX = Regex("""[-+]?[0-9]+(?:\.[0-9]+)?""")

    fun generateComparativePanels(testsWithItems: List<LabTestWithItems>): List<LabTestComparison> {
        if (testsWithItems.isEmpty()) return emptyList()

        // 1. Sort lab test orders from newest to oldest by date
        val sortedVisits = testsWithItems.sortedWith { a, b ->
            val dateComparison = DateUtils.newestFirstComparator.compare(a.labTest.testDate, b.labTest.testDate)
            if (dateComparison != 0) dateComparison
            else {
                val createdComparison = b.labTest.createdAt.compareTo(a.labTest.createdAt)
                if (createdComparison != 0) createdComparison else b.labTest.id.compareTo(a.labTest.id)
            }
        }

        // 2. Flatten and group all items by normalized test name
        val groupedMap = LinkedHashMap<String, MutableList<Pair<LabTestWithItems, LabTestItem>>>()

        for (visit in sortedVisits) {
            for (item in visit.items) {
                if (item.testName.isNotBlank()) {
                    val key = item.testName.trim()
                    groupedMap.getOrPut(key) { mutableListOf() }.add(Pair(visit, item))
                }
            }
        }

        val comparisons = mutableListOf<LabTestComparison>()

        for ((testName, records) in groupedMap) {
            if (records.isEmpty()) continue

            val (recentVisit, recentItem) = records.first()
            val recentResult = recentItem.results.trim()
            val recentRange = recentItem.normalRange.trim()
            val recentDate = recentVisit.labTest.testDate.trim()
            val recentStatus = LabResultEvaluator.evaluate(recentResult, recentRange)
            val recentNum = extractNumber(recentResult)
            val unit = extractUnit(recentResult).ifBlank { extractUnit(recentRange) }

            val historyEntries = records.map { (visit, item) ->
                HistoricalLabEntry(
                    date = visit.labTest.testDate,
                    labTitle = visit.labTest.title,
                    result = item.results,
                    normalRange = item.normalRange,
                    notes = item.notes,
                    status = LabResultEvaluator.evaluate(item.results, item.normalRange),
                    numericValue = extractNumber(item.results)
                )
            }

            if (records.size == 1) {
                comparisons.add(
                    LabTestComparison(
                        testName = testName,
                        category = recentItem.category,
                        recentResult = recentResult,
                        recentDate = recentDate,
                        recentNormalRange = recentRange,
                        recentNotes = recentItem.notes,
                        recentStatus = recentStatus,
                        recentNumericValue = recentNum,
                        previousResult = null,
                        previousDate = null,
                        previousNormalRange = null,
                        previousNotes = null,
                        previousStatus = null,
                        previousNumericValue = null,
                        trend = ComparisonTrend.NO_PREVIOUS,
                        deltaSummary = "Baseline",
                        deltaValue = null,
                        unit = unit,
                        statusTransition = if (recentStatus == LabResultStatus.NORMAL) "Normal" else if (recentStatus == LabResultStatus.ABNORMAL) "Out of Range" else "Recorded",
                        totalHistoricalRecords = 1,
                        history = historyEntries
                    )
                )
            } else {
                val (prevVisit, prevItem) = records[1]
                val prevResult = prevItem.results.trim()
                val prevRange = prevItem.normalRange.trim()
                val prevDate = prevVisit.labTest.testDate.trim()
                val prevStatus = LabResultEvaluator.evaluate(prevResult, prevRange)
                val prevNum = extractNumber(prevResult)

                val trend: ComparisonTrend
                val deltaSummary: String
                val deltaValue: Double?

                if (recentNum != null && prevNum != null) {
                    val rawDelta = recentNum - prevNum
                    val delta = Math.round(rawDelta * 100.0) / 100.0
                    deltaValue = delta
                    val unitStr = if (unit.isNotBlank()) " $unit" else ""

                    if (delta > 0.001) {
                        trend = ComparisonTrend.ELEVATED
                        deltaSummary = "▲ +$delta$unitStr"
                    } else if (delta < -0.001) {
                        trend = ComparisonTrend.DECREASED
                        deltaSummary = "▼ $delta$unitStr"
                    } else {
                        trend = ComparisonTrend.UNCHANGED
                        deltaSummary = "— Stable (0$unitStr)"
                    }
                } else {
                    deltaValue = null
                    if (recentResult.equals(prevResult, ignoreCase = true)) {
                        trend = ComparisonTrend.UNCHANGED
                        deltaSummary = "— Stable ($recentResult)"
                    } else {
                        if (prevStatus == LabResultStatus.NORMAL && recentStatus == LabResultStatus.ABNORMAL) {
                            trend = ComparisonTrend.ELEVATED
                            deltaSummary = "▲ Elevated ($prevResult → $recentResult)"
                        } else if (prevStatus == LabResultStatus.ABNORMAL && recentStatus == LabResultStatus.NORMAL) {
                            trend = ComparisonTrend.DECREASED
                            deltaSummary = "▼ Normalized ($prevResult → $recentResult)"
                        } else {
                            trend = ComparisonTrend.UNCHANGED
                            deltaSummary = "$prevResult → $recentResult"
                        }
                    }
                }

                val transition = buildStatusTransition(prevStatus, recentStatus)

                comparisons.add(
                    LabTestComparison(
                        testName = testName,
                        category = recentItem.category,
                        recentResult = recentResult,
                        recentDate = recentDate,
                        recentNormalRange = recentRange,
                        recentNotes = recentItem.notes,
                        recentStatus = recentStatus,
                        recentNumericValue = recentNum,
                        previousResult = prevResult,
                        previousDate = prevDate,
                        previousNormalRange = prevRange,
                        previousNotes = prevItem.notes,
                        previousStatus = prevStatus,
                        previousNumericValue = prevNum,
                        trend = trend,
                        deltaSummary = deltaSummary,
                        deltaValue = deltaValue,
                        unit = unit,
                        statusTransition = transition,
                        totalHistoricalRecords = records.size,
                        history = historyEntries
                    )
                )
            }
        }

        // Sort comparison items: Items with comparison first (Elevated, Decreased, Unchanged), then Baseline
        return comparisons.sortedWith(
            compareBy<LabTestComparison> {
                when (it.trend) {
                    ComparisonTrend.ELEVATED -> 0
                    ComparisonTrend.DECREASED -> 1
                    ComparisonTrend.UNCHANGED -> 2
                    ComparisonTrend.NO_PREVIOUS -> 3
                }
            }.thenBy { it.testName.lowercase(Locale.getDefault()) }
        )
    }

    private fun extractNumber(str: String): Double? {
        val match = NUMBER_REGEX.find(str) ?: return null
        return match.value.toDoubleOrNull()
    }

    private fun extractUnit(str: String): String {
        val match = UNIT_REGEX.find(str) ?: return ""
        return match.value.trim()
    }

    private fun buildStatusTransition(prev: LabResultStatus, recent: LabResultStatus): String {
        return when {
            prev == LabResultStatus.NORMAL && recent == LabResultStatus.NORMAL -> "Stable (Normal)"
            prev == LabResultStatus.ABNORMAL && recent == LabResultStatus.ABNORMAL -> "Consistently Out of Range"
            prev == LabResultStatus.NORMAL && recent == LabResultStatus.ABNORMAL -> "Normal → Out of Range ⚠️"
            prev == LabResultStatus.ABNORMAL && recent == LabResultStatus.NORMAL -> "Out of Range → Normal ✅ (Improved)"
            else -> if (recent == LabResultStatus.NORMAL) "Normal" else if (recent == LabResultStatus.ABNORMAL) "Out of Range" else "Recorded"
        }
    }
}

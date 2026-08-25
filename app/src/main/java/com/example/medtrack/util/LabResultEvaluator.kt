package com.example.medtrack.util

import androidx.compose.ui.graphics.Color

enum class LabResultStatus {
    NORMAL,
    ABNORMAL,
    UNSPECIFIED
}

object LabResultEvaluator {

    /**
     * Evaluates a lab test result against a normal/reference range string.
     * Returns NORMAL (Green), ABNORMAL (Red), or UNSPECIFIED (Default text color).
     */
    fun evaluate(result: String?, normalRange: String?): LabResultStatus {
        if (result.isNullOrBlank() || normalRange.isNullOrBlank()) {
            return LabResultStatus.UNSPECIFIED
        }

        val cleanResult = result.trim()
        val cleanRange = normalRange.trim()

        val lowerResult = cleanResult.lowercase()
        val lowerRange = cleanRange.lowercase()

        // 1. Qualitative / text-based findings check
        if (lowerResult.contains("negative") || lowerResult.contains("non-reactive") ||
            lowerResult.contains("nonreactive") || lowerResult.contains("unremarkable") ||
            lowerResult.contains("normal sinus") || lowerResult.contains("clear") ||
            lowerResult == "neg"
        ) {
            if (!lowerResult.contains("abnormal") && !lowerResult.contains("positive")) {
                return LabResultStatus.NORMAL
            }
        }

        if (lowerResult.contains("positive") || lowerResult.contains("reactive") ||
            lowerResult.contains("abnormal") || lowerResult.contains("critical") ||
            lowerResult.contains("high") || lowerResult.contains("elevated") ||
            lowerResult.contains("detected")
        ) {
            if (lowerRange.contains("neg") || lowerRange.contains("non-reactive") || lowerRange.contains("normal")) {
                return LabResultStatus.ABNORMAL
            }
        }

        // 2. Extract numeric value from result
        val resultNumber = extractFirstNumber(cleanResult) ?: return LabResultStatus.UNSPECIFIED

        // 3. Evaluate against numeric rules in the normal range
        return evaluateNumericRange(resultNumber, cleanRange)
    }

    private fun extractFirstNumber(str: String): Double? {
        val regex = Regex("""[-+]?[0-9]+(?:\.[0-9]+)?""")
        val match = regex.find(str) ?: return null
        return match.value.toDoubleOrNull()
    }

    private fun evaluateNumericRange(value: Double, rangeStr: String): LabResultStatus {
        val lowerRange = rangeStr.lowercase()

        // Case A: Less than rule: "< 200", "<= 200", "< 5.7%", "<100 mg/dL"
        val lessThanRegex = Regex("""(?:<|<=|less than|under)\s*([0-9]+(?:\.[0-9]+)?)""")
        val lessMatch = lessThanRegex.find(lowerRange)
        if (lessMatch != null) {
            val limit = lessMatch.groupValues[1].toDoubleOrNull()
            if (limit != null) {
                return if (value <= limit) LabResultStatus.NORMAL else LabResultStatus.ABNORMAL
            }
        }

        // Case B: Greater than rule: "> 40", ">= 40", "> 50"
        val greaterMatches = Regex("""(?:>|>=|greater than|over|above)\s*([0-9]+(?:\.[0-9]+)?)""").findAll(lowerRange).toList()
        if (greaterMatches.isNotEmpty()) {
            val limits = greaterMatches.mapNotNull { it.groupValues[1].toDoubleOrNull() }
            if (limits.isNotEmpty()) {
                val minLimit = limits.minOrNull() ?: limits[0]
                return if (value >= minLimit) LabResultStatus.NORMAL else LabResultStatus.ABNORMAL
            }
        }

        // Case C: Min - Max range: "13.5 - 17.5", "70 - 99", "3.5 to 7.2", "135-145", "0.7 - 1.3"
        val rangeRegex = Regex("""([0-9]+(?:\.[0-9]+)?)\s*(?:-|–|—|to)\s*([0-9]+(?:\.[0-9]+)?)""")
        val rangeMatch = rangeRegex.find(lowerRange)
        if (rangeMatch != null) {
            val min = rangeMatch.groupValues[1].toDoubleOrNull()
            val max = rangeMatch.groupValues[2].toDoubleOrNull()
            if (min != null && max != null && min <= max) {
                return if (value in min..max) LabResultStatus.NORMAL else LabResultStatus.ABNORMAL
            }
        }

        return LabResultStatus.UNSPECIFIED
    }

    /**
     * Resolves text and container colors based on result evaluation.
     */
    fun getStatusColors(
        status: LabResultStatus,
        defaultTextColor: Color,
        normalColor: Color = Color(0xFF2E7D32),
        abnormalColor: Color = Color(0xFFD32F2F)
    ): ResultColorSet {
        return when (status) {
            LabResultStatus.NORMAL -> ResultColorSet(
                textColor = normalColor,
                backgroundColor = Color(0xFFE8F5E9),
                badgeText = "Normal",
                badgeColor = normalColor,
                badgeBg = Color(0xFFC8E6C9)
            )
            LabResultStatus.ABNORMAL -> ResultColorSet(
                textColor = abnormalColor,
                backgroundColor = Color(0xFFFFEBEE),
                badgeText = "Out of Range",
                badgeColor = abnormalColor,
                badgeBg = Color(0xFFFFCDD2)
            )
            LabResultStatus.UNSPECIFIED -> ResultColorSet(
                textColor = defaultTextColor,
                backgroundColor = Color.Transparent,
                badgeText = null,
                badgeColor = defaultTextColor,
                badgeBg = Color.Transparent
            )
        }
    }
}

data class ResultColorSet(
    val textColor: Color,
    val backgroundColor: Color,
    val badgeText: String?,
    val badgeColor: Color,
    val badgeBg: Color
)

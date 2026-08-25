package com.example.medtrack.util

import androidx.compose.ui.graphics.Color
import java.util.Locale

enum class AsianBmiCategory(
    val label: String,
    val rangeDescription: String,
    val healthRisk: String,
    val primaryColor: Color,
    val containerColor: Color,
    val textColor: Color
) {
    UNDERWEIGHT(
        label = "Underweight",
        rangeDescription = "< 18.5",
        healthRisk = "Increased risk of nutritional deficiency and osteoporosis. Consider nutrient-rich dietary support.",
        primaryColor = Color(0xFF0288D1),
        containerColor = Color(0xFFE1F5FE),
        textColor = Color(0xFF01579B)
    ),
    NORMAL(
        label = "Normal (Healthy)",
        rangeDescription = "18.5 – 22.9",
        healthRisk = "Optimal healthy weight range for Asian adults. Lower risk of cardiovascular & metabolic conditions.",
        primaryColor = Color(0xFF2E7D32),
        containerColor = Color(0xFFE8F5E9),
        textColor = Color(0xFF1B5E20)
    ),
    OVERWEIGHT(
        label = "Overweight (At Risk)",
        rangeDescription = "23.0 – 24.9",
        healthRisk = "Increased risk of hypertension and type 2 diabetes in Asian adults. Diet & lifestyle adjustments recommended.",
        primaryColor = Color(0xFFF57F17),
        containerColor = Color(0xFFFFFDE7),
        textColor = Color(0xFFE65100)
    ),
    OBESE_CLASS_1(
        label = "Obese (Class I)",
        rangeDescription = "25.0 – 29.9",
        healthRisk = "High risk of metabolic syndrome, diabetes, fatty liver, and cardiovascular complications.",
        primaryColor = Color(0xFFE65100),
        containerColor = Color(0xFFFFF3E0),
        textColor = Color(0xFFBF360C)
    ),
    OBESE_CLASS_2(
        label = "Obese (Class II - Severe)",
        rangeDescription = "≥ 30.0",
        healthRisk = "Very high risk of severe health conditions. Clinical consultation and weight management strongly advised.",
        primaryColor = Color(0xFFC62828),
        containerColor = Color(0xFFFFEBEE),
        textColor = Color(0xFFB71C1C)
    );

    companion object {
        fun fromBmi(bmi: Double): AsianBmiCategory {
            return when {
                bmi < 18.5 -> UNDERWEIGHT
                bmi < 23.0 -> NORMAL
                bmi < 25.0 -> OVERWEIGHT
                bmi < 30.0 -> OBESE_CLASS_1
                else -> OBESE_CLASS_2
            }
        }
    }
}

object AsianBmiCalculator {
    fun calculateBmi(weightKg: Double, heightCm: Double): Double? {
        if (weightKg <= 0 || heightCm <= 0) return null
        val heightM = heightCm / 100.0
        val bmi = weightKg / (heightM * heightM)
        return if (bmi.isFinite() && bmi > 0) bmi else null
    }

    fun getHealthyWeightRange(heightCm: Double): Pair<Double, Double>? {
        if (heightCm <= 0) return null
        val heightM = heightCm / 100.0
        val minWeight = 18.5 * (heightM * heightM)
        val maxWeight = 22.9 * (heightM * heightM)
        return Pair(minWeight, maxWeight)
    }

    fun formatBmi(bmi: Double): String {
        return String.format(Locale.US, "%.1f", bmi)
    }

    fun formatWeight(weight: Double): String {
        return if (weight % 1.0 == 0.0) {
            String.format(Locale.US, "%.0f", weight)
        } else {
            String.format(Locale.US, "%.1f", weight)
        }
    }

    fun formatHeight(height: Double): String {
        return if (height % 1.0 == 0.0) {
            String.format(Locale.US, "%.0f", height)
        } else {
            String.format(Locale.US, "%.1f", height)
        }
    }
}

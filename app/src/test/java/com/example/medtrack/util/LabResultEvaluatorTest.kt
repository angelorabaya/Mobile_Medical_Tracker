package com.example.medtrack.util

import org.junit.Assert.assertEquals
import org.junit.Test

class LabResultEvaluatorTest {

    @Test
    fun evaluate_lessThanRule_withinRange_returnsNormal() {
        val status = LabResultEvaluator.evaluate(
            result = "180 mg/dL",
            normalRange = "< 200 mg/dL"
        )
        assertEquals(LabResultStatus.NORMAL, status)
    }

    @Test
    fun evaluate_lessThanRule_aboveRange_returnsAbnormal() {
        val status = LabResultEvaluator.evaluate(
            result = "235 mg/dL",
            normalRange = "< 200 mg/dL"
        )
        assertEquals(LabResultStatus.ABNORMAL, status)
    }

    @Test
    fun evaluate_greaterThanRule_withinRange_returnsNormal() {
        val status = LabResultEvaluator.evaluate(
            result = "48 mg/dL",
            normalRange = "> 40 mg/dL (Male), > 50 mg/dL (Female)"
        )
        assertEquals(LabResultStatus.NORMAL, status)
    }

    @Test
    fun evaluate_greaterThanRule_belowRange_returnsAbnormal() {
        val status = LabResultEvaluator.evaluate(
            result = "32 mg/dL",
            normalRange = "> 40 mg/dL"
        )
        assertEquals(LabResultStatus.ABNORMAL, status)
    }

    @Test
    fun evaluate_minMaxRange_withinRange_returnsNormal() {
        val status = LabResultEvaluator.evaluate(
            result = "Hemoglobin: 14.2 g/dL",
            normalRange = "13.5 - 17.5 g/dL"
        )
        assertEquals(LabResultStatus.NORMAL, status)
    }

    @Test
    fun evaluate_minMaxRange_belowRange_returnsAbnormal() {
        val status = LabResultEvaluator.evaluate(
            result = "11.8 g/dL",
            normalRange = "13.5 - 17.5 g/dL"
        )
        assertEquals(LabResultStatus.ABNORMAL, status)
    }

    @Test
    fun evaluate_minMaxRange_aboveRange_returnsAbnormal() {
        val status = LabResultEvaluator.evaluate(
            result = "FBS: 115 mg/dL",
            normalRange = "70 - 99 mg/dL"
        )
        assertEquals(LabResultStatus.ABNORMAL, status)
    }

    @Test
    fun evaluate_qualitativeNegative_returnsNormal() {
        val status = LabResultEvaluator.evaluate(
            result = "Negative",
            normalRange = "Protein: Neg, Sugar: Neg"
        )
        assertEquals(LabResultStatus.NORMAL, status)
    }

    @Test
    fun evaluate_qualitativePositive_returnsAbnormal() {
        val status = LabResultEvaluator.evaluate(
            result = "Protein: Positive (+2)",
            normalRange = "Color: Straw/Yellow, Protein: Neg, Sugar: Neg"
        )
        assertEquals(LabResultStatus.ABNORMAL, status)
    }

    @Test
    fun evaluate_unspecified_returnsUnspecified() {
        val status = LabResultEvaluator.evaluate(
            result = "",
            normalRange = "13.5 - 17.5"
        )
        assertEquals(LabResultStatus.UNSPECIFIED, status)
    }
}

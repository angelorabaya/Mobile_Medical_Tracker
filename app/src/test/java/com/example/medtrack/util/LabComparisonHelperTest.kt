package com.example.medtrack.util

import com.example.medtrack.data.entity.LabTest
import com.example.medtrack.data.entity.LabTestItem
import com.example.medtrack.data.entity.LabTestWithItems
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class LabComparisonHelperTest {

    @Test
    fun generateComparativePanels_computesElevatedAndDecreasedResults() {
        val oldVisit = LabTestWithItems(
            labTest = LabTest(id = 1, patientId = 1, testDate = "2026-01-10", createdAt = 1000L),
            items = listOf(
                LabTestItem(id = 1, labTestId = 1, testName = "Fasting Blood Sugar (FBS)", category = "Blood Work", results = "95 mg/dL", normalRange = "70 - 99 mg/dL"),
                LabTestItem(id = 2, labTestId = 1, testName = "Total Cholesterol", category = "Blood Work", results = "220 mg/dL", normalRange = "< 200 mg/dL"),
                LabTestItem(id = 3, labTestId = 1, testName = "Serum Creatinine", category = "Blood Work", results = "1.0 mg/dL", normalRange = "0.7 - 1.3 mg/dL")
            )
        )

        val recentVisit = LabTestWithItems(
            labTest = LabTest(id = 2, patientId = 1, testDate = "2026-08-25", createdAt = 2000L),
            items = listOf(
                LabTestItem(id = 4, labTestId = 2, testName = "Fasting Blood Sugar (FBS)", category = "Blood Work", results = "110 mg/dL", normalRange = "70 - 99 mg/dL"),
                LabTestItem(id = 5, labTestId = 2, testName = "Total Cholesterol", category = "Blood Work", results = "185 mg/dL", normalRange = "< 200 mg/dL"),
                LabTestItem(id = 6, labTestId = 2, testName = "Serum Creatinine", category = "Blood Work", results = "1.0 mg/dL", normalRange = "0.7 - 1.3 mg/dL"),
                LabTestItem(id = 7, labTestId = 2, testName = "HbA1c", category = "Blood Work", results = "5.6%", normalRange = "< 5.7%")
            )
        )

        val panels = LabComparisonHelper.generateComparativePanels(listOf(oldVisit, recentVisit))
        assertEquals(4, panels.size)

        // FBS should be ELEVATED (+15)
        val fbs = panels.find { it.testName.contains("Fasting Blood Sugar") }
        assertNotNull(fbs)
        assertEquals(ComparisonTrend.ELEVATED, fbs!!.trend)
        assertEquals(15.0, fbs.deltaValue!!, 0.001)
        assertEquals("95 mg/dL", fbs.previousResult)
        assertEquals("110 mg/dL", fbs.recentResult)

        // Cholesterol should be DECREASED (-35)
        val chol = panels.find { it.testName == "Total Cholesterol" }
        assertNotNull(chol)
        assertEquals(ComparisonTrend.DECREASED, chol!!.trend)
        assertEquals(-35.0, chol.deltaValue!!, 0.001)

        // Creatinine should be UNCHANGED (0)
        val creat = panels.find { it.testName == "Serum Creatinine" }
        assertNotNull(creat)
        assertEquals(ComparisonTrend.UNCHANGED, creat!!.trend)
        assertEquals(0.0, creat.deltaValue!!, 0.001)

        // HbA1c should be NO_PREVIOUS (Baseline)
        val hba1c = panels.find { it.testName == "HbA1c" }
        assertNotNull(hba1c)
        assertEquals(ComparisonTrend.NO_PREVIOUS, hba1c!!.trend)
    }
}

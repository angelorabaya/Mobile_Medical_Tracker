package com.example.medtrack.data.entity

import org.junit.Assert.assertEquals
import org.junit.Test

class LabTestWithItemsTest {

    @Test
    fun displayTitle_explicitTitle_returnsExplicitTitle() {
        val labTest = LabTest(
            id = 1,
            patientId = 1,
            title = "Annual Executive Checkup",
            testDate = "2026-08-25"
        )
        val items = listOf(
            LabTestItem(id = 1, labTestId = 1, testName = "CBC"),
            LabTestItem(id = 2, labTestId = 1, testName = "Lipid Profile")
        )
        val testWithItems = LabTestWithItems(labTest, items)
        assertEquals("Annual Executive Checkup", testWithItems.displayTitle)
    }

    @Test
    fun displayTitle_blankTitleSingleItem_returnsItemName() {
        val labTest = LabTest(
            id = 1,
            patientId = 1,
            title = "",
            testDate = "2026-08-25"
        )
        val items = listOf(
            LabTestItem(id = 1, labTestId = 1, testName = "Complete Blood Count")
        )
        val testWithItems = LabTestWithItems(labTest, items)
        assertEquals("Complete Blood Count", testWithItems.displayTitle)
    }

    @Test
    fun displayTitle_blankTitleTwoItems_returnsBothNames() {
        val labTest = LabTest(
            id = 1,
            patientId = 1,
            title = "",
            testDate = "2026-08-25"
        )
        val items = listOf(
            LabTestItem(id = 1, labTestId = 1, testName = "CBC"),
            LabTestItem(id = 2, labTestId = 1, testName = "FBS")
        )
        val testWithItems = LabTestWithItems(labTest, items)
        assertEquals("CBC & FBS", testWithItems.displayTitle)
    }

    @Test
    fun displayTitle_blankTitleMultipleItems_returnsFirstPlusCount() {
        val labTest = LabTest(
            id = 1,
            patientId = 1,
            title = "",
            testDate = "2026-08-25"
        )
        val items = listOf(
            LabTestItem(id = 1, labTestId = 1, testName = "CBC"),
            LabTestItem(id = 2, labTestId = 1, testName = "FBS"),
            LabTestItem(id = 3, labTestId = 1, testName = "Lipid Profile")
        )
        val testWithItems = LabTestWithItems(labTest, items)
        assertEquals("CBC (+2 more)", testWithItems.displayTitle)
    }
}

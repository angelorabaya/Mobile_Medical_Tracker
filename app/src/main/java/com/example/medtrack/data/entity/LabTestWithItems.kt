package com.example.medtrack.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class LabTestWithItems(
    @Embedded val labTest: LabTest,
    @Relation(
        parentColumn = "id",
        entityColumn = "labTestId"
    )
    val items: List<LabTestItem>
) {
    val displayTitle: String
        get() {
            if (labTest.title.isNotBlank()) return labTest.title
            return when {
                items.isEmpty() -> "Lab Test"
                items.size == 1 -> items.first().testName
                items.size == 2 -> "${items[0].testName} & ${items[1].testName}"
                else -> "${items[0].testName} (+${items.size - 1} more)"
            }
        }
}

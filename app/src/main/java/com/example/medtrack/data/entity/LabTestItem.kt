package com.example.medtrack.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lab_test_items",
    foreignKeys = [ForeignKey(
        entity = LabTest::class,
        parentColumns = ["id"],
        childColumns = ["labTestId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("labTestId")]
)
data class LabTestItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val labTestId: Int,
    val testName: String,
    val category: String = "Blood Work",
    val results: String = "",
    val normalRange: String = "",
    val notes: String = ""
)

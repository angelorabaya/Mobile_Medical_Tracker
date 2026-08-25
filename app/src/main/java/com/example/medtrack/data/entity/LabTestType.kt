package com.example.medtrack.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lab_test_types")
data class LabTestType(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val defaultCategory: String = "Blood Work",
    val defaultNormalRange: String = "",
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

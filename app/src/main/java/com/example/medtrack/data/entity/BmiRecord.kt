package com.example.medtrack.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bmi_records")
data class BmiRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientId: Int = 0,
    val weightKg: Double,
    val heightCm: Double,
    val bmi: Double,
    val category: String,
    val calculatedAt: Long = System.currentTimeMillis()
)

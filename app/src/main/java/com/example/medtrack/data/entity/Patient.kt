package com.example.medtrack.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val dateOfBirth: String,
    val gender: String,
    val bloodType: String = "",
    val allergies: String = "",
    val emergencyContact: String = "",
    val photoUri: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

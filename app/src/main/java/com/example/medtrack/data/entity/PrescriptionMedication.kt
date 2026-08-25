package com.example.medtrack.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "prescription_medications",
    foreignKeys = [ForeignKey(
        entity = Prescription::class,
        parentColumns = ["id"],
        childColumns = ["prescriptionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("prescriptionId")]
)
data class PrescriptionMedication(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val prescriptionId: Int,
    val medicationName: String,
    val dosage: String = "",
    val frequency: String = "",
    val duration: String = "",
    val instructions: String = ""
)

package com.example.medtrack.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medicine_reminders",
    foreignKeys = [ForeignKey(
        entity = Prescription::class,
        parentColumns = ["id"],
        childColumns = ["prescriptionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("prescriptionId")]
)
data class MedicineReminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val prescriptionId: Int,
    val reminderTime: String,
    val isEnabled: Boolean = true,
    val label: String = ""
)

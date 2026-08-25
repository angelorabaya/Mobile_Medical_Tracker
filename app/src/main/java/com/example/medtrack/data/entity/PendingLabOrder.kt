package com.example.medtrack.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pending_lab_orders",
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("patientId")]
)
data class PendingLabOrder(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val patientId: Int,
    val testName: String,
    val scheduledDate: String, // YYYY-MM-DD
    val scheduledTime: String = "07:30", // HH:mm
    val facilityName: String = "",
    val fastingInstructions: String = "",
    val notes: String = "",
    val isReminderEnabled: Boolean = true,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

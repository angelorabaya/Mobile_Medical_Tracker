package com.example.medtrack.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class PrescriptionWithMedications(
    @Embedded val prescription: Prescription,
    @Relation(
        parentColumn = "id",
        entityColumn = "prescriptionId"
    )
    val medications: List<PrescriptionMedication>
) {
    val displayTitle: String
        get() {
            if (prescription.title.isNotBlank()) return prescription.title
            return when {
                medications.isEmpty() -> "Prescription"
                medications.size == 1 -> medications.first().medicationName
                medications.size == 2 -> "${medications[0].medicationName} & ${medications[1].medicationName}"
                else -> "${medications[0].medicationName} (+${medications.size - 1} more)"
            }
        }
}

package com.example.medtrack.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FrequencyHelperTest {

    @Test
    fun formatSchedule_morningOnly() {
        assertEquals("1-0-0 (Morning only)", FrequencyHelper.formatSchedule("1", "0", "0"))
    }

    @Test
    fun formatSchedule_eveningOnly() {
        assertEquals("0-0-1 (Evening / Bedtime only)", FrequencyHelper.formatSchedule("0", "0", "1"))
    }

    @Test
    fun formatSchedule_allThree() {
        assertEquals("1-1-1 (Morning, Noon & Evening)", FrequencyHelper.formatSchedule("1", "1", "1"))
    }

    @Test
    fun formatSchedule_normalizesNonOnesToZero() {
        // Only an exact "1" counts as taken; everything else normalizes to "0".
        assertEquals("0-0-0 (As needed / As prescribed)", FrequencyHelper.formatSchedule("2", "x", "0"))
    }

    @Test
    fun parseSchedule_numeric() {
        assertEquals(Triple("1", "0", "1"), FrequencyHelper.parseSchedule("1-0-1 (Morning & Evening)"))
    }

    @Test
    fun parseSchedule_legacyBedtime() {
        assertEquals(Triple("0", "0", "1"), FrequencyHelper.parseSchedule("Take at bedtime"))
    }

    @Test
    fun parseSchedule_legacyTwice() {
        assertEquals(Triple("1", "0", "1"), FrequencyHelper.parseSchedule("twice a day"))
    }

    @Test
    fun parseSchedule_legacyMorningOnly() {
        assertEquals(Triple("1", "0", "0"), FrequencyHelper.parseSchedule("Morning only"))
    }
}

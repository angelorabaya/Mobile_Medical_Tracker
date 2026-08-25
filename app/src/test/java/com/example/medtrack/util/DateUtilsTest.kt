package com.example.medtrack.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class DateUtilsTest {

    @Test
    fun parseDateOrNull_standardIsoDate() {
        val parsed = DateUtils.parseDateOrNull("2026-08-25")
        assertEquals(LocalDate.of(2026, 8, 25), parsed)
    }

    @Test
    fun parseDateOrNull_singleDigitMonthDay() {
        val parsed = DateUtils.parseDateOrNull("2026-8-5")
        assertEquals(LocalDate.of(2026, 8, 5), parsed)
    }

    @Test
    fun parseDateOrNull_slashesFormat() {
        val parsed = DateUtils.parseDateOrNull("2026/08/25")
        assertEquals(LocalDate.of(2026, 8, 25), parsed)
    }

    @Test
    fun parseDateOrNull_usFormat() {
        val parsed = DateUtils.parseDateOrNull("08/25/2026")
        assertEquals(LocalDate.of(2026, 8, 25), parsed)
    }

    @Test
    fun parseDateOrNull_textMonthFormat() {
        val parsed = DateUtils.parseDateOrNull("Aug 25, 2026")
        assertEquals(LocalDate.of(2026, 8, 25), parsed)
    }

    @Test
    fun parseDateOrNull_invalidOrBlank_returnsNull() {
        assertNull(DateUtils.parseDateOrNull(""))
        assertNull(DateUtils.parseDateOrNull("invalid"))
        assertNull(DateUtils.parseDateOrNull(null))
    }

    @Test
    fun newestFirstComparator_sortsDatesCorrectly() {
        val dates = listOf(
            "2024-01-10",
            "2026-08-25",
            "2025-12-31",
            "2026-01-01",
            "2026-08-20"
        )

        val sorted = dates.sortedWith(DateUtils.newestFirstComparator)
        val expected = listOf(
            "2026-08-25",
            "2026-08-20",
            "2026-01-01",
            "2025-12-31",
            "2024-01-10"
        )

        assertEquals(expected, sorted)
    }
}

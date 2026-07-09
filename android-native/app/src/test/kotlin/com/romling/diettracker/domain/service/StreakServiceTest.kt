package com.romling.diettracker.domain.service

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class StreakServiceTest {
    private val service = StreakService()

    @Test
    fun summaryCountsCurrentBestAndActiveDays() {
        val summary = service.summary(
            listOf("2026-07-01", "2026-07-03", "2026-07-04", "2026-07-05"),
            LocalDate.parse("2026-07-05"),
        )

        assertEquals(3, summary.current)
        assertEquals(3, summary.best)
        assertEquals(4, summary.activeDays)
    }

    @Test
    fun currentStreakIsZeroWhenEndDateIsInactive() {
        val summary = service.summary(
            listOf("2026-07-01", "2026-07-02"),
            LocalDate.parse("2026-07-03"),
        )

        assertEquals(0, summary.current)
    }
}

package com.romling.diettracker.domain.service

import kotlin.test.Test
import kotlin.test.assertEquals

class StreakServiceTest {
    private val service = StreakService()

    @Test
    fun summaryCountsCurrentBestAndActiveDays() {
        val summary = service.summary(
            activeDays = listOf("2026-06-28", "2026-06-30", "2026-07-01", "2026-07-02"),
            endDay = "2026-07-02",
        )

        assertEquals(StreakSummary(current = 3, best = 3, activeDays = 4), summary)
    }
}

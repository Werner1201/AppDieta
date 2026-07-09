package com.romling.diettracker.domain.service

import java.time.LocalDate

class StreakService {
    fun summary(activeDateStrings: List<String>, endDate: LocalDate): StreakSummary {
        val activeDates = activeDateStrings.map(LocalDate::parse).toSet()
        var current = 0
        var cursor = endDate
        while (cursor in activeDates) {
            current += 1
            cursor = cursor.minusDays(1)
        }

        var best = 0
        var run = 0
        var previous: LocalDate? = null
        activeDates.sorted().forEach { day ->
            run = if (previous != null && day == previous!!.plusDays(1)) run + 1 else 1
            best = maxOf(best, run)
            previous = day
        }

        return StreakSummary(current = current, best = best, activeDays = activeDates.size)
    }
}

data class StreakSummary(
    val current: Int = 0,
    val best: Int = 0,
    val activeDays: Int = 0,
)

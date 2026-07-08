package com.romling.diettracker.domain.service

import java.time.LocalDate

class StreakService {
    fun summary(activeDays: Collection<String>, endDay: String): StreakSummary {
        val days = activeDays.mapTo(sortedSetOf()) { LocalDate.parse(it) }
        var current = 0
        var cursor = LocalDate.parse(endDay)
        while (cursor in days) {
            current += 1
            cursor = cursor.minusDays(1)
        }

        var best = 0
        var run = 0
        var previous: LocalDate? = null
        for (day in days) {
            run = if (previous != null && day == previous.plusDays(1)) run + 1 else 1
            best = maxOf(best, run)
            previous = day
        }
        return StreakSummary(current = current, best = best, activeDays = days.size)
    }
}

data class StreakSummary(
    val current: Int,
    val best: Int,
    val activeDays: Int,
)

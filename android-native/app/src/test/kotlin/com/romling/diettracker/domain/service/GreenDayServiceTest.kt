package com.romling.diettracker.domain.service

import com.romling.diettracker.data.local.entity.DiaryEntryEntity
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GreenDayServiceTest {
    private val service = GreenDayService()

    @Test
    fun greenDayRequiresEntryWithinKcalAndProteinGoal() {
        val entries = listOf(entry(kcal = 1800.0, protein = 92.0))

        assertTrue(service.isGreenDay(entries, dailyKcal = 2333.0, dailyProtein = 114.0))
        assertFalse(service.isGreenDay(emptyList(), dailyKcal = 2333.0, dailyProtein = 114.0))
        assertFalse(service.isGreenDay(listOf(entry(kcal = 2400.0, protein = 100.0)), 2333.0, 114.0))
        assertFalse(service.isGreenDay(listOf(entry(kcal = 1800.0, protein = 80.0)), 2333.0, 114.0))
    }
}

private fun entry(kcal: Double, protein: Double) = DiaryEntryEntity(
    date = "2026-07-01",
    mealType = "lunch",
    foodId = 1,
    foodNameSnapshot = "Peito de frango",
    quantity = 1.0,
    unitLabel = "100 g",
    gramsTotal = 100.0,
    kcal = kcal,
    carbs = 0.0,
    protein = protein,
    fat = 0.0,
)

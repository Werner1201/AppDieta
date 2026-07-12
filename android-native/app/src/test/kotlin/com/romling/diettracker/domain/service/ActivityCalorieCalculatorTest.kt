package com.romling.diettracker.domain.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ActivityCalorieCalculatorTest {
    @Test
    fun calculatesStrengthTrainingExample() {
        val kcal = ActivityCalorieCalculator.calculate(met = 4.5, weightKg = 108.0, durationMinutes = 60)
        assertEquals(510, kcal.toInt())
    }

    @Test
    fun calculatesWalkingExample() {
        val kcal = ActivityCalorieCalculator.calculate(met = 3.5, weightKg = 100.0, durationMinutes = 30)
        assertEquals(183, kcal.toInt())
    }

    @Test
    fun rejectsInvalidInputs() {
        assertFailsWith<IllegalArgumentException> {
            ActivityCalorieCalculator.calculate(met = 0.0, weightKg = 100.0, durationMinutes = 30)
        }
    }
}

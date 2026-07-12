package com.romling.diettracker.domain.service

object ActivityCalorieCalculator {
    fun calculate(met: Double, weightKg: Double, durationMinutes: Int): Double {
        require(met > 0) { "MET must be positive." }
        require(weightKg > 0) { "Weight must be positive." }
        require(durationMinutes > 0) { "Duration must be positive." }
        return met * 3.5 * weightKg / 200.0 * durationMinutes
    }
}

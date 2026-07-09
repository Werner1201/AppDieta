package com.romling.diettracker.data.repository

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GoalSettings(
    val dailyKcal: Double = 2333.0,
    val dailyCarbs: Double = 284.0,
    val dailyProtein: Double = 114.0,
    val dailyFat: Double = 75.0,
    val dailyWaterMl: Int = 2000,
    val defaultWeightKg: Double = 108.0,
    val weightGoalKg: Double = 80.0,
)

class SettingsRepository(private val preferences: SharedPreferences) {
    private val _settings = MutableStateFlow(read())
    val settings: StateFlow<GoalSettings> = _settings.asStateFlow()

    fun save(settings: GoalSettings) {
        preferences.edit()
            .putFloat(KEY_KCAL, settings.dailyKcal.toFloat())
            .putFloat(KEY_CARBS, settings.dailyCarbs.toFloat())
            .putFloat(KEY_PROTEIN, settings.dailyProtein.toFloat())
            .putFloat(KEY_FAT, settings.dailyFat.toFloat())
            .putInt(KEY_WATER, settings.dailyWaterMl)
            .putFloat(KEY_DEFAULT_WEIGHT, settings.defaultWeightKg.toFloat())
            .putFloat(KEY_WEIGHT_GOAL, settings.weightGoalKg.toFloat())
            .apply()
        _settings.value = settings
    }

    private fun read() = GoalSettings(
        dailyKcal = preferences.getFloat(KEY_KCAL, 2333f).toDouble(),
        dailyCarbs = preferences.getFloat(KEY_CARBS, 284f).toDouble(),
        dailyProtein = preferences.getFloat(KEY_PROTEIN, 114f).toDouble(),
        dailyFat = preferences.getFloat(KEY_FAT, 75f).toDouble(),
        dailyWaterMl = preferences.getInt(KEY_WATER, 2000),
        defaultWeightKg = preferences.getFloat(KEY_DEFAULT_WEIGHT, 108f).toDouble(),
        weightGoalKg = preferences.getFloat(KEY_WEIGHT_GOAL, 80f).toDouble(),
    )

    private companion object {
        const val KEY_KCAL = "daily_kcal"
        const val KEY_CARBS = "daily_carbs"
        const val KEY_PROTEIN = "daily_protein"
        const val KEY_FAT = "daily_fat"
        const val KEY_WATER = "daily_water_ml"
        const val KEY_DEFAULT_WEIGHT = "default_weight_kg"
        const val KEY_WEIGHT_GOAL = "weight_goal_kg"
    }
}

package com.romling.diettracker.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.romling.diettracker.data.local.entity.DiaryEntryEntity
import com.romling.diettracker.data.repository.DiaryRepository
import com.romling.diettracker.data.repository.WaterRepository
import com.romling.diettracker.data.repository.WeightRepository
import com.romling.diettracker.domain.service.GreenDayService
import java.time.LocalDate
import java.time.temporal.IsoFields
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class TodayViewModel(
    private val diaryRepository: DiaryRepository,
    private val waterRepository: WaterRepository,
    private val weightRepository: WeightRepository,
    private val greenDayService: GreenDayService = GreenDayService(),
    dateProvider: () -> LocalDate = { LocalDate.now() },
    private val dailyKcal: Double = 2333.0,
    private val dailyProtein: Double = 114.0,
    private val dailyWaterMl: Int = 2000,
    private val defaultWeightKg: Double = 108.0,
    private val weightGoalKg: Double = 80.0,
) : ViewModel() {
    private val day = dateProvider()
    private val dayString = day.toString()

    val state: StateFlow<TodayUiState> = combine(
        diaryRepository.entriesForDate(dayString),
        waterRepository.entriesForDate(dayString),
        weightRepository.entries(),
    ) { entries, waterEntries, weightEntries ->
        entries.toTodayState(
            waterMl = waterEntries.sumOf { it.amountMl },
            weightKg = weightEntries.firstOrNull()?.weightKg ?: defaultWeightKg,
        )
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyState())

    private fun List<DiaryEntryEntity>.toTodayState(waterMl: Int, weightKg: Double): TodayUiState {
        val totals = TodayNutritionTotals(
            kcal = sumOf { it.kcal },
            carbs = sumOf { it.carbs },
            protein = sumOf { it.protein },
            fat = sumOf { it.fat },
        )
        return emptyState().copy(
            totals = totals,
            entries = map {
                TodayEntrySummary(
                    id = it.id,
                    mealType = it.mealType,
                    name = it.foodNameSnapshot,
                    kcal = it.kcal,
                )
            },
            meals = defaultMeals().map { meal ->
                val mealEntries = filter { it.mealType == meal.key }
                meal.copy(
                    kcal = mealEntries.sumOf { it.kcal },
                    items = mealEntries.take(3).joinToString(", ") { it.foodNameSnapshot },
                )
            },
            remainingKcal = maxOf(0, (dailyKcal - totals.kcal).roundToInt()),
            isGreenDay = greenDayService.isGreenDay(this, dailyKcal, dailyProtein),
            water = TodayWaterSummary(consumedMl = waterMl, goalMl = dailyWaterMl),
            weight = TodayWeightSummary(currentKg = weightKg, goalKg = weightGoalKg),
        )
    }

    fun removeEntry(entryId: Long) {
        viewModelScope.launch {
            diaryRepository.deleteById(entryId)
        }
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            waterRepository.addWater(dayString, amountMl)
        }
    }

    fun removeLastWater() {
        viewModelScope.launch {
            waterRepository.deleteLastForDate(dayString)
        }
    }

    fun addWeight(weightKg: Double) {
        viewModelScope.launch {
            weightRepository.addWeight(dayString, weightKg)
        }
    }

    private fun emptyState() = TodayUiState(
        date = dayString,
        week = day.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR),
        dailyKcal = dailyKcal,
        dailyProtein = dailyProtein,
        water = TodayWaterSummary(goalMl = dailyWaterMl),
        weight = TodayWeightSummary(currentKg = defaultWeightKg, goalKg = weightGoalKg),
        remainingKcal = dailyKcal.roundToInt(),
    )
}

class TodayViewModelFactory(
    private val diaryRepository: DiaryRepository,
    private val waterRepository: WaterRepository,
    private val weightRepository: WeightRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TodayViewModel(diaryRepository, waterRepository, weightRepository) as T
    }
}

data class TodayUiState(
    val date: String,
    val week: Int,
    val dailyKcal: Double,
    val dailyProtein: Double,
    val totals: TodayNutritionTotals = TodayNutritionTotals(),
    val entries: List<TodayEntrySummary> = emptyList(),
    val meals: List<TodayMealSummary> = defaultMeals(),
    val water: TodayWaterSummary = TodayWaterSummary(),
    val weight: TodayWeightSummary = TodayWeightSummary(),
    val remainingKcal: Int,
    val isGreenDay: Boolean = false,
)

data class TodayWaterSummary(
    val consumedMl: Int = 0,
    val goalMl: Int = 2000,
) {
    val consumedLiters: Double = consumedMl / 1000.0
    val goalLiters: Double = goalMl / 1000.0
}

data class TodayWeightSummary(
    val currentKg: Double = 108.0,
    val goalKg: Double = 80.0,
)

data class TodayEntrySummary(
    val id: Long,
    val mealType: String,
    val name: String,
    val kcal: Double,
)

data class TodayNutritionTotals(
    val kcal: Double = 0.0,
    val carbs: Double = 0.0,
    val protein: Double = 0.0,
    val fat: Double = 0.0,
)

data class TodayMealSummary(
    val key: String,
    val label: String,
    val icon: String,
    val goalKcal: Int,
    val kcal: Double = 0.0,
    val items: String = "",
)

fun defaultMeals() = listOf(
    TodayMealSummary(key = "breakfast", label = "Café da manhã", icon = "☕", goalKcal = 816),
    TodayMealSummary(key = "lunch", label = "Almoço", icon = "🍲", goalKcal = 816),
    TodayMealSummary(key = "dinner", label = "Jantar", icon = "🥗", goalKcal = 700),
    TodayMealSummary(key = "snack", label = "Lanches", icon = "⌛", goalKcal = 250),
)

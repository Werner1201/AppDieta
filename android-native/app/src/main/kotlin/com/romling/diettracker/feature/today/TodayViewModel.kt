package com.romling.diettracker.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.romling.diettracker.data.local.entity.DiaryEntryEntity
import com.romling.diettracker.data.repository.DiaryRepository
import com.romling.diettracker.domain.service.GreenDayService
import java.time.LocalDate
import java.time.temporal.IsoFields
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.math.roundToInt

class TodayViewModel(
    diaryRepository: DiaryRepository,
    private val greenDayService: GreenDayService = GreenDayService(),
    dateProvider: () -> LocalDate = { LocalDate.now() },
    private val dailyKcal: Double = 2333.0,
    private val dailyProtein: Double = 114.0,
) : ViewModel() {
    private val day = dateProvider()
    private val dayString = day.toString()

    val state: StateFlow<TodayUiState> = diaryRepository.entriesForDate(dayString)
        .map { entries -> entries.toTodayState() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyState())

    private fun List<DiaryEntryEntity>.toTodayState(): TodayUiState {
        val totals = TodayNutritionTotals(
            kcal = sumOf { it.kcal },
            carbs = sumOf { it.carbs },
            protein = sumOf { it.protein },
            fat = sumOf { it.fat },
        )
        return emptyState().copy(
            totals = totals,
            remainingKcal = maxOf(0, (dailyKcal - totals.kcal).roundToInt()),
            isGreenDay = greenDayService.isGreenDay(this, dailyKcal, dailyProtein),
        )
    }

    private fun emptyState() = TodayUiState(
        date = dayString,
        week = day.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR),
        dailyKcal = dailyKcal,
        dailyProtein = dailyProtein,
        remainingKcal = dailyKcal.roundToInt(),
    )
}

data class TodayUiState(
    val date: String,
    val week: Int,
    val dailyKcal: Double,
    val dailyProtein: Double,
    val totals: TodayNutritionTotals = TodayNutritionTotals(),
    val remainingKcal: Int,
    val isGreenDay: Boolean = false,
)

data class TodayNutritionTotals(
    val kcal: Double = 0.0,
    val carbs: Double = 0.0,
    val protein: Double = 0.0,
    val fat: Double = 0.0,
)

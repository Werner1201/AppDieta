package com.romling.diettracker.feature.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.romling.diettracker.data.local.entity.DiaryEntryEntity
import com.romling.diettracker.data.local.entity.ActivityEntryEntity
import com.romling.diettracker.data.repository.ActivityRepository
import com.romling.diettracker.data.local.entity.WeightEntryEntity
import com.romling.diettracker.data.repository.DiaryRepository
import com.romling.diettracker.data.repository.DiaryBackupEntry
import com.romling.diettracker.data.repository.DEFAULT_CHAT_GPT_PROMPT
import com.romling.diettracker.data.repository.DEFAULT_CHAT_GPT_URL
import com.romling.diettracker.data.repository.GoalSettings
import com.romling.diettracker.data.repository.SettingsRepository
import com.romling.diettracker.data.repository.WaterRepository
import com.romling.diettracker.data.repository.WeightRepository
import com.romling.diettracker.domain.service.GreenDayService
import com.romling.diettracker.domain.service.StreakSummary
import com.romling.diettracker.domain.service.StreakService
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.IsoFields
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModel(
    private val diaryRepository: DiaryRepository,
    private val waterRepository: WaterRepository,
    private val weightRepository: WeightRepository,
    private val settingsRepository: SettingsRepository? = null,
    private val activityRepository: ActivityRepository? = null,
    private val greenDayService: GreenDayService = GreenDayService(),
    private val streakService: StreakService = StreakService(),
    dateProvider: () -> LocalDate = { LocalDate.now() },
    private val dailyKcal: Double = 2333.0,
    private val dailyProtein: Double = 114.0,
    private val dailyWaterMl: Int = 2000,
    private val defaultWeightKg: Double = 108.0,
    private val weightGoalKg: Double = 80.0,
) : ViewModel() {

    private val fallbackSettings = GoalSettings(
        dailyKcal = dailyKcal,
        dailyProtein = dailyProtein,
        dailyWaterMl = dailyWaterMl,
        defaultWeightKg = defaultWeightKg,
        weightGoalKg = weightGoalKg,
    )
    private val settingsFlow = settingsRepository?.settings ?: flowOf(fallbackSettings)
    private val today = dateProvider()
    private val _date = MutableStateFlow(today)
    val currentDate: StateFlow<LocalDate> = _date.asStateFlow()
    val frequentActivityNames: StateFlow<List<String>> =
        (activityRepository?.frequentNames() ?: flowOf(emptyList()))
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _calendarMonth = MutableStateFlow(YearMonth.from(today))

    val state: StateFlow<TodayUiState> = _date.flatMapLatest { date ->
        val dateString = date.toString()
        val diaryAndActivities = combine(
            diaryRepository.entriesForDate(dateString),
            activityRepository?.entriesForDate(dateString) ?: flowOf(emptyList()),
        ) { entries, activities -> entries to activities }
        combine(
            settingsFlow,
            diaryAndActivities,
            waterRepository.entriesForDate(dateString),
            weightRepository.entries(),
            diaryRepository.activeDates(),
        ) { settings, diaryAndActivities, waterEntries, weightEntries, activeDates ->
            val (entries, activities) = diaryAndActivities
            entries.toTodayState(
                date = date,
                waterMl = waterEntries.sumOf { it.amountMl },
                weightKg = weightEntries.firstOrNull()?.weightKg ?: settings.defaultWeightKg,
                weightHistory = weightEntries,
                streak = streakService.summary(activeDates, date),
                settings = settings,
                activities = activities,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyState(today))

    val calendarGreenDays: StateFlow<Map<LocalDate, Boolean>> = _calendarMonth.flatMapLatest { month ->
        combine(settingsFlow, diaryRepository.entriesForMonth(month.toString())) { settings, entries -> settings to entries }
    }.map { (settings, entries) ->
        entries.groupBy { it.date }
            .mapValues { (_, dayEntries) -> greenDayService.isGreenDay(dayEntries, settings.dailyKcal, settings.dailyProtein) }
            .mapKeys { (dateStr, _) -> LocalDate.parse(dateStr) }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    fun previousDay() { _date.update { it.minusDays(1) } }
    fun nextDay() { _date.update { it.plusDays(1) } }
    fun goToDate(date: LocalDate) { _date.value = date; _calendarMonth.value = YearMonth.from(date) }
    fun setCalendarMonth(yearMonth: YearMonth) { _calendarMonth.value = yearMonth }

    fun removeEntry(entryId: Long) {
        viewModelScope.launch { diaryRepository.deleteById(entryId) }
    }

    fun updateEntryGrams(entryId: Long, newGrams: Double) {
        viewModelScope.launch { diaryRepository.updateEntryGrams(entryId, newGrams) }
    }

    fun addWater(amountMl: Int) {
        viewModelScope.launch { waterRepository.addWater(_date.value.toString(), amountMl) }
    }

    fun removeLastWater() {
        viewModelScope.launch { waterRepository.deleteLastForDate(_date.value.toString()) }
    }

    fun addWeight(weightKg: Double) {
        viewModelScope.launch { weightRepository.addWeight(_date.value.toString(), weightKg) }
    }

    fun addActivity(
        name: String,
        icon: String,
        met: Double,
        durationMinutes: Int,
        distanceKm: Double? = null,
        note: String = "",
    ) {
        val repository = activityRepository ?: return
        viewModelScope.launch {
            repository.add(
                date = _date.value.toString(),
                name = name,
                icon = icon,
                met = met,
                durationMinutes = durationMinutes,
                weightKg = state.value.weight.currentKg,
                distanceKm = distanceKm,
                note = note,
            )
        }
    }

    fun removeActivity(id: Long) {
        viewModelScope.launch { activityRepository?.deleteById(id) }
    }

    fun saveGoals(settings: GoalSettings) {
        settingsRepository?.save(settings)
    }

    fun importBackup(entries: List<DiaryBackupEntry>, onDone: () -> Unit) {
        if (entries.isEmpty()) return
        viewModelScope.launch {
            diaryRepository.importBackup(entries)
            onDone()
        }
    }

    suspend fun exportJson(): String {
        val entries = diaryRepository.allEntries()
        val array = org.json.JSONArray()
        for (e in entries) {
            array.put(
                org.json.JSONObject()
                    .put("date", e.date)
                    .put("meal", e.mealType)
                    .put("name", e.foodNameSnapshot)
                    .put("grams", e.gramsTotal)
                    .put("kcal", e.kcal.toInt())
                    .put("carbs", e.carbs)
                    .put("protein", e.protein)
                    .put("fat", e.fat)
            )
        }
        return org.json.JSONObject()
            .put("app", "AppDieta")
            .put("exported_at", java.time.LocalDate.now().toString())
            .put("count", entries.size)
            .put("entries", array)
            .toString(2)
    }

    private fun List<DiaryEntryEntity>.toTodayState(
        date: LocalDate,
        waterMl: Int,
        weightKg: Double,
        weightHistory: List<WeightEntryEntity>,
        streak: StreakSummary,
        settings: GoalSettings,
        activities: List<ActivityEntryEntity>,
    ): TodayUiState {
        val totals = TodayNutritionTotals(
            kcal = sumOf { it.kcal },
            carbs = sumOf { it.carbs },
            protein = sumOf { it.protein },
            fat = sumOf { it.fat },
        )
        return emptyState(date, settings).copy(
            totals = totals,
            entries = map {
                TodayEntrySummary(
                    id = it.id,
                    mealType = it.mealType,
                    name = it.foodNameSnapshot,
                    kcal = it.kcal,
                    carbs = it.carbs,
                    protein = it.protein,
                    fat = it.fat,
                    gramsTotal = it.gramsTotal,
                )
            },
            meals = defaultMeals(settings.dailyKcal).map { meal ->
                val mealEntries = filter { it.mealType == meal.key }
                meal.copy(
                    kcal = mealEntries.sumOf { it.kcal },
                    items = mealEntries.take(3).joinToString(", ") { it.foodNameSnapshot },
                )
            },
            activities = activities.map {
                TodayActivitySummary(it.id, it.name, it.icon, it.durationMinutes, it.kcal)
            },
            spentKcal = activities.sumOf { it.kcal },
            remainingKcal = calculateRemainingKcal(
                settings.dailyKcal,
                totals.kcal,
                activities.sumOf { it.kcal },
            ),
            isGreenDay = greenDayService.isGreenDay(this, settings.dailyKcal, settings.dailyProtein),
            water = TodayWaterSummary(consumedMl = waterMl, goalMl = settings.dailyWaterMl),
            weight = TodayWeightSummary(currentKg = weightKg, goalKg = settings.weightGoalKg),
            weightHistory = weightHistory.map { TodayWeightEntry(date = it.date, kg = it.weightKg) },
            streak = TodayStreakSummary(current = streak.current, best = streak.best, activeDays = streak.activeDays),
        )
    }

    private fun emptyState(date: LocalDate, settings: GoalSettings = fallbackSettings) = TodayUiState(
        date = date.toString(),
        isToday = date == today,
        week = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR),
        dailyKcal = settings.dailyKcal,
        dailyCarbs = settings.dailyCarbs,
        dailyProtein = settings.dailyProtein,
        dailyFat = settings.dailyFat,
        chatGptUrl = settings.chatGptUrl,
        chatGptPrompt = settings.chatGptPrompt,
        meals = defaultMeals(settings.dailyKcal),
        water = TodayWaterSummary(goalMl = settings.dailyWaterMl),
        weight = TodayWeightSummary(currentKg = settings.defaultWeightKg, goalKg = settings.weightGoalKg),
        remainingKcal = settings.dailyKcal.roundToInt(),
    )
}

class TodayViewModelFactory(
    private val diaryRepository: DiaryRepository,
    private val waterRepository: WaterRepository,
    private val weightRepository: WeightRepository,
    private val settingsRepository: SettingsRepository? = null,
    private val activityRepository: ActivityRepository? = null,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        TodayViewModel(
            diaryRepository,
            waterRepository,
            weightRepository,
            settingsRepository,
            activityRepository,
        ) as T
}

data class TodayUiState(
    val date: String,
    val isToday: Boolean = true,
    val week: Int,
    val dailyKcal: Double,
    val dailyCarbs: Double = 284.0,
    val dailyProtein: Double,
    val dailyFat: Double = 75.0,
    val chatGptUrl: String = DEFAULT_CHAT_GPT_URL,
    val chatGptPrompt: String = DEFAULT_CHAT_GPT_PROMPT,
    val totals: TodayNutritionTotals = TodayNutritionTotals(),
    val entries: List<TodayEntrySummary> = emptyList(),
    val activities: List<TodayActivitySummary> = emptyList(),
    val spentKcal: Double = 0.0,
    val meals: List<TodayMealSummary> = defaultMeals(),
    val water: TodayWaterSummary = TodayWaterSummary(),
    val weight: TodayWeightSummary = TodayWeightSummary(),
    val weightHistory: List<TodayWeightEntry> = emptyList(),
    val streak: TodayStreakSummary = TodayStreakSummary(),
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

data class TodayWeightEntry(
    val date: String,
    val kg: Double,
)

data class TodayStreakSummary(
    val current: Int = 0,
    val best: Int = 0,
    val activeDays: Int = 0,
)

data class TodayEntrySummary(
    val id: Long,
    val mealType: String,
    val name: String,
    val kcal: Double,
    val carbs: Double = 0.0,
    val protein: Double = 0.0,
    val fat: Double = 0.0,
    val gramsTotal: Double = 0.0,
)

data class TodayNutritionTotals(
    val kcal: Double = 0.0,
    val carbs: Double = 0.0,
    val protein: Double = 0.0,
    val fat: Double = 0.0,
)

data class TodayActivitySummary(
    val id: Long,
    val name: String,
    val icon: String,
    val durationMinutes: Int,
    val kcal: Double,
)

fun calculateRemainingKcal(goal: Double, consumed: Double, spent: Double): Int =
    maxOf(0, (goal - consumed + spent).roundToInt())

data class TodayMealSummary(
    val key: String,
    val label: String,
    val icon: String,
    val goalKcal: Int,
    val kcal: Double = 0.0,
    val items: String = "",
)

private const val MEAL_SHARE_TOTAL = 2582.0

fun defaultMeals(dailyKcal: Double = 2333.0): List<TodayMealSummary> {
    fun share(portion: Int) = (dailyKcal * portion / MEAL_SHARE_TOTAL).roundToInt()
    return listOf(
        TodayMealSummary(key = "breakfast", label = "Café da manhã", icon = "☕", goalKcal = share(816)),
        TodayMealSummary(key = "lunch", label = "Almoço", icon = "🍲", goalKcal = share(816)),
        TodayMealSummary(key = "dinner", label = "Jantar", icon = "🥗", goalKcal = share(700)),
        TodayMealSummary(key = "snack", label = "Lanches", icon = "⌛", goalKcal = share(250)),
    )
}

package com.romling.diettracker.feature.today

import com.romling.diettracker.data.local.dao.DiaryEntryDao
import com.romling.diettracker.data.local.dao.WaterEntryDao
import com.romling.diettracker.data.local.dao.WeightEntryDao
import com.romling.diettracker.data.local.entity.DiaryEntryEntity
import com.romling.diettracker.data.local.entity.WaterEntryEntity
import com.romling.diettracker.data.local.entity.WeightEntryEntity
import com.romling.diettracker.data.repository.DiaryRepository
import com.romling.diettracker.data.repository.WaterRepository
import com.romling.diettracker.data.repository.WeightRepository
import java.time.LocalDate
import org.json.JSONObject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun stateSummarizesTodayEntries() = runTest(dispatcher) {
        val viewModel = TodayViewModel(
            diaryRepository = DiaryRepository(FakeDiaryEntryDao(listOf(entry(kcal = 1800.0, protein = 92.0)))),
            waterRepository = WaterRepository(FakeWaterEntryDao()),
            weightRepository = WeightRepository(FakeWeightEntryDao()),
            dateProvider = { LocalDate.parse("2026-07-01") },
        )

        advanceUntilIdle()

        assertEquals("2026-07-01", viewModel.state.value.date)
        assertEquals(27, viewModel.state.value.week)
        assertEquals(1800.0, viewModel.state.value.totals.kcal)
        assertEquals(533, viewModel.state.value.remainingKcal)
        assertTrue(viewModel.state.value.isGreenDay)
        assertEquals(4, viewModel.state.value.meals.size)
        assertEquals("Peito de frango", viewModel.state.value.entries.single().name)
        assertEquals(1800.0, viewModel.state.value.meals.single { it.key == "lunch" }.kcal)
    }

    @Test
    fun stateUpdatesWhenDiaryEntriesChange() = runTest(dispatcher) {
        val dao = FakeDiaryEntryDao(emptyList())
        val viewModel = TodayViewModel(
            diaryRepository = DiaryRepository(dao),
            waterRepository = WaterRepository(FakeWaterEntryDao()),
            weightRepository = WeightRepository(FakeWeightEntryDao()),
            dateProvider = { LocalDate.parse("2026-07-01") },
        )

        advanceUntilIdle()
        assertEquals(0.0, viewModel.state.value.totals.kcal)

        dao.entries.value = listOf(entry(kcal = 237.0, protein = 12.4))
        advanceUntilIdle()

        assertEquals(237.0, viewModel.state.value.totals.kcal)
        assertEquals(237.0, viewModel.state.value.meals.single { it.key == "lunch" }.kcal)
    }

    @Test
    fun removeEntryDeletesById() = runTest(dispatcher) {
        val dao = FakeDiaryEntryDao(listOf(entry(id = 10, kcal = 237.0, protein = 12.4)))
        val viewModel = TodayViewModel(
            diaryRepository = DiaryRepository(dao),
            waterRepository = WaterRepository(FakeWaterEntryDao()),
            weightRepository = WeightRepository(FakeWeightEntryDao()),
            dateProvider = { LocalDate.parse("2026-07-01") },
        )

        viewModel.removeEntry(10)
        advanceUntilIdle()

        assertEquals(emptyList(), dao.entries.value)
    }

    @Test
    fun stateSummarizesWaterEntries() = runTest(dispatcher) {
        val waterDao = FakeWaterEntryDao(listOf(water(amountMl = 250), water(amountMl = 500)))
        val viewModel = TodayViewModel(
            diaryRepository = DiaryRepository(FakeDiaryEntryDao(emptyList())),
            waterRepository = WaterRepository(waterDao),
            weightRepository = WeightRepository(FakeWeightEntryDao()),
            dateProvider = { LocalDate.parse("2026-07-01") },
        )

        advanceUntilIdle()

        assertEquals(750, viewModel.state.value.water.consumedMl)
        assertEquals(2000, viewModel.state.value.water.goalMl)
    }

    @Test
    fun addWaterInsertsAmountForToday() = runTest(dispatcher) {
        val waterDao = FakeWaterEntryDao()
        val viewModel = TodayViewModel(
            diaryRepository = DiaryRepository(FakeDiaryEntryDao(emptyList())),
            waterRepository = WaterRepository(waterDao),
            weightRepository = WeightRepository(FakeWeightEntryDao()),
            dateProvider = { LocalDate.parse("2026-07-01") },
        )

        viewModel.addWater(250)
        advanceUntilIdle()

        assertEquals(250, waterDao.entries.value.single().amountMl)
        assertEquals("2026-07-01", waterDao.entries.value.single().date)
    }

    @Test
    fun stateUsesLatestWeightEntry() = runTest(dispatcher) {
        val viewModel = TodayViewModel(
            diaryRepository = DiaryRepository(FakeDiaryEntryDao(emptyList())),
            waterRepository = WaterRepository(FakeWaterEntryDao()),
            weightRepository = WeightRepository(FakeWeightEntryDao(listOf(weight(weightKg = 107.4)))),
            dateProvider = { LocalDate.parse("2026-07-01") },
        )

        advanceUntilIdle()

        assertEquals(107.4, viewModel.state.value.weight.currentKg)
        assertEquals(80.0, viewModel.state.value.weight.goalKg)
    }

    @Test
    fun addWeightInsertsWeightForToday() = runTest(dispatcher) {
        val weightDao = FakeWeightEntryDao()
        val viewModel = TodayViewModel(
            diaryRepository = DiaryRepository(FakeDiaryEntryDao(emptyList())),
            waterRepository = WaterRepository(FakeWaterEntryDao()),
            weightRepository = WeightRepository(weightDao),
            dateProvider = { LocalDate.parse("2026-07-01") },
        )

        viewModel.addWeight(107.8)
        advanceUntilIdle()

        assertEquals(107.8, weightDao.entries.value.single().weightKg)
        assertEquals("2026-07-01", weightDao.entries.value.single().date)
    }

    @Test
    fun stateSummarizesCurrentStreak() = runTest(dispatcher) {
        val viewModel = TodayViewModel(
            diaryRepository = DiaryRepository(
                FakeDiaryEntryDao(
                    listOf(
                        entry(date = "2026-06-29", kcal = 100.0, protein = 10.0),
                        entry(date = "2026-06-30", kcal = 100.0, protein = 10.0),
                        entry(date = "2026-07-01", kcal = 100.0, protein = 10.0),
                    ),
                ),
            ),
            waterRepository = WaterRepository(FakeWaterEntryDao()),
            weightRepository = WeightRepository(FakeWeightEntryDao()),
            dateProvider = { LocalDate.parse("2026-07-01") },
        )

        advanceUntilIdle()

        assertEquals(3, viewModel.state.value.streak.current)
        assertEquals(3, viewModel.state.value.streak.best)
    }

    @Test
    fun previousDayDecrementsDate() = runTest(dispatcher) {
        val viewModel = TodayViewModel(
            diaryRepository = DiaryRepository(FakeDiaryEntryDao(emptyList())),
            waterRepository = WaterRepository(FakeWaterEntryDao()),
            weightRepository = WeightRepository(FakeWeightEntryDao()),
            dateProvider = { LocalDate.parse("2026-07-09") },
        )

        viewModel.previousDay()
        advanceUntilIdle()

        assertEquals("2026-07-08", viewModel.state.value.date)
        assertEquals(false, viewModel.state.value.isToday)
    }

    @Test
    fun nextDayIncrementsDate() = runTest(dispatcher) {
        val viewModel = TodayViewModel(
            diaryRepository = DiaryRepository(FakeDiaryEntryDao(emptyList())),
            waterRepository = WaterRepository(FakeWaterEntryDao()),
            weightRepository = WeightRepository(FakeWeightEntryDao()),
            dateProvider = { LocalDate.parse("2026-07-01") },
        )

        viewModel.nextDay()
        advanceUntilIdle()

        assertEquals("2026-07-02", viewModel.state.value.date)
        assertEquals(false, viewModel.state.value.isToday)
    }

    @Test
    fun mealGoalsScaleProportionallyWithDailyKcal() = runTest(dispatcher) {
        val viewModel = TodayViewModel(
            diaryRepository = DiaryRepository(FakeDiaryEntryDao(emptyList())),
            waterRepository = WaterRepository(FakeWaterEntryDao()),
            weightRepository = WeightRepository(FakeWeightEntryDao()),
            dateProvider = { LocalDate.parse("2026-07-01") },
            dailyKcal = 2000.0,
        )

        advanceUntilIdle()

        val meals = viewModel.state.value.meals
        assertEquals(632, meals.single { it.key == "breakfast" }.goalKcal)
        assertEquals(632, meals.single { it.key == "lunch" }.goalKcal)
        assertEquals(542, meals.single { it.key == "dinner" }.goalKcal)
        assertEquals(194, meals.single { it.key == "snack" }.goalKcal)
    }

    @Test
    fun exportJsonEscapesSpecialChars() = runTest(dispatcher) {
        val nameWithQuotes = """Café "duplo" \ test"""
        val dao = FakeDiaryEntryDao(listOf(entry(kcal = 200.0, protein = 10.0).copy(foodNameSnapshot = nameWithQuotes)))
        val viewModel = TodayViewModel(
            diaryRepository = DiaryRepository(dao),
            waterRepository = WaterRepository(FakeWaterEntryDao()),
            weightRepository = WeightRepository(FakeWeightEntryDao()),
            dateProvider = { LocalDate.parse("2026-07-01") },
        )

        val json = viewModel.exportJson()
        val parsed = JSONObject(json)
        val firstName = parsed.getJSONArray("entries").getJSONObject(0).getString("name")
        assertEquals(nameWithQuotes, firstName)
    }

    @Test
    fun goToDateChangesDateAndRestoresIsToday() = runTest(dispatcher) {
        val today = LocalDate.parse("2026-07-09")
        val viewModel = TodayViewModel(
            diaryRepository = DiaryRepository(FakeDiaryEntryDao(emptyList())),
            waterRepository = WaterRepository(FakeWaterEntryDao()),
            weightRepository = WeightRepository(FakeWeightEntryDao()),
            dateProvider = { today },
        )

        viewModel.goToDate(LocalDate.parse("2026-07-01"))
        advanceUntilIdle()
        assertEquals("2026-07-01", viewModel.state.value.date)
        assertEquals(false, viewModel.state.value.isToday)

        viewModel.goToDate(today)
        advanceUntilIdle()
        assertEquals(today.toString(), viewModel.state.value.date)
        assertEquals(true, viewModel.state.value.isToday)
    }
}

private fun entry(id: Long = 0, date: String = "2026-07-01", kcal: Double, protein: Double) = DiaryEntryEntity(
    id = id,
    date = date,
    mealType = "lunch",
    foodId = 1,
    foodNameSnapshot = "Peito de frango",
    quantity = 1.0,
    unitLabel = "100 g",
    gramsTotal = 100.0,
    kcal = kcal,
    carbs = 10.0,
    protein = protein,
    fat = 5.0,
)

private fun water(id: Long = 0, amountMl: Int) = WaterEntryEntity(
    id = id,
    date = "2026-07-01",
    amountMl = amountMl,
)

private fun weight(id: Long = 0, weightKg: Double) = WeightEntryEntity(
    id = id,
    date = "2026-07-01",
    weightKg = weightKg,
)

private class FakeDiaryEntryDao(initialEntries: List<DiaryEntryEntity>) : DiaryEntryDao {
    val entries = MutableStateFlow(initialEntries)

    override fun entriesForDate(date: String): Flow<List<DiaryEntryEntity>> = entries.map { items -> items.filter { it.date == date } }
    override fun entriesForMeal(date: String, mealType: String): Flow<List<DiaryEntryEntity>> =
        entries.map { items -> items.filter { it.date == date && it.mealType == mealType } }
    override fun entriesForMonth(yearMonth: String): Flow<List<DiaryEntryEntity>> =
        entries.map { items -> items.filter { it.date.startsWith(yearMonth) } }
    override fun activeDates(): Flow<List<String>> =
        entries.map { items -> items.map { it.date }.distinct().sorted() }
    override suspend fun allEntries(): List<DiaryEntryEntity> = entries.value

    override suspend fun insert(entry: DiaryEntryEntity): Long = error("Not used")
    override suspend fun delete(entry: DiaryEntryEntity) = Unit
    override suspend fun deleteById(entryId: Long) {
        entries.value = entries.value.filterNot { it.id == entryId }
    }
}

private class FakeWaterEntryDao(initialEntries: List<WaterEntryEntity> = emptyList()) : WaterEntryDao {
    val entries = MutableStateFlow(initialEntries)

    override fun entriesForDate(date: String): Flow<List<WaterEntryEntity>> =
        entries.map { items -> items.filter { it.date == date } }

    override suspend fun insert(entry: WaterEntryEntity): Long {
        entries.value = entries.value + entry.copy(id = entries.value.size + 1L)
        return entries.value.size.toLong()
    }

    override suspend fun deleteLastForDate(date: String) {
        val last = entries.value.filter { it.date == date }.maxByOrNull { it.id } ?: return
        entries.value = entries.value.filterNot { it.id == last.id }
    }
}

private class FakeWeightEntryDao(initialEntries: List<WeightEntryEntity> = emptyList()) : WeightEntryDao {
    val entries = MutableStateFlow(initialEntries)

    override fun entries(): Flow<List<WeightEntryEntity>> = entries

    override suspend fun insert(entry: WeightEntryEntity): Long {
        entries.value = listOf(entry.copy(id = entries.value.size + 1L)) + entries.value
        return entries.value.first().id
    }
}

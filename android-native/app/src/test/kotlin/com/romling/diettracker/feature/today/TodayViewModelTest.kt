package com.romling.diettracker.feature.today

import com.romling.diettracker.data.local.dao.DiaryEntryDao
import com.romling.diettracker.data.local.entity.DiaryEntryEntity
import com.romling.diettracker.data.repository.DiaryRepository
import java.time.LocalDate
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
            dateProvider = { LocalDate.parse("2026-07-01") },
        )

        viewModel.removeEntry(10)
        advanceUntilIdle()

        assertEquals(emptyList(), dao.entries.value)
    }
}

private fun entry(id: Long = 0, kcal: Double, protein: Double) = DiaryEntryEntity(
    id = id,
    date = "2026-07-01",
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

private class FakeDiaryEntryDao(initialEntries: List<DiaryEntryEntity>) : DiaryEntryDao {
    val entries = MutableStateFlow(initialEntries)

    override fun entriesForDate(date: String): Flow<List<DiaryEntryEntity>> = entries.map { items -> items.filter { it.date == date } }
    override fun entriesForMeal(date: String, mealType: String): Flow<List<DiaryEntryEntity>> =
        entries.map { items -> items.filter { it.date == date && it.mealType == mealType } }

    override suspend fun insert(entry: DiaryEntryEntity): Long = error("Not used")
    override suspend fun delete(entry: DiaryEntryEntity) = Unit
    override suspend fun deleteById(entryId: Long) {
        entries.value = entries.value.filterNot { it.id == entryId }
    }
}

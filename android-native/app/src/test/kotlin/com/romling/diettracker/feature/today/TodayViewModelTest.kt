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
import kotlinx.coroutines.flow.flowOf
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
        assertEquals(1800.0, viewModel.state.value.meals.single { it.key == "lunch" }.kcal)
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
    carbs = 10.0,
    protein = protein,
    fat = 5.0,
)

private class FakeDiaryEntryDao(private val entries: List<DiaryEntryEntity>) : DiaryEntryDao {
    override fun entriesForDate(date: String): Flow<List<DiaryEntryEntity>> = flowOf(entries.filter { it.date == date })
    override fun entriesForMeal(date: String, mealType: String): Flow<List<DiaryEntryEntity>> =
        flowOf(entries.filter { it.date == date && it.mealType == mealType })

    override suspend fun insert(entry: DiaryEntryEntity): Long = error("Not used")
    override suspend fun delete(entry: DiaryEntryEntity) = Unit
    override suspend fun deleteById(entryId: Long) = Unit
}

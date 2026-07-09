package com.romling.diettracker.feature.meal

import com.romling.diettracker.data.local.dao.FoodDao
import com.romling.diettracker.data.local.dao.FoodPortionDao
import com.romling.diettracker.data.local.dao.DiaryEntryDao
import com.romling.diettracker.data.local.entity.DiaryEntryEntity
import com.romling.diettracker.data.local.entity.FoodEntity
import com.romling.diettracker.data.local.entity.FoodPortionEntity
import com.romling.diettracker.data.repository.DiaryRepository
import com.romling.diettracker.data.repository.FoodRepository
import java.time.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
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
class AddFoodViewModelTest {
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
    fun updateQueryFiltersFoods() = runTest(dispatcher) {
        val viewModel = AddFoodViewModel(
            foodRepository = FoodRepository(FakeFoodDao(), FakeFoodPortionDao()),
            diaryRepository = DiaryRepository(FakeDiaryEntryDao()),
        )

        advanceUntilIdle()
        assertEquals(2, viewModel.state.value.foods.size)

        viewModel.updateQuery("caf")
        advanceUntilIdle()

        assertEquals("Café", viewModel.state.value.foods.single().name)
    }

    @Test
    fun addFoodSavesDefaultPortion() = runTest(dispatcher) {
        val diaryDao = FakeDiaryEntryDao()
        val viewModel = AddFoodViewModel(
            foodRepository = FoodRepository(FakeFoodDao(), FakeFoodPortionDao()),
            diaryRepository = DiaryRepository(diaryDao),
            dateProvider = { LocalDate.parse("2026-07-01") },
        )

        viewModel.addFood(mealType = "breakfast", foodId = 1)
        advanceUntilIdle()

        assertEquals("2026-07-01", diaryDao.entries.single().date)
        assertEquals("breakfast", diaryDao.entries.single().mealType)
        assertEquals("Café", diaryDao.entries.single().foodNameSnapshot)
        assertEquals("100 g", diaryDao.entries.single().unitLabel)
        assertEquals(100.0, diaryDao.entries.single().gramsTotal)
    }

    @Test
    fun selectFoodShowsPortions() = runTest(dispatcher) {
        val viewModel = AddFoodViewModel(
            foodRepository = FoodRepository(FakeFoodDao(), FakeFoodPortionDao()),
            diaryRepository = DiaryRepository(FakeDiaryEntryDao()),
        )

        viewModel.selectFood(1)
        advanceUntilIdle()

        assertEquals("1 xícara", viewModel.state.value.portions.single().label)
    }

    @Test
    fun openFoodDetailsShowsNutrition() = runTest(dispatcher) {
        val viewModel = AddFoodViewModel(
            foodRepository = FoodRepository(FakeFoodDao(), FakeFoodPortionDao()),
            diaryRepository = DiaryRepository(FakeDiaryEntryDao()),
        )

        advanceUntilIdle()
        viewModel.openFoodDetails(1)
        advanceUntilIdle()

        assertEquals("Café", viewModel.state.value.detailFood?.name)
        assertEquals(0.3, viewModel.state.value.detailFood?.protein)

        viewModel.closeFoodDetails()
        advanceUntilIdle()

        assertEquals(null, viewModel.state.value.detailFood)
    }

    @Test
    fun addFoodSavesSelectedPortion() = runTest(dispatcher) {
        val diaryDao = FakeDiaryEntryDao()
        val viewModel = AddFoodViewModel(
            foodRepository = FoodRepository(FakeFoodDao(), FakeFoodPortionDao()),
            diaryRepository = DiaryRepository(diaryDao),
            dateProvider = { LocalDate.parse("2026-07-01") },
        )

        viewModel.addFood(mealType = "breakfast", foodId = 1, portion = FoodPortionItem("1 xícara", 237.0))
        advanceUntilIdle()

        assertEquals("1 xícara", diaryDao.entries.single().unitLabel)
        assertEquals(237.0, diaryDao.entries.single().gramsTotal)
    }
}

private class FakeFoodDao : FoodDao {
    private val foods = listOf(
        FoodEntity(id = 1, name = "Café", category = "Bebidas", kcal100g = 2.0, carbs100g = 0.0, protein100g = 0.3, fat100g = 0.0),
        FoodEntity(id = 2, name = "Arroz branco", category = "Básicos", kcal100g = 130.0, carbs100g = 28.0, protein100g = 2.5, fat100g = 0.3),
    )

    override fun search(query: String, category: String): Flow<List<FoodEntity>> {
        val normalized = query.lowercase()
        return flowOf(foods.filter { normalized.isBlank() || it.name.lowercase().contains(normalized) })
    }

    override suspend fun getById(id: Long): FoodEntity? = foods.firstOrNull { it.id == id }
    override suspend fun count(): Int = foods.size
    override suspend fun insert(food: FoodEntity): Long = food.id
    override suspend fun insertAll(foods: List<FoodEntity>): List<Long> = foods.map { it.id }
    override suspend fun update(food: FoodEntity) = Unit
}

private class FakeFoodPortionDao : FoodPortionDao {
    override fun portionsForFood(foodId: Long): Flow<List<FoodPortionEntity>> =
        flowOf(
            if (foodId == 1L) {
                listOf(FoodPortionEntity(foodId = 1, label = "1 xícara", grams = 237.0))
            } else {
                emptyList()
            },
        )

    override suspend fun insert(portion: FoodPortionEntity): Long = portion.id
}

private class FakeDiaryEntryDao : DiaryEntryDao {
    val entries = mutableListOf<DiaryEntryEntity>()

    override fun entriesForDate(date: String): Flow<List<DiaryEntryEntity>> = flowOf(entries.filter { it.date == date })
    override fun entriesForMeal(date: String, mealType: String): Flow<List<DiaryEntryEntity>> =
        flowOf(entries.filter { it.date == date && it.mealType == mealType })

    override suspend fun insert(entry: DiaryEntryEntity): Long {
        entries += entry.copy(id = entries.size + 1L)
        return entries.size.toLong()
    }

    override suspend fun delete(entry: DiaryEntryEntity) {
        entries.removeAll { it.id == entry.id }
    }

    override suspend fun deleteById(entryId: Long) {
        entries.removeAll { it.id == entryId }
    }
}

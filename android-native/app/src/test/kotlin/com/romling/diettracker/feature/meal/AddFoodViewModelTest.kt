package com.romling.diettracker.feature.meal

import com.romling.diettracker.data.local.dao.FoodDao
import com.romling.diettracker.data.local.dao.FoodPortionDao
import com.romling.diettracker.data.local.entity.FoodEntity
import com.romling.diettracker.data.local.entity.FoodPortionEntity
import com.romling.diettracker.data.repository.FoodRepository
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
        val viewModel = AddFoodViewModel(FoodRepository(FakeFoodDao(), FakeFoodPortionDao()))

        advanceUntilIdle()
        assertEquals(2, viewModel.state.value.foods.size)

        viewModel.updateQuery("caf")
        advanceUntilIdle()

        assertEquals("Café", viewModel.state.value.foods.single().name)
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
    override fun portionsForFood(foodId: Long): Flow<List<FoodPortionEntity>> = flowOf(emptyList())
    override suspend fun insert(portion: FoodPortionEntity): Long = portion.id
}

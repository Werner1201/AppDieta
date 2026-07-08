package com.romling.diettracker.data.local.seed

import com.romling.diettracker.data.local.dao.FoodDao
import com.romling.diettracker.data.local.entity.FoodEntity
import java.io.ByteArrayInputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class FoodSeedLoaderTest {
    @Test
    fun seedIfEmptyInsertsOnlyOnce() = runTest {
        val dao = FakeFoodDao()
        val loader = FoodSeedLoader(dao)
        val json = """
            [
              {
                "name": "Feijão Preto Cozido",
                "category": "Básicos",
                "aliases": "feijao feijão",
                "kcal100g": 77.0,
                "carbs100g": 14.0,
                "protein100g": 4.5,
                "fat100g": 0.5,
                "fiber100g": 8.4,
                "sugar100g": 0.3,
                "sodiumMg100g": 2.0,
                "defaultUnit": "concha",
                "gramsPerDefaultUnit": 100.0,
                "source": "aproximado"
              }
            ]
        """.trimIndent()

        assertEquals(1, loader.seedIfEmpty(json.byteInput()))
        assertEquals(0, loader.seedIfEmpty(json.byteInput()))
        assertEquals("Feijão Preto Cozido", dao.foods.single().name)
    }
}

private fun String.byteInput() = ByteArrayInputStream(toByteArray(Charsets.UTF_8))

private class FakeFoodDao : FoodDao {
    val foods = mutableListOf<FoodEntity>()

    override fun search(query: String, category: String): Flow<List<FoodEntity>> = flowOf(foods)
    override suspend fun getById(id: Long): FoodEntity? = foods.firstOrNull { it.id == id }
    override suspend fun count(): Int = foods.size
    override suspend fun insert(food: FoodEntity): Long {
        foods += food.copy(id = foods.size + 1L)
        return foods.size.toLong()
    }
    override suspend fun insertAll(foods: List<FoodEntity>): List<Long> = foods.map { insert(it) }
    override suspend fun update(food: FoodEntity) = Unit
}

package com.romling.diettracker.data.repository

import com.romling.diettracker.data.local.dao.DiaryEntryDao
import com.romling.diettracker.data.local.entity.DiaryEntryEntity
import com.romling.diettracker.data.local.entity.FoodEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class DiaryRepositoryTest {
    @Test
    fun addFoodCalculatesSnapshotTotals() = runTest {
        val dao = FakeDiaryEntryDao()
        val repository = DiaryRepository(dao) { "2026-07-01T12:00:00Z" }
        val food = FoodEntity(
            id = 7,
            name = "Feijão Preto Cozido",
            category = "Básicos",
            kcal100g = 77.0,
            carbs100g = 14.0,
            protein100g = 4.54,
            fat100g = 0.45,
            fiber100g = 8.44,
            sugar100g = 0.34,
            sodiumMg100g = 2.04,
        )

        repository.addFood("2026-07-01", "lunch", food, gramsTotal = 150.0)

        val entry = dao.entries.single()
        assertEquals("Feijão Preto Cozido", entry.foodNameSnapshot)
        assertEquals(116.0, entry.kcal)
        assertEquals(21.0, entry.carbs)
        assertEquals(6.8, entry.protein)
        assertEquals(0.7, entry.fat)
        assertEquals("2026-07-01T12:00:00Z", entry.createdAt)
    }

    @Test
    fun addFoodRejectsNonPositiveGrams() = runTest {
        val repository = DiaryRepository(FakeDiaryEntryDao())
        val food = FoodEntity(
            id = 1,
            name = "Café",
            category = "Bebidas",
            kcal100g = 1.0,
            carbs100g = 0.0,
            protein100g = 0.1,
            fat100g = 0.0,
        )

        assertFailsWith<IllegalArgumentException> {
            repository.addFood("2026-07-01", "breakfast", food, gramsTotal = 0.0)
        }
    }
}

private class FakeDiaryEntryDao : DiaryEntryDao {
    val entries = mutableListOf<DiaryEntryEntity>()

    override fun entriesForDate(date: String): Flow<List<DiaryEntryEntity>> = flowOf(entries.filter { it.date == date })
    override fun entriesForMeal(date: String, mealType: String): Flow<List<DiaryEntryEntity>> =
        flowOf(entries.filter { it.date == date && it.mealType == mealType })
    override fun entriesForMonth(yearMonth: String): Flow<List<DiaryEntryEntity>> =
        flowOf(entries.filter { it.date.startsWith(yearMonth) })
    override fun activeDates(): Flow<List<String>> =
        flowOf(entries.map { it.date }.distinct().sorted())

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

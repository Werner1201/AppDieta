package com.romling.diettracker.data.repository

import com.romling.diettracker.data.local.dao.DiaryEntryDao
import com.romling.diettracker.data.local.entity.DiaryEntryEntity
import com.romling.diettracker.data.local.entity.FoodEntity
import java.time.Instant
import kotlin.math.round

class DiaryRepository(
    private val diaryEntryDao: DiaryEntryDao,
    private val now: () -> String = { Instant.now().toString() },
) {
    suspend fun allEntries(): List<DiaryEntryEntity> = diaryEntryDao.allEntries()
    fun entriesForDate(date: String) = diaryEntryDao.entriesForDate(date)
    fun entriesForMeal(date: String, mealType: String) = diaryEntryDao.entriesForMeal(date, mealType)
    fun entriesForMonth(yearMonth: String) = diaryEntryDao.entriesForMonth(yearMonth)
    fun activeDates() = diaryEntryDao.activeDates()
    suspend fun delete(entry: DiaryEntryEntity) = diaryEntryDao.delete(entry)
    suspend fun deleteById(entryId: Long) = diaryEntryDao.deleteById(entryId)

    suspend fun updateEntryGrams(entryId: Long, newGrams: Double) {
        require(newGrams > 0) { "Grams must be positive." }
        val entry = diaryEntryDao.getById(entryId) ?: return
        if (entry.gramsTotal <= 0) return
        val ratio = newGrams / entry.gramsTotal
        diaryEntryDao.update(
            entry.copy(
                gramsTotal = newGrams,
                unitLabel = "${newGrams.toInt()} g",
                kcal = round(entry.kcal * ratio),
                carbs = round1(entry.carbs * ratio),
                protein = round1(entry.protein * ratio),
                fat = round1(entry.fat * ratio),
                fiber = round1(entry.fiber * ratio),
                sugar = round1(entry.sugar * ratio),
                sodiumMg = round1(entry.sodiumMg * ratio),
            )
        )
    }

    suspend fun addImportedFood(
        date: String,
        mealType: String,
        name: String,
        kcal: Double,
        carbs: Double = 0.0,
        protein: Double = 0.0,
        fat: Double = 0.0,
        gramsTotal: Double = 100.0,
    ): Long {
        val timestamp = now()
        return diaryEntryDao.insert(
            DiaryEntryEntity(
                date = date,
                mealType = mealType,
                foodId = 0L,
                foodNameSnapshot = name,
                quantity = 1.0,
                unitLabel = "${gramsTotal.toInt()} g",
                gramsTotal = gramsTotal,
                kcal = kotlin.math.round(kcal),
                carbs = round1(carbs),
                protein = round1(protein),
                fat = round1(fat),
                createdAt = timestamp,
                updatedAt = timestamp,
            ),
        )
    }

    suspend fun addFood(
        date: String,
        mealType: String,
        food: FoodEntity,
        quantity: Double = 1.0,
        gramsTotal: Double = food.gramsPerDefaultUnit,
        unitLabel: String = food.defaultUnit,
        aiImportId: Long? = null,
    ): Long {
        require(quantity > 0) { "Quantity must be positive." }
        require(gramsTotal > 0) { "Grams must be positive." }

        val factor = gramsTotal / 100.0
        val timestamp = now()
        return diaryEntryDao.insert(
            DiaryEntryEntity(
                date = date,
                mealType = mealType,
                foodId = food.id,
                foodNameSnapshot = food.name,
                quantity = quantity,
                unitLabel = unitLabel,
                gramsTotal = gramsTotal,
                kcal = round(food.kcal100g * factor),
                carbs = round1(food.carbs100g * factor),
                protein = round1(food.protein100g * factor),
                fat = round1(food.fat100g * factor),
                fiber = round1(food.fiber100g * factor),
                sugar = round1(food.sugar100g * factor),
                sodiumMg = round1(food.sodiumMg100g * factor),
                aiImportId = aiImportId,
                createdAt = timestamp,
                updatedAt = timestamp,
            ),
        )
    }
}

private fun round1(value: Double) = round(value * 10.0) / 10.0

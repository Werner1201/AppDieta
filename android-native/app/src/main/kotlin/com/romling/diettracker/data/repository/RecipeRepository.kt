package com.romling.diettracker.data.repository

import com.romling.diettracker.data.local.dao.RecipeDao
import com.romling.diettracker.data.local.dao.RecipeIngredientDao
import com.romling.diettracker.data.local.entity.RecipeEntity
import com.romling.diettracker.data.local.entity.RecipeIngredientEntity
import java.time.Instant
import kotlin.math.round

class RecipeRepository(
    private val recipeDao: RecipeDao,
    private val recipeIngredientDao: RecipeIngredientDao,
) {
    fun allRecipes() = recipeDao.allRecipes()
    fun ingredientsForRecipe(recipeId: Long) = recipeIngredientDao.ingredientsForRecipe(recipeId)

    suspend fun create(name: String, description: String) {
        recipeDao.insert(RecipeEntity(name = name, description = description, createdAt = Instant.now().toString()))
    }

    suspend fun deleteById(id: Long) = recipeDao.deleteById(id)

    suspend fun addIngredient(
        recipeId: Long,
        foodName: String,
        grams: Double,
        kcal100g: Double,
        carbs100g: Double,
        protein100g: Double,
        fat100g: Double,
    ) {
        val factor = grams / 100.0
        recipeIngredientDao.insert(
            RecipeIngredientEntity(
                recipeId = recipeId,
                foodNameSnapshot = foodName,
                grams = grams,
                kcal = round(kcal100g * factor),
                carbs = round1(carbs100g * factor),
                protein = round1(protein100g * factor),
                fat = round1(fat100g * factor),
                createdAt = Instant.now().toString(),
            )
        )
    }

    suspend fun removeIngredient(id: Long) = recipeIngredientDao.deleteById(id)
}

private fun round1(value: Double) = round(value * 10.0) / 10.0

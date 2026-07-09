package com.romling.diettracker.data.repository

import com.romling.diettracker.data.local.dao.RecipeDao
import com.romling.diettracker.data.local.entity.RecipeEntity
import java.time.Instant

class RecipeRepository(private val recipeDao: RecipeDao) {
    fun allRecipes() = recipeDao.allRecipes()

    suspend fun create(name: String, description: String) {
        recipeDao.insert(RecipeEntity(name = name, description = description, createdAt = Instant.now().toString()))
    }

    suspend fun deleteById(id: Long) = recipeDao.deleteById(id)
}

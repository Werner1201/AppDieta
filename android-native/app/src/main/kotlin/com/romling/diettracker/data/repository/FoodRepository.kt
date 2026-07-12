package com.romling.diettracker.data.repository

import com.romling.diettracker.data.local.dao.FoodDao
import com.romling.diettracker.data.local.dao.FoodPortionDao
import com.romling.diettracker.data.local.entity.FoodEntity
import com.romling.diettracker.data.local.entity.FoodPortionEntity

class FoodRepository(
    private val foodDao: FoodDao,
    private val foodPortionDao: FoodPortionDao,
) {
    fun search(query: String = "", category: String = "") = foodDao.search(query, category)
    fun frequentForMeal(mealType: String) = foodDao.frequentForMeal(mealType)
    fun customFoods() = foodDao.customFoods()
    fun portionsForFood(foodId: Long) = foodPortionDao.portionsForFood(foodId)
    suspend fun getById(id: Long) = foodDao.getById(id)
    suspend fun add(food: FoodEntity) = foodDao.insert(food)
    suspend fun update(food: FoodEntity) = foodDao.update(food)
    suspend fun deleteById(id: Long) = foodDao.deleteById(id)
    suspend fun addPortion(portion: FoodPortionEntity) = foodPortionDao.insert(portion)
}

package com.romling.diettracker.data.local.seed

import com.romling.diettracker.data.local.dao.FoodDao
import com.romling.diettracker.data.local.entity.FoodEntity
import java.io.InputStream
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class FoodSeedLoader(
    private val foodDao: FoodDao,
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    suspend fun seedIfEmpty(input: InputStream): Int {
        if (foodDao.count() > 0) return 0
        val foods = json.decodeFromString<List<SeedFood>>(input.bufferedReader().use { it.readText() })
            .map { it.toEntity() }
        return foodDao.insertAll(foods).count { it != -1L }
    }
}

@Serializable
private data class SeedFood(
    val name: String,
    val category: String,
    val aliases: String,
    val kcal100g: Double,
    val carbs100g: Double,
    val protein100g: Double,
    val fat100g: Double,
    val fiber100g: Double,
    val sugar100g: Double,
    val sodiumMg100g: Double,
    val defaultUnit: String,
    val gramsPerDefaultUnit: Double,
    val source: String,
) {
    fun toEntity() = FoodEntity(
        name = name,
        category = category,
        aliases = aliases,
        kcal100g = kcal100g,
        carbs100g = carbs100g,
        protein100g = protein100g,
        fat100g = fat100g,
        fiber100g = fiber100g,
        sugar100g = sugar100g,
        sodiumMg100g = sodiumMg100g,
        defaultUnit = defaultUnit,
        gramsPerDefaultUnit = gramsPerDefaultUnit,
        source = source,
    )
}

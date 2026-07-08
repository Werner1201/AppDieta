package com.romling.diettracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "foods")
data class FoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val aliases: String = "",
    @ColumnInfo(name = "kcal_100g") val kcal100g: Double,
    @ColumnInfo(name = "carbs_100g") val carbs100g: Double,
    @ColumnInfo(name = "protein_100g") val protein100g: Double,
    @ColumnInfo(name = "fat_100g") val fat100g: Double,
    @ColumnInfo(name = "fiber_100g") val fiber100g: Double = 0.0,
    @ColumnInfo(name = "sugar_100g") val sugar100g: Double = 0.0,
    @ColumnInfo(name = "sodium_mg_100g") val sodiumMg100g: Double = 0.0,
    @ColumnInfo(name = "default_unit") val defaultUnit: String = "100 g",
    @ColumnInfo(name = "grams_per_default_unit") val gramsPerDefaultUnit: Double = 100.0,
    val source: String = "aproximado",
    @ColumnInfo(name = "is_custom") val isCustom: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: String = "",
    @ColumnInfo(name = "updated_at") val updatedAt: String = "",
)

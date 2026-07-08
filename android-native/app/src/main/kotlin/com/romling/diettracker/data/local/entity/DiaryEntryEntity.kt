package com.romling.diettracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "diary_entries",
    foreignKeys = [
        ForeignKey(
            entity = FoodEntity::class,
            parentColumns = ["id"],
            childColumns = ["food_id"],
        ),
    ],
    indices = [Index("date"), Index("meal_type"), Index("food_id")],
)
data class DiaryEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    @ColumnInfo(name = "meal_type") val mealType: String,
    @ColumnInfo(name = "food_id") val foodId: Long,
    @ColumnInfo(name = "food_name_snapshot") val foodNameSnapshot: String,
    val quantity: Double,
    @ColumnInfo(name = "unit_label") val unitLabel: String,
    @ColumnInfo(name = "grams_total") val gramsTotal: Double,
    val kcal: Double,
    val carbs: Double,
    val protein: Double,
    val fat: Double,
    val fiber: Double = 0.0,
    val sugar: Double = 0.0,
    @ColumnInfo(name = "sodium_mg") val sodiumMg: Double = 0.0,
    @ColumnInfo(name = "ai_import_id") val aiImportId: Long? = null,
    @ColumnInfo(name = "created_at") val createdAt: String = "",
    @ColumnInfo(name = "updated_at") val updatedAt: String = "",
)

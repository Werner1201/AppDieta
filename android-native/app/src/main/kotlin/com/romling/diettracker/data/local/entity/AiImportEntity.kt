package com.romling.diettracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "ai_imports", indices = [Index("date"), Index("meal_type")])
data class AiImportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val source: String,
    val date: String,
    @ColumnInfo(name = "meal_type") val mealType: String,
    @ColumnInfo(name = "dish_name") val dishName: String,
    @ColumnInfo(name = "raw_payload_json") val rawPayloadJson: String,
    val confidence: String,
    val notes: String = "",
    @ColumnInfo(name = "created_at") val createdAt: String = "",
    @ColumnInfo(name = "confirmed_at") val confirmedAt: String? = null,
)

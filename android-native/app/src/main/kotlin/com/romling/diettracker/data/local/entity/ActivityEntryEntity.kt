package com.romling.diettracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "activity_entries", indices = [Index("date")])
data class ActivityEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val name: String,
    val icon: String,
    val met: Double,
    @ColumnInfo(name = "duration_minutes") val durationMinutes: Int,
    @ColumnInfo(name = "distance_km") val distanceKm: Double? = null,
    val steps: Int? = null,
    @ColumnInfo(name = "weight_kg") val weightKg: Double,
    val kcal: Double,
    val note: String = "",
    @ColumnInfo(name = "created_at") val createdAt: String = "",
)

package com.romling.diettracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "weight_entries", indices = [Index("date")])
data class WeightEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    @ColumnInfo(name = "weight_kg") val weightKg: Double,
    @ColumnInfo(name = "created_at") val createdAt: String = "",
)

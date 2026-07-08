package com.romling.diettracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "water_entries", indices = [Index("date")])
data class WaterEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    @ColumnInfo(name = "amount_ml") val amountMl: Int,
    @ColumnInfo(name = "created_at") val createdAt: String = "",
)

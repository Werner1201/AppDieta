package com.romling.diettracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "daily_commitments", indices = [Index(value = ["date"], unique = true)])
data class DailyCommitmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val committed: Boolean = true,
    @ColumnInfo(name = "created_at") val createdAt: String = "",
)

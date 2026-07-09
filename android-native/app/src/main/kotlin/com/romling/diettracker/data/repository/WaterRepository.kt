package com.romling.diettracker.data.repository

import com.romling.diettracker.data.local.dao.WaterEntryDao
import com.romling.diettracker.data.local.entity.WaterEntryEntity
import java.time.Instant

class WaterRepository(
    private val waterEntryDao: WaterEntryDao,
    private val now: () -> String = { Instant.now().toString() },
) {
    fun entriesForDate(date: String) = waterEntryDao.entriesForDate(date)

    suspend fun addWater(date: String, amountMl: Int): Long {
        require(amountMl > 0) { "Water amount must be positive." }
        return waterEntryDao.insert(WaterEntryEntity(date = date, amountMl = amountMl, createdAt = now()))
    }

    suspend fun deleteLastForDate(date: String) = waterEntryDao.deleteLastForDate(date)
}

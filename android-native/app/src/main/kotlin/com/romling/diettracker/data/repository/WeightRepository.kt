package com.romling.diettracker.data.repository

import com.romling.diettracker.data.local.dao.WeightEntryDao
import com.romling.diettracker.data.local.entity.WeightEntryEntity
import java.time.Instant

class WeightRepository(
    private val weightEntryDao: WeightEntryDao,
    private val now: () -> String = { Instant.now().toString() },
) {
    fun entries() = weightEntryDao.entries()

    suspend fun addWeight(date: String, weightKg: Double): Long {
        require(weightKg > 0.0) { "Weight must be positive." }
        return weightEntryDao.insert(WeightEntryEntity(date = date, weightKg = weightKg, createdAt = now()))
    }
}

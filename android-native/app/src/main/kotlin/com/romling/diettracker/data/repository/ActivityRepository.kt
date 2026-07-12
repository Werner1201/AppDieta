package com.romling.diettracker.data.repository

import com.romling.diettracker.data.local.dao.ActivityEntryDao
import com.romling.diettracker.data.local.entity.ActivityEntryEntity
import com.romling.diettracker.domain.service.ActivityCalorieCalculator
import java.time.Instant

class ActivityRepository(
    private val dao: ActivityEntryDao,
    private val now: () -> String = { Instant.now().toString() },
) {
    fun entriesForDate(date: String) = dao.entriesForDate(date)

    suspend fun add(
        date: String,
        name: String,
        icon: String,
        met: Double,
        durationMinutes: Int,
        weightKg: Double,
        distanceKm: Double? = null,
        note: String = "",
    ) = dao.insert(
        ActivityEntryEntity(
            date = date,
            name = name,
            icon = icon,
            met = met,
            durationMinutes = durationMinutes,
            distanceKm = distanceKm,
            weightKg = weightKg,
            kcal = ActivityCalorieCalculator.calculate(met, weightKg, durationMinutes),
            note = note,
            createdAt = now(),
        ),
    )

    suspend fun deleteById(id: Long) = dao.deleteById(id)
}

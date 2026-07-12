package com.romling.diettracker.data.repository

import com.romling.diettracker.data.local.dao.ActivityEntryDao
import com.romling.diettracker.data.local.entity.ActivityEntryEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

class ActivityRepositoryTest {
    @Test
    fun updateKeepsIdAndRecalculatesCalories() = runTest {
        val dao = FakeActivityDao()
        dao.insert(
            ActivityEntryEntity(
                id = 7,
                date = "2026-07-01",
                name = "Caminhada",
                icon = "W",
                met = 3.5,
                durationMinutes = 30,
                weightKg = 100.0,
                kcal = 183.75,
            ),
        )

        ActivityRepository(dao).update(7, "Caminhada", "W", 3.5, 60, 100.0)

        assertEquals(7, dao.entry?.id)
        assertEquals(367.5, dao.entry?.kcal)
    }
}

private class FakeActivityDao : ActivityEntryDao {
    var entry: ActivityEntryEntity? = null
    override fun entriesForDate(date: String): Flow<List<ActivityEntryEntity>> = flowOf(listOfNotNull(entry))
    override fun frequentNames(): Flow<List<String>> = flowOf(listOfNotNull(entry?.name))
    override suspend fun getById(id: Long): ActivityEntryEntity? = entry?.takeIf { it.id == id }
    override suspend fun insert(entry: ActivityEntryEntity): Long { this.entry = entry; return entry.id }
    override suspend fun update(entry: ActivityEntryEntity) { this.entry = entry }
    override suspend fun deleteById(id: Long) { if (entry?.id == id) entry = null }
}

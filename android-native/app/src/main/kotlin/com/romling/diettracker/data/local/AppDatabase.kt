package com.romling.diettracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.romling.diettracker.data.local.dao.AiImportDao
import com.romling.diettracker.data.local.dao.DailyCommitmentDao
import com.romling.diettracker.data.local.dao.DiaryEntryDao
import com.romling.diettracker.data.local.dao.FoodDao
import com.romling.diettracker.data.local.dao.FoodPortionDao
import com.romling.diettracker.data.local.dao.WaterEntryDao
import com.romling.diettracker.data.local.dao.WeightEntryDao
import com.romling.diettracker.data.local.entity.AiImportEntity
import com.romling.diettracker.data.local.entity.DailyCommitmentEntity
import com.romling.diettracker.data.local.entity.DiaryEntryEntity
import com.romling.diettracker.data.local.entity.FoodEntity
import com.romling.diettracker.data.local.entity.FoodPortionEntity
import com.romling.diettracker.data.local.entity.WaterEntryEntity
import com.romling.diettracker.data.local.entity.WeightEntryEntity

@Database(
    entities = [
        FoodEntity::class,
        FoodPortionEntity::class,
        DiaryEntryEntity::class,
        WaterEntryEntity::class,
        WeightEntryEntity::class,
        DailyCommitmentEntity::class,
        AiImportEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun foodPortionDao(): FoodPortionDao
    abstract fun diaryEntryDao(): DiaryEntryDao
    abstract fun waterEntryDao(): WaterEntryDao
    abstract fun weightEntryDao(): WeightEntryDao
    abstract fun dailyCommitmentDao(): DailyCommitmentDao
    abstract fun aiImportDao(): AiImportDao
}

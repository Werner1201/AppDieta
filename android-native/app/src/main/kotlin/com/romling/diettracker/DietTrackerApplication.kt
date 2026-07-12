package com.romling.diettracker

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.romling.diettracker.data.local.AppDatabase
import com.romling.diettracker.data.local.MIGRATION_1_2
import com.romling.diettracker.data.local.MIGRATION_2_3
import com.romling.diettracker.data.local.MIGRATION_3_4
import com.romling.diettracker.data.local.MIGRATION_4_5
import com.romling.diettracker.data.local.seed.FoodSeedLoader
import com.romling.diettracker.data.repository.DiaryRepository
import com.romling.diettracker.data.repository.ActivityRepository
import com.romling.diettracker.data.repository.FoodRepository
import com.romling.diettracker.data.repository.RecipeRepository
import com.romling.diettracker.data.repository.SettingsRepository
import com.romling.diettracker.data.repository.WaterRepository
import com.romling.diettracker.data.repository.WeightRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DietTrackerApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.seedFoods()
    }

    override fun onTerminate() {
        container.close()
        super.onTerminate()
    }
}

class AppContainer(private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var databaseInstance: AppDatabase? = null

    val database: AppDatabase
        get() = databaseInstance ?: Room.databaseBuilder(context, AppDatabase::class.java, "diet_tracker.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .build()
            .also { databaseInstance = it }
    val foodRepository: FoodRepository by lazy {
        FoodRepository(database.foodDao(), database.foodPortionDao())
    }
    val diaryRepository: DiaryRepository by lazy {
        DiaryRepository(database.diaryEntryDao())
    }
    val waterRepository: WaterRepository by lazy {
        WaterRepository(database.waterEntryDao())
    }
    val weightRepository: WeightRepository by lazy {
        WeightRepository(database.weightEntryDao())
    }
    val activityRepository: ActivityRepository by lazy {
        ActivityRepository(database.activityEntryDao())
    }
    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(context.getSharedPreferences("diet_tracker_settings", Context.MODE_PRIVATE))
    }
    val recipeRepository: RecipeRepository by lazy {
        RecipeRepository(database.recipeDao(), database.recipeIngredientDao())
    }

    fun seedFoods() {
        scope.launch {
            context.assets.open("foods_seed.json").use {
                FoodSeedLoader(database.foodDao()).seedIfEmpty(it)
            }
        }
    }

    fun close() {
        scope.cancel()
        databaseInstance?.close()
    }
}

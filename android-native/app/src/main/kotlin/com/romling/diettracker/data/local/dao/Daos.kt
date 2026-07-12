package com.romling.diettracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.romling.diettracker.data.local.entity.AiImportEntity
import com.romling.diettracker.data.local.entity.DailyCommitmentEntity
import com.romling.diettracker.data.local.entity.DiaryEntryEntity
import com.romling.diettracker.data.local.entity.FoodEntity
import com.romling.diettracker.data.local.entity.FoodPortionEntity
import com.romling.diettracker.data.local.entity.RecipeEntity
import com.romling.diettracker.data.local.entity.RecipeIngredientEntity
import com.romling.diettracker.data.local.entity.WaterEntryEntity
import com.romling.diettracker.data.local.entity.WeightEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query(
        """
        SELECT * FROM foods
        WHERE (:query = '' OR name LIKE '%' || :query || '%' OR aliases LIKE '%' || :query || '%')
        AND (:category = '' OR category = :category)
        ORDER BY name
        LIMIT 100
        """,
    )
    fun search(query: String = "", category: String = ""): Flow<List<FoodEntity>>

    @Query(
        """
        SELECT foods.* FROM foods
        INNER JOIN diary_entries ON diary_entries.food_id = foods.id
        WHERE diary_entries.meal_type = :mealType
        GROUP BY foods.id
        ORDER BY COUNT(*) DESC, MAX(diary_entries.created_at) DESC
        LIMIT 20
        """,
    )
    fun frequentForMeal(mealType: String): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): FoodEntity?

    @Query("SELECT COUNT(*) FROM foods")
    suspend fun count(): Int

    @Query("SELECT * FROM foods WHERE is_custom = 1 ORDER BY name")
    fun customFoods(): Flow<List<FoodEntity>>

    @Query("DELETE FROM foods WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(food: FoodEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(foods: List<FoodEntity>): List<Long>

    @Update
    suspend fun update(food: FoodEntity)
}

@Dao
interface FoodPortionDao {
    @Query("SELECT * FROM food_portions WHERE food_id = :foodId ORDER BY grams")
    fun portionsForFood(foodId: Long): Flow<List<FoodPortionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(portion: FoodPortionEntity): Long
}

@Dao
interface DiaryEntryDao {
    @Query("SELECT * FROM diary_entries WHERE date = :date ORDER BY created_at, id")
    fun entriesForDate(date: String): Flow<List<DiaryEntryEntity>>

    @Query("SELECT * FROM diary_entries WHERE date = :date AND meal_type = :mealType ORDER BY created_at, id")
    fun entriesForMeal(date: String, mealType: String): Flow<List<DiaryEntryEntity>>

    @Query("SELECT * FROM diary_entries WHERE date LIKE :yearMonth || '-%' ORDER BY date, created_at")
    fun entriesForMonth(yearMonth: String): Flow<List<DiaryEntryEntity>>

    @Query("SELECT DISTINCT date FROM diary_entries ORDER BY date")
    fun activeDates(): Flow<List<String>>

    @Query("SELECT * FROM diary_entries ORDER BY date DESC, created_at DESC")
    suspend fun allEntries(): List<DiaryEntryEntity>

    @Query("SELECT * FROM diary_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): DiaryEntryEntity?

    @Insert
    suspend fun insert(entry: DiaryEntryEntity): Long

    @Update
    suspend fun update(entry: DiaryEntryEntity)

    @Delete
    suspend fun delete(entry: DiaryEntryEntity)

    @Query("DELETE FROM diary_entries WHERE id = :entryId")
    suspend fun deleteById(entryId: Long)
}

@Dao
interface WaterEntryDao {
    @Query("SELECT * FROM water_entries WHERE date = :date ORDER BY created_at, id")
    fun entriesForDate(date: String): Flow<List<WaterEntryEntity>>

    @Insert
    suspend fun insert(entry: WaterEntryEntity): Long

    @Query("DELETE FROM water_entries WHERE id = (SELECT id FROM water_entries WHERE date = :date ORDER BY id DESC LIMIT 1)")
    suspend fun deleteLastForDate(date: String)
}

@Dao
interface WeightEntryDao {
    @Query("SELECT * FROM weight_entries ORDER BY date DESC, id DESC")
    fun entries(): Flow<List<WeightEntryEntity>>

    @Insert
    suspend fun insert(entry: WeightEntryEntity): Long
}

@Dao
interface DailyCommitmentDao {
    @Query("SELECT * FROM daily_commitments WHERE date = :date LIMIT 1")
    suspend fun get(date: String): DailyCommitmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: DailyCommitmentEntity)
}

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY name")
    fun allRecipes(): Flow<List<RecipeEntity>>

    @Insert
    suspend fun insert(recipe: RecipeEntity): Long

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface RecipeIngredientDao {
    @Query("SELECT * FROM recipe_ingredients WHERE recipe_id = :recipeId ORDER BY created_at, id")
    fun ingredientsForRecipe(recipeId: Long): Flow<List<RecipeIngredientEntity>>

    @Insert
    suspend fun insert(ingredient: RecipeIngredientEntity): Long

    @Query("DELETE FROM recipe_ingredients WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface AiImportDao {
    @Insert
    suspend fun insert(entry: AiImportEntity): Long

    @Query("SELECT * FROM ai_imports WHERE date = :date AND meal_type = :mealType ORDER BY created_at DESC")
    fun importsForMeal(date: String, mealType: String): Flow<List<AiImportEntity>>
}

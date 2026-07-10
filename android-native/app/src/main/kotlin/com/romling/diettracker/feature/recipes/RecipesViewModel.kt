package com.romling.diettracker.feature.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.romling.diettracker.data.local.entity.RecipeEntity
import com.romling.diettracker.data.local.entity.RecipeIngredientEntity
import com.romling.diettracker.data.repository.FoodRepository
import com.romling.diettracker.data.repository.DiaryRepository
import com.romling.diettracker.data.repository.RecipeRepository
import com.romling.diettracker.feature.meal.FoodSearchItem
import kotlin.math.round
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class RecipesViewModel(
    private val recipeRepository: RecipeRepository,
    private val foodRepository: FoodRepository,
    private val diaryRepository: DiaryRepository,
) : ViewModel() {
    val recipes: StateFlow<List<RecipeEntity>> = recipeRepository.allRecipes()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _selectedRecipeId = MutableStateFlow<Long?>(null)
    val selectedRecipeId: StateFlow<Long?> = _selectedRecipeId.asStateFlow()

    val selectedIngredients: StateFlow<List<RecipeIngredientEntity>> = _selectedRecipeId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else recipeRepository.ingredientsForRecipe(id)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _foodQuery = MutableStateFlow("")
    val foodResults: StateFlow<List<FoodSearchItem>> = _foodQuery
        .flatMapLatest { q -> foodRepository.search(q) }
        .map { foods ->
            foods.map { food ->
                FoodSearchItem(
                    id = food.id,
                    name = food.name,
                    serving = food.defaultUnit,
                    kcal = round(food.kcal100g * food.gramsPerDefaultUnit / 100.0),
                    carbs = round1(food.carbs100g * food.gramsPerDefaultUnit / 100.0),
                    protein = round1(food.protein100g * food.gramsPerDefaultUnit / 100.0),
                    fat = round1(food.fat100g * food.gramsPerDefaultUnit / 100.0),
                    fiber = round1(food.fiber100g * food.gramsPerDefaultUnit / 100.0),
                    sugar = round1(food.sugar100g * food.gramsPerDefaultUnit / 100.0),
                    sodiumMg = round1(food.sodiumMg100g * food.gramsPerDefaultUnit / 100.0),
                    source = if (food.isCustom) "custom" else "db",
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun selectRecipe(id: Long) { _selectedRecipeId.value = id }
    fun clearRecipe() { _selectedRecipeId.value = null }
    fun searchFoods(query: String) { _foodQuery.value = query }
    fun clearFoodSearch() { _foodQuery.value = "" }

    fun create(name: String, description: String) {
        if (name.isBlank()) return
        viewModelScope.launch { recipeRepository.create(name.trim(), description.trim()) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { recipeRepository.deleteById(id) }
    }

    fun addIngredientFromFood(recipeId: Long, food: FoodSearchItem, grams: Double) {
        viewModelScope.launch {
            val entity = foodRepository.getById(food.id) ?: return@launch
            recipeRepository.addIngredient(
                recipeId = recipeId,
                foodName = entity.name,
                grams = grams,
                kcal100g = entity.kcal100g,
                carbs100g = entity.carbs100g,
                protein100g = entity.protein100g,
                fat100g = entity.fat100g,
            )
        }
    }

    fun removeIngredient(id: Long) {
        viewModelScope.launch { recipeRepository.removeIngredient(id) }
    }

    fun addToDiary(
        date: String,
        mealType: String,
        recipe: RecipeEntity,
        ingredients: List<RecipeIngredientEntity>,
        onDone: () -> Unit,
    ) {
        if (ingredients.isEmpty()) return
        val totals = recipeTotals(ingredients)
        viewModelScope.launch {
            diaryRepository.addImportedFood(
                date = date,
                mealType = mealType,
                name = recipe.name,
                kcal = totals.kcal,
                carbs = totals.carbs,
                protein = totals.protein,
                fat = totals.fat,
                gramsTotal = totals.grams,
            )
            onDone()
        }
    }
}

private fun round1(value: Double) = round(value * 10.0) / 10.0

internal data class RecipeTotals(
    val grams: Double,
    val kcal: Double,
    val carbs: Double,
    val protein: Double,
    val fat: Double,
)

internal fun recipeTotals(ingredients: List<RecipeIngredientEntity>) = RecipeTotals(
    grams = ingredients.sumOf { it.grams },
    kcal = ingredients.sumOf { it.kcal },
    carbs = ingredients.sumOf { it.carbs },
    protein = ingredients.sumOf { it.protein },
    fat = ingredients.sumOf { it.fat },
)

class RecipesViewModelFactory(
    private val recipeRepository: RecipeRepository,
    private val foodRepository: FoodRepository,
    private val diaryRepository: DiaryRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return RecipesViewModel(recipeRepository, foodRepository, diaryRepository) as T
    }
}

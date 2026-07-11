package com.romling.diettracker.feature.meal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.romling.diettracker.data.local.entity.FoodEntity
import com.romling.diettracker.data.repository.DiaryRepository
import com.romling.diettracker.data.repository.FoodRepository
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class AddFoodViewModel(
    private val foodRepository: FoodRepository,
    private val diaryRepository: DiaryRepository,
    private val dateProvider: () -> LocalDate = { LocalDate.now() },
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val selectedFoodId = MutableStateFlow<Long?>(null)
    private val detailFoodId = MutableStateFlow<Long?>(null)
    private val foods = query.flatMapLatest { foodRepository.search(it) }

    val customFoods: StateFlow<List<FoodSearchItem>> = foodRepository.customFoods()
        .map { list ->
            list.map { food ->
                FoodSearchItem(
                    id = food.id, name = food.name, serving = food.defaultUnit,
                    kcal = food.kcal100g, carbs = food.carbs100g, protein = food.protein100g,
                    fat = food.fat100g, fiber = food.fiber100g, sugar = food.sugar100g,
                    sodiumMg = food.sodiumMg100g, source = food.source,
                )
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    private val portions = selectedFoodId.flatMapLatest { foodId ->
        if (foodId == null) flowOf(emptyList()) else foodRepository.portionsForFood(foodId)
    }

    val state: StateFlow<AddFoodUiState> = combine(query, foods, selectedFoodId, detailFoodId, portions) { query, foods, selectedFoodId, detailFoodId, portions ->
        val items = foods.map {
            FoodSearchItem(
                id = it.id,
                name = it.name,
                serving = it.defaultUnit,
                kcal = it.kcal100g,
                carbs = it.carbs100g,
                protein = it.protein100g,
                fat = it.fat100g,
                fiber = it.fiber100g,
                sugar = it.sugar100g,
                sodiumMg = it.sodiumMg100g,
                source = it.source,
            )
        }
        AddFoodUiState(
            query = query,
            selectedFoodId = selectedFoodId,
            detailFood = items.firstOrNull { it.id == detailFoodId },
            portions = portions.map { FoodPortionItem(label = it.label, grams = it.grams) },
            foods = items,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AddFoodUiState())

    fun updateQuery(value: String) {
        query.value = value
    }

    fun selectFood(foodId: Long) {
        selectedFoodId.value = if (selectedFoodId.value == foodId) null else foodId
    }

    fun openFoodDetails(foodId: Long) {
        detailFoodId.value = foodId
    }

    fun closeFoodDetails() {
        detailFoodId.value = null
    }

    fun deleteCustomFood(id: Long) {
        viewModelScope.launch { foodRepository.deleteById(id) }
    }

    fun createCustomFood(input: CustomFoodInput, onCreated: () -> Unit = {}) {
        viewModelScope.launch {
            foodRepository.add(
                FoodEntity(
                    name = input.name,
                    category = input.category,
                    aliases = input.aliases,
                    kcal100g = input.kcal100g,
                    carbs100g = input.carbs100g,
                    protein100g = input.protein100g,
                    fat100g = input.fat100g,
                    fiber100g = input.fiber100g,
                    sugar100g = input.sugar100g,
                    sodiumMg100g = input.sodiumMg100g,
                    defaultUnit = input.defaultUnit,
                    gramsPerDefaultUnit = input.gramsPerDefaultUnit,
                    source = input.source,
                    isCustom = true,
                ),
            )
            query.value = input.name
            onCreated()
        }
    }

    fun addFood(mealType: String, foodId: Long, portion: FoodPortionItem? = null, onAdded: () -> Unit = {}) {
        viewModelScope.launch {
            val food = foodRepository.getById(foodId) ?: return@launch
            diaryRepository.addFood(
                date = dateProvider().toString(),
                mealType = mealType,
                food = food,
                gramsTotal = portion?.grams ?: food.gramsPerDefaultUnit,
                unitLabel = portion?.label ?: food.defaultUnit,
            )
            onAdded()
        }
    }
}

class AddFoodViewModelFactory(
    private val foodRepository: FoodRepository,
    private val diaryRepository: DiaryRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AddFoodViewModel(foodRepository, diaryRepository) as T
    }
}

data class AddFoodUiState(
    val query: String = "",
    val foods: List<FoodSearchItem> = emptyList(),
    val selectedFoodId: Long? = null,
    val detailFood: FoodSearchItem? = null,
    val portions: List<FoodPortionItem> = emptyList(),
)

data class FoodSearchItem(
    val id: Long,
    val name: String,
    val serving: String,
    val kcal: Double,
    val carbs: Double,
    val protein: Double,
    val fat: Double,
    val fiber: Double,
    val sugar: Double,
    val sodiumMg: Double,
    val source: String,
)

data class FoodPortionItem(
    val label: String,
    val grams: Double,
)

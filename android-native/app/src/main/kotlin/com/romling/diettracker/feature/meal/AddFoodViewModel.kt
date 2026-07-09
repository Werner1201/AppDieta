package com.romling.diettracker.feature.meal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class AddFoodViewModel(
    private val foodRepository: FoodRepository,
    private val diaryRepository: DiaryRepository,
    private val dateProvider: () -> LocalDate = { LocalDate.now() },
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val selectedFoodId = MutableStateFlow<Long?>(null)
    private val foods = query.flatMapLatest { foodRepository.search(it) }
    private val portions = selectedFoodId.flatMapLatest { foodId ->
        if (foodId == null) flowOf(emptyList()) else foodRepository.portionsForFood(foodId)
    }

    val state: StateFlow<AddFoodUiState> = combine(query, foods, selectedFoodId, portions) { query, foods, selectedFoodId, portions ->
        AddFoodUiState(
            query = query,
            selectedFoodId = selectedFoodId,
            portions = portions.map { FoodPortionItem(label = it.label, grams = it.grams) },
            foods = foods.map {
                FoodSearchItem(
                    id = it.id,
                    name = it.name,
                    serving = it.defaultUnit,
                    kcal = it.kcal100g,
                )
            },
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AddFoodUiState())

    fun updateQuery(value: String) {
        query.value = value
    }

    fun selectFood(foodId: Long) {
        selectedFoodId.value = if (selectedFoodId.value == foodId) null else foodId
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
    val portions: List<FoodPortionItem> = emptyList(),
)

data class FoodSearchItem(
    val id: Long,
    val name: String,
    val serving: String,
    val kcal: Double,
)

data class FoodPortionItem(
    val label: String,
    val grams: Double,
)

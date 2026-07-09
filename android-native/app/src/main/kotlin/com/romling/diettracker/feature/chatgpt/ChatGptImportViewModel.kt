package com.romling.diettracker.feature.chatgpt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.romling.diettracker.data.repository.DiaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray

class ChatGptImportViewModel(
    private val diaryRepository: DiaryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ChatGptImportUiState())
    val state: StateFlow<ChatGptImportUiState> = _state.asStateFlow()

    fun updateJson(text: String) {
        _state.update { it.copy(json = text, parseError = null, savedCount = 0) }
    }

    fun parse() {
        val raw = _state.value.json.trim()
        if (raw.isBlank()) {
            _state.update { it.copy(parseError = "Cole o JSON do ChatGPT acima.", preview = emptyList()) }
            return
        }
        try {
            val array = JSONArray(raw)
            val items = (0 until array.length()).mapNotNull { i ->
                val obj = array.getJSONObject(i)
                val name = obj.optString("nome").ifBlank {
                    obj.optString("name").ifBlank {
                        obj.optString("alimento").ifBlank { null }
                    }
                } ?: return@mapNotNull null
                val grams = obj.optDouble("porcao_g", obj.optDouble("porcao", obj.optDouble("grams", obj.optDouble("gramas", 100.0))))
                val meal = parseMeal(obj.optString("refeicao").ifBlank { obj.optString("meal").ifBlank { obj.optString("refeição") } })
                val kcal = obj.optDouble("kcal", obj.optDouble("calorias", obj.optDouble("calories", 0.0)))
                val carbs = obj.optDouble("carbs", obj.optDouble("carboidratos", obj.optDouble("carbo", 0.0)))
                val protein = obj.optDouble("proteina", obj.optDouble("protein", obj.optDouble("proteína", 0.0)))
                val fat = obj.optDouble("gordura", obj.optDouble("fat", obj.optDouble("gorduras", 0.0)))
                ImportItem(
                    name = name,
                    mealType = meal,
                    mealLabel = mealLabel(meal),
                    kcal = kcal,
                    carbs = carbs,
                    protein = protein,
                    fat = fat,
                    gramsTotal = grams,
                )
            }
            if (items.isEmpty()) {
                _state.update { it.copy(parseError = "Nenhum item encontrado. Verifique o formato JSON.", preview = emptyList()) }
            } else {
                _state.update { it.copy(preview = items, parseError = null) }
            }
        } catch (e: Exception) {
            _state.update { it.copy(parseError = "JSON inválido: ${e.message?.take(120)}", preview = emptyList()) }
        }
    }

    fun saveAll(date: String, onDone: () -> Unit = {}) {
        val items = _state.value.preview
        if (items.isEmpty()) return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            items.forEach { item ->
                diaryRepository.addImportedFood(
                    date = date,
                    mealType = item.mealType,
                    name = item.name,
                    kcal = item.kcal,
                    carbs = item.carbs,
                    protein = item.protein,
                    fat = item.fat,
                    gramsTotal = item.gramsTotal,
                )
            }
            _state.update { it.copy(isSaving = false, savedCount = items.size, preview = emptyList(), json = "") }
            onDone()
        }
    }

    fun reset() {
        _state.value = ChatGptImportUiState()
    }
}

private fun parseMeal(raw: String): String {
    val s = raw.lowercase().trim()
    return when {
        s.contains("almoco") || s.contains("almoço") || s == "lunch" -> "lunch"
        s.contains("cafe") || s.contains("café") || s == "breakfast" -> "breakfast"
        s.contains("jantar") || s == "dinner" -> "dinner"
        s.contains("lanche") || s == "snack" -> "snack"
        else -> "lunch"
    }
}

private fun mealLabel(mealType: String) = when (mealType) {
    "breakfast" -> "Café da manhã"
    "lunch" -> "Almoço"
    "dinner" -> "Jantar"
    "snack" -> "Lanche"
    else -> mealType
}

class ChatGptImportViewModelFactory(
    private val diaryRepository: DiaryRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ChatGptImportViewModel(diaryRepository) as T
}

data class ChatGptImportUiState(
    val json: String = "",
    val preview: List<ImportItem> = emptyList(),
    val parseError: String? = null,
    val isSaving: Boolean = false,
    val savedCount: Int = 0,
)

data class ImportItem(
    val name: String,
    val mealType: String,
    val mealLabel: String,
    val kcal: Double,
    val carbs: Double,
    val protein: Double,
    val fat: Double,
    val gramsTotal: Double,
)

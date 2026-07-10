package com.romling.diettracker.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.romling.diettracker.core.ui.components.AppCard
import com.romling.diettracker.core.ui.components.BottomPrimaryButton
import com.romling.diettracker.core.ui.theme.AppColors
import com.romling.diettracker.core.ui.theme.AppShapes
import com.romling.diettracker.core.ui.theme.AppSpacing
import com.romling.diettracker.data.repository.DiaryBackupEntry
import java.text.Normalizer
import java.text.NumberFormat
import java.time.LocalDate
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun BackupImportScreen(
    onSave: (List<DiaryBackupEntry>) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var raw by remember { mutableStateOf("") }
    var preview by remember { mutableStateOf(emptyList<DiaryBackupEntry>()) }
    var error by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) {
                Text("←", style = MaterialTheme.typography.headlineSmall, color = AppColors.TextPrimary)
            }
            Text("Importar diário", style = MaterialTheme.typography.headlineSmall, color = AppColors.TextPrimary)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.ScreenHorizontal, vertical = AppSpacing.SectionGap),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.SectionGap),
        ) {
            AppCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Cole o backup JSON exportado pelo AppDieta.", color = AppColors.TextSecondary)
                    OutlinedTextField(
                        value = raw,
                        onValueChange = { raw = it; preview = emptyList(); error = null },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        label = { Text("Backup JSON") },
                    )
                    error?.let { Text(it, color = AppColors.Remove, style = MaterialTheme.typography.bodySmall) }
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clickable {
                                runCatching { BackupImportParser.parse(raw) }
                                    .onSuccess { preview = it; error = null }
                                    .onFailure { preview = emptyList(); error = it.message ?: "Backup inválido." }
                            },
                        shape = AppShapes.Button,
                        color = AppColors.Panel,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("Analisar backup", color = AppColors.Accent, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            if (preview.isNotEmpty()) {
                AppCard {
                    Column {
                        Text(
                            "Prévia — ${preview.size} registro(s). A importação adicionará os dados sem apagar os atuais.",
                            color = AppColors.TextSecondary,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                        preview.take(20).forEachIndexed { index, entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(entry.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text("${entry.date} · ${mealLabel(entry.mealType)} · ${NumberFormat.getNumberInstance().format(entry.grams)} g", color = AppColors.TextSecondary)
                                }
                                Text("${entry.kcal.toInt()} kcal")
                            }
                            if (index < minOf(preview.lastIndex, 19)) HorizontalDivider(color = AppColors.Line)
                        }
                        if (preview.size > 20) Text("+ ${preview.size - 20} registro(s)", color = AppColors.TextSecondary)
                    }
                }
            }
        }

        if (preview.isNotEmpty()) {
            Box(modifier = Modifier.padding(horizontal = AppSpacing.ScreenHorizontal, vertical = 12.dp)) {
                BottomPrimaryButton(
                    if (isSaving) "Importando…" else "Importar ${preview.size} registro(s)",
                    onClick = {
                        if (!isSaving) {
                            isSaving = true
                            onSave(preview)
                        }
                    },
                )
            }
        }
    }
}

internal object BackupImportParser {
    private const val MAX_INPUT_CHARS = 2_000_000
    private const val MAX_ENTRIES = 5_000

    fun parse(raw: String): List<DiaryBackupEntry> {
        require(raw.isNotBlank()) { "Cole o backup JSON." }
        require(raw.length <= MAX_INPUT_CHARS) { "Backup muito grande." }
        val text = raw.trim()
        val array = if (text.startsWith("[")) JSONArray(text) else JSONObject(text).optJSONArray("entries")
            ?: throw IllegalArgumentException("Backup sem lista de registros.")
        require(array.length() in 1..MAX_ENTRIES) { "O backup deve conter entre 1 e $MAX_ENTRIES registros." }
        return (0 until array.length()).map { index -> parseEntry(array.getJSONObject(index), index + 1) }
    }

    private fun parseEntry(obj: JSONObject, position: Int): DiaryBackupEntry {
        val date = obj.firstText("date", "data")
        require(runCatching { LocalDate.parse(date) }.isSuccess) { "Data inválida no registro $position." }
        val name = obj.firstText("name", "food_name", "nome")
        require(name.isNotBlank()) { "Registro $position sem nome." }
        val grams = obj.number("grams", "grams_total", "gramas")
        require(grams > 0) { "Porção do registro $position deve ser positiva." }
        return DiaryBackupEntry(
            date = date,
            mealType = normalizeMeal(obj.firstText("meal", "meal_type", "refeicao", "refeição")),
            name = name,
            grams = grams,
            kcal = obj.number("kcal", "calorias"),
            carbs = obj.number("carbs", "carboidratos"),
            protein = obj.number("protein", "proteina", "proteína"),
            fat = obj.number("fat", "gordura", "gorduras"),
        )
    }

    private fun JSONObject.firstText(vararg keys: String): String =
        keys.firstNotNullOfOrNull { key -> optString(key).trim().takeIf { it.isNotBlank() } }.orEmpty()

    private fun JSONObject.number(vararg keys: String): Double {
        val key = keys.firstOrNull { has(it) && !isNull(it) } ?: return 0.0
        val value = optDouble(key, Double.NaN)
        require(value.isFinite() && value >= 0) { "Valor inválido em '$key'." }
        return value
    }

    private fun normalizeMeal(raw: String): String {
        val meal = Normalizer.normalize(raw.lowercase().trim(), Normalizer.Form.NFD)
            .filterNot { Character.getType(it) == Character.NON_SPACING_MARK.toInt() }
            .replace('_', ' ')
        return when (meal) {
            "breakfast", "cafe", "cafe da manha" -> "breakfast"
            "lunch", "almoco" -> "lunch"
            "dinner", "jantar" -> "dinner"
            "snack", "lanche", "lanches" -> "snack"
            else -> throw IllegalArgumentException("Refeição inválida: '$raw'.")
        }
    }
}

private fun mealLabel(mealType: String) = when (mealType) {
    "breakfast" -> "Café da manhã"
    "lunch" -> "Almoço"
    "dinner" -> "Jantar"
    else -> "Lanche"
}

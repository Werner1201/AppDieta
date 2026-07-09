package com.romling.diettracker.feature.chatgpt

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.romling.diettracker.core.ui.components.AppCard
import com.romling.diettracker.core.ui.components.BottomPrimaryButton
import com.romling.diettracker.core.ui.theme.AppColors
import com.romling.diettracker.core.ui.theme.AppShapes
import com.romling.diettracker.core.ui.theme.AppSpacing

@Composable
fun ChatGptImportScreen(
    state: ChatGptImportUiState,
    onJsonChange: (String) -> Unit,
    onParse: () -> Unit,
    onSaveAll: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppColors.Background)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onClose) {
                    Text(text = "←", style = MaterialTheme.typography.headlineSmall, color = AppColors.TextPrimary)
                }
                Text(
                    text = "Importar do ChatGPT",
                    style = MaterialTheme.typography.headlineSmall,
                    color = AppColors.TextPrimary,
                )
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
                        Text(
                            text = "Cole o JSON gerado pelo ChatGPT:",
                            style = MaterialTheme.typography.labelMedium,
                            color = AppColors.TextSecondary,
                        )
                        OutlinedTextField(
                            value = state.json,
                            onValueChange = onJsonChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            placeholder = { Text("""[{"nome": "Frango", "porcao_g": 150, "refeicao": "almoco", "kcal": 195, "proteina": 40, "carbs": 0, "gordura": 5}]""") },
                            textStyle = MaterialTheme.typography.bodySmall,
                        )
                        if (state.parseError != null) {
                            Text(
                                text = state.parseError,
                                style = MaterialTheme.typography.bodySmall,
                                color = AppColors.Remove,
                            )
                        }
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clickable(onClick = onParse),
                            shape = AppShapes.Button,
                            color = AppColors.Panel,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Analisar JSON",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = AppColors.Accent,
                                )
                            }
                        }
                    }
                }

                if (state.savedCount > 0) {
                    AppCard {
                        Text(
                            text = "✅ ${state.savedCount} item(s) salvo(s) com sucesso!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = AppColors.TextPrimary,
                        )
                    }
                }

                if (state.preview.isNotEmpty()) {
                    AppCard {
                        Column {
                            Text(
                                text = "Prévia — ${state.preview.size} item(s)",
                                style = MaterialTheme.typography.labelMedium,
                                color = AppColors.TextSecondary,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                            state.preview.forEachIndexed { index, item ->
                                PreviewRow(item = item)
                                if (index < state.preview.lastIndex) {
                                    HorizontalDivider(color = AppColors.Line, thickness = 1.dp)
                                }
                            }
                        }
                    }
                }
            }

            if (state.preview.isNotEmpty()) {
                Box(modifier = Modifier.padding(horizontal = AppSpacing.ScreenHorizontal, vertical = 12.dp)) {
                    BottomPrimaryButton(
                        text = if (state.isSaving) "Salvando…" else "Salvar tudo (${state.preview.size})",
                        onClick = onSaveAll,
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewRow(item: ImportItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${item.mealLabel} · ${item.gramsTotal.toInt()} g",
                style = MaterialTheme.typography.bodySmall,
                color = AppColors.TextSecondary,
            )
        }
        Text(
            text = "${item.kcal.toInt()} kcal",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.TextPrimary,
        )
    }
}

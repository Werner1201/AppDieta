package com.romling.diettracker.feature.chatgpt

import android.content.Intent
import android.net.Uri
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
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
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }

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
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "1. Copie o prompt abaixo e cole no ChatGPT:",
                            style = MaterialTheme.typography.labelMedium,
                            color = AppColors.TextSecondary,
                        )
                        Text(
                            text = CHATGPT_PROMPT,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.TextPrimary,
                        )
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clickable {
                                    clipboard.setText(AnnotatedString(CHATGPT_PROMPT))
                                    copied = true
                                },
                            shape = AppShapes.Button,
                            color = if (copied) AppColors.Green else AppColors.Panel,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (copied) "✅ Copiado!" else "Copiar prompt",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = AppColors.Accent,
                                )
                            }
                        }
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clickable {
                                    runCatching {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(CUSTOM_GPT_URL)))
                                    }
                                },
                            shape = AppShapes.Button,
                            color = AppColors.Panel,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Abrir GPT personalizado",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = AppColors.Accent,
                                )
                            }
                        }
                    }
                }

                AppCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "2. Cole o JSON que o ChatGPT gerou:",
                            style = MaterialTheme.typography.labelMedium,
                            color = AppColors.TextSecondary,
                        )
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clickable {
                                    onJsonChange(clipboard.getText()?.text.orEmpty())
                                    onParse()
                                },
                            shape = AppShapes.Button,
                            color = AppColors.Panel,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "Importar do clipboard",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = AppColors.Accent,
                                )
                            }
                        }
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

private const val CUSTOM_GPT_URL = "https://chatgpt.com/g/g-6a4594e4a6c88191b132ffc25a95ff0d-importador-de-refeicoes-para-app-local"

private const val CHATGPT_PROMPT = """Me dê o JSON com todas as refeições de hoje no formato abaixo. Responda APENAS com o JSON, sem explicações.

Formato:
[{"nome": "nome do alimento", "porcao_g": 100, "refeicao": "almoco", "kcal": 200, "proteina": 15, "carbs": 20, "gordura": 5}]

Valores de refeicao: "cafe" (café da manhã), "almoco" (almoço), "jantar", "lanche"

Refeições de hoje:
"""

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

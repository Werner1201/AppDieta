package com.romling.diettracker.feature.weight

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.romling.diettracker.core.ui.components.AppCard
import com.romling.diettracker.core.ui.components.BottomPrimaryButton
import com.romling.diettracker.core.ui.theme.AppColors
import com.romling.diettracker.core.ui.theme.AppSpacing
import com.romling.diettracker.feature.today.TodayWeightEntry
import com.romling.diettracker.feature.today.TodayWeightSummary

@Composable
fun WeightScreen(
    weight: TodayWeightSummary,
    history: List<TodayWeightEntry>,
    onAddWeight: (Double) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var draftKg by remember(weight.currentKg) { mutableStateOf(weight.currentKg) }

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
                    text = "Peso",
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
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text(
                            text = "${"%.1f".format(draftKg)} kg",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = "Objetivo: ${"%.1f".format(weight.goalKg)} kg",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.TextSecondary,
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            WeightAdjustButton(text = "−") { draftKg = maxOf(1.0, (draftKg - 0.1 + 0.001).let { "%.1f".format(it).toDouble() }) }
                            WeightAdjustButton(text = "+") { draftKg = "%.1f".format(draftKg + 0.1).toDouble() }
                        }
                    }
                }

                if (history.isNotEmpty()) {
                    AppCard {
                        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                            Text(
                                text = "Histórico",
                                style = MaterialTheme.typography.labelMedium,
                                color = AppColors.TextSecondary,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )
                            history.forEachIndexed { index, entry ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = entry.date,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AppColors.TextSecondary,
                                    )
                                    Text(
                                        text = "${"%.1f".format(entry.kg)} kg",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppColors.TextPrimary,
                                    )
                                }
                                if (index < history.lastIndex) {
                                    HorizontalDivider(color = AppColors.Line, thickness = 1.dp)
                                }
                            }
                        }
                    }
                }
            }

            Box(modifier = Modifier.padding(horizontal = AppSpacing.ScreenHorizontal, vertical = 12.dp)) {
                BottomPrimaryButton(
                    text = "Registrar ${"%.1f".format(draftKg)} kg",
                    onClick = { onAddWeight(draftKg) },
                )
            }
        }
    }
}

@Composable
private fun WeightAdjustButton(text: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(52.dp)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = AppColors.Background,
        border = androidx.compose.foundation.BorderStroke(2.dp, AppColors.TextPrimary),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, style = MaterialTheme.typography.headlineMedium, color = AppColors.TextPrimary)
        }
    }
}

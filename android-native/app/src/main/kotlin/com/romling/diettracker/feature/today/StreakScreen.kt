package com.romling.diettracker.feature.today

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.romling.diettracker.core.ui.components.AppCard
import com.romling.diettracker.core.ui.theme.AppColors
import com.romling.diettracker.core.ui.theme.AppSpacing

@Composable
fun StreakScreen(streak: TodayStreakSummary, onClose: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(horizontal = AppSpacing.ScreenHorizontal, vertical = 34.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "×",
                modifier = Modifier
                    .clickable(onClick = onClose)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(text = "Sequência", style = MaterialTheme.typography.headlineSmall)
        }
        AppCard {
            StreakMetric(label = "Atual", value = streak.current)
            StreakMetric(label = "Maior sequência", value = streak.best)
            StreakMetric(label = "Dias ativos", value = streak.activeDays)
        }
    }
}

@Composable
private fun StreakMetric(label: String, value: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = value.toString(), style = MaterialTheme.typography.titleLarge)
    }
}

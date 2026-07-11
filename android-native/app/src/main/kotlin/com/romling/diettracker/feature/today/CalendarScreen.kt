package com.romling.diettracker.feature.today

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.romling.diettracker.core.ui.theme.AppColors
import com.romling.diettracker.core.ui.theme.AppSpacing
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    greenDays: Map<LocalDate, Boolean>,
    selectedDate: LocalDate,
    onDayClick: (LocalDate) -> Unit,
    onMonthChanged: (YearMonth) -> Unit,
    onClose: () -> Unit,
) {
    var displayedMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }

    Column(
        modifier = Modifier
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
                color = AppColors.TextPrimary,
            )
            Text(
                text = "Calendário",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineSmall,
                color = AppColors.TextPrimary,
            )
        }

        MonthGrid(
            displayedMonth = displayedMonth,
            greenDays = greenDays,
            selectedDate = selectedDate,
            onDayClick = onDayClick,
            onPreviousMonth = {
                displayedMonth = displayedMonth.minusMonths(1)
                onMonthChanged(displayedMonth)
            },
            onNextMonth = {
                displayedMonth = displayedMonth.plusMonths(1)
                onMonthChanged(displayedMonth)
            },
        )
    }
}

@Composable
private fun MonthGrid(
    displayedMonth: YearMonth,
    greenDays: Map<LocalDate, Boolean>,
    selectedDate: LocalDate,
    onDayClick: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Month header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "‹",
                modifier = Modifier
                    .clickable(onClick = onPreviousMonth)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.headlineMedium,
                color = AppColors.Accent,
            )
            Text(
                text = displayedMonth.month.getDisplayName(TextStyle.FULL, Locale("pt", "BR"))
                    .replaceFirstChar { it.uppercase() } + " ${displayedMonth.year}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary,
            )
            Text(
                text = "›",
                modifier = Modifier
                    .clickable(onClick = onNextMonth)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.headlineMedium,
                color = AppColors.Accent,
            )
        }

        // Day-of-week headers
        val daysOfWeek = listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = AppColors.TextSecondary,
                )
            }
        }

        // Day grid
        val firstDay = displayedMonth.atDay(1)
        // Sunday = 0 offset (DayOfWeek.SUNDAY = 7 in ISO, so adjust)
        val startOffset = (firstDay.dayOfWeek.value % 7)
        val totalDays = displayedMonth.lengthOfMonth()
        val cells = startOffset + totalDays
        val rows = (cells + 6) / 7

        repeat(rows) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val cellIndex = row * 7 + col
                    val dayNum = cellIndex - startOffset + 1
                    if (dayNum in 1..totalDays) {
                        val date = displayedMonth.atDay(dayNum)
                        val isSelected = date == selectedDate
                        val isGreen = greenDays[date] == true
                        val isToday = date == LocalDate.now()
                        DayCell(
                            day = dayNum,
                            isSelected = isSelected,
                            isGreen = isGreen,
                            isToday = isToday,
                            modifier = Modifier.weight(1f),
                            onClick = { onDayClick(date) },
                        )
                    } else {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isSelected: Boolean,
    isGreen: Boolean,
    isToday: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val bgColor = when {
        isSelected -> AppColors.Accent
        isGreen -> AppColors.Accent.copy(alpha = 0.25f)
        else -> androidx.compose.ui.graphics.Color.Transparent
    }
    val textColor = when {
        isSelected -> AppColors.Background
        isGreen -> AppColors.Accent
        isToday -> AppColors.Accent
        else -> AppColors.TextSecondary
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
        )
    }
}

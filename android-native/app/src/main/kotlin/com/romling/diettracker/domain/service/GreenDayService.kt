package com.romling.diettracker.domain.service

import com.romling.diettracker.data.local.entity.DiaryEntryEntity

class GreenDayService {
    fun isGreenDay(
        entries: List<DiaryEntryEntity>,
        dailyKcal: Double,
        dailyProtein: Double,
    ): Boolean {
        if (entries.isEmpty()) return false
        return entries.sumOf { it.kcal } <= dailyKcal &&
            entries.sumOf { it.protein } >= dailyProtein * 0.8
    }
}

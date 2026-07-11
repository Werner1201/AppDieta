package com.romling.diettracker.feature.settings

import com.romling.diettracker.data.repository.GoalSettings
import com.romling.diettracker.data.repository.isValid
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsScreenTest {
    @Test
    fun validatesChatGptHttpUrl() {
        val fallback = "https://chatgpt.com"

        assertEquals("https://example.com/gpt", validatedChatGptUrl(" https://example.com/gpt ", fallback))
        assertEquals("HTTPS://example.com/gpt", validatedChatGptUrl("HTTPS://example.com/gpt", fallback))
        assertEquals(fallback, validatedChatGptUrl("intent://unsafe", fallback))
        assertEquals(fallback, validatedChatGptUrl("https://", fallback))
    }

    @Test
    fun validatesCompleteSettingsBeforePersistence() {
        assertTrue(GoalSettings().isValid())
        assertFalse(GoalSettings(dailyKcal = 0.0).isValid())
        assertFalse(GoalSettings(dailyProtein = Double.POSITIVE_INFINITY).isValid())
        assertFalse(GoalSettings(dailyWaterMl = -1).isValid())
        assertFalse(GoalSettings(chatGptUrl = "intent://unsafe").isValid())
        assertFalse(GoalSettings(chatGptPrompt = " ").isValid())
    }

    @Test
    fun validatesGoalInputsUsedBySaveButton() {
        assertTrue(areGoalInputsValid("2333", "284", "114", "75", "2000", "80"))
        assertFalse(areGoalInputsValid("0", "284", "114", "75", "2000", "80"))
        assertFalse(areGoalInputsValid("2333", "-1", "114", "75", "2000", "80"))
        assertFalse(areGoalInputsValid("2333", "284", "Infinity", "75", "2000", "80"))
        assertFalse(areGoalInputsValid("2333", "284", "114", "75", "0", "80"))
    }
}

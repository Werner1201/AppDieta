package com.romling.diettracker.feature.settings

import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsScreenTest {
    @Test
    fun validatesChatGptHttpUrl() {
        val fallback = "https://chatgpt.com"

        assertEquals("https://example.com/gpt", validatedChatGptUrl(" https://example.com/gpt ", fallback))
        assertEquals(fallback, validatedChatGptUrl("intent://unsafe", fallback))
        assertEquals(fallback, validatedChatGptUrl("https://", fallback))
    }
}

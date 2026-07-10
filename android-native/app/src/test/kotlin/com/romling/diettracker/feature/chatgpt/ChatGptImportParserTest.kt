package com.romling.diettracker.feature.chatgpt

import java.nio.charset.StandardCharsets
import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ChatGptImportParserTest {
    private val json = """[{"nome":"Café","porcao_g":237,"refeicao":"café da manhã","kcal":2,"proteina":0.3,"carbs":0,"gordura":0}]"""

    @Test
    fun acceptsJsonMarkdownAndBase64Url() {
        val encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(json.toByteArray(StandardCharsets.UTF_8))

        assertEquals("breakfast", ChatGptImportParser.parse(json).single().mealType)
        assertEquals("Café", ChatGptImportParser.parse("```json\n$json\n```").single().name)
        assertEquals(237.0, ChatGptImportParser.parse(encoded).single().gramsTotal)
        assertEquals(2.0, ChatGptImportParser.parse("romlingdiet://import/chatgpt?payload=$encoded").single().kcal)
    }

    @Test
    fun acceptsWebPayloadSchema() {
        val payload = """{"meal_type":"almoco","items":[{"name":"Arroz","estimated_grams":125,"kcal":160,"carbs":35,"protein":3,"fat":0.3}]}"""
        val item = ChatGptImportParser.parse(payload).single()

        assertEquals("lunch", item.mealType)
        assertEquals(125.0, item.gramsTotal)

        val breakfast = payload.replace("almoco", "cafe_da_manha")
        assertEquals("breakfast", ChatGptImportParser.parse(breakfast).single().mealType)
    }

    @Test
    fun rejectsNegativeNutrition() {
        assertFailsWith<IllegalArgumentException> {
            ChatGptImportParser.parse("""[{"nome":"Teste","refeicao":"almoco","kcal":-1}]""")
        }
    }

    @Test
    fun rejectsInvalidMealAndPortion() {
        assertFailsWith<IllegalArgumentException> {
            ChatGptImportParser.parse("""[{"nome":"Teste","refeicao":"ceia","kcal":10}]""")
        }
        assertFailsWith<IllegalArgumentException> {
            ChatGptImportParser.parse("""[{"nome":"Teste","refeicao":"lanche","porcao_g":0,"kcal":10}]""")
        }
    }

    @Test
    fun rejectsOversizedInput() {
        assertFailsWith<IllegalArgumentException> { ChatGptImportParser.parse("a".repeat(100_001)) }

        val largeJson = """[{"nome":"${"a".repeat(66_000)}","refeicao":"almoco"}]"""
        val encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(largeJson.toByteArray(StandardCharsets.UTF_8))
        assertFailsWith<IllegalArgumentException> { ChatGptImportParser.parse(encoded) }
    }

    @Test
    fun rejectsTooManyItemsAndNonFiniteNumbers() {
        val item = """{"nome":"Teste","refeicao":"almoco","kcal":1}"""
        val tooMany = List(201) { item }.joinToString(prefix = "[", postfix = "]")

        assertFailsWith<IllegalArgumentException> { ChatGptImportParser.parse(tooMany) }
        assertFailsWith<IllegalArgumentException> {
            ChatGptImportParser.parse("""[{"nome":"Teste","refeicao":"almoco","kcal":1e309}]""")
        }
    }
}

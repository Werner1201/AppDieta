package com.romling.diettracker.feature.chatgpt

import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.text.Normalizer
import java.util.Base64
import org.json.JSONArray
import org.json.JSONObject

internal object ChatGptImportParser {
    private const val MAX_INPUT_CHARS = 100_000
    private const val MAX_DECODED_BYTES = 65_536
    private const val MAX_ITEMS = 200

    fun parse(raw: String): List<ImportItem> {
        require(raw.isNotBlank()) { "Cole o JSON ou payload do ChatGPT." }
        require(raw.length <= MAX_INPUT_CHARS) { "Conteúdo muito grande." }

        val candidate = extractMarkdown(raw.trim())
        val payload = extractLinkPayload(candidate) ?: candidate
        val json = if (payload.startsWith("[") || payload.startsWith("{")) payload else decodeBase64Url(payload)
        require(json.toByteArray().size <= MAX_DECODED_BYTES) { "Payload muito grande." }

        val payloadJson = jsonArray(json)
        require(payloadJson.items.length() in 1..MAX_ITEMS) { "A importação deve conter entre 1 e $MAX_ITEMS itens." }
        return (0 until payloadJson.items.length()).map { index ->
            parseItem(payloadJson.items.getJSONObject(index), index + 1, payloadJson.mealType)
        }
    }

    private fun extractMarkdown(raw: String): String {
        val firstFence = raw.indexOf("```")
        if (firstFence < 0) return raw
        val contentStart = raw.indexOf('\n', firstFence)
        val closingFence = if (contentStart >= 0) raw.indexOf("```", contentStart + 1) else -1
        require(contentStart >= 0 && closingFence > contentStart) { "Bloco Markdown incompleto." }
        return raw.substring(contentStart + 1, closingFence).trim()
    }

    private fun extractLinkPayload(raw: String): String? {
        if (!raw.contains("payload=")) return null
        val query = runCatching { URI(raw).rawQuery }.getOrNull() ?: return null
        val value = query.split('&')
            .firstOrNull { it.substringBefore('=') == "payload" }
            ?.substringAfter('=', "")
            ?.takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("Link sem payload válido.")
        return URLDecoder.decode(value, "UTF-8")
    }

    private fun decodeBase64Url(value: String): String {
        val bytes = try {
            Base64.getUrlDecoder().decode(value)
        } catch (_: IllegalArgumentException) {
            throw IllegalArgumentException("JSON ou payload base64url inválido.")
        }
        require(bytes.size <= MAX_DECODED_BYTES) { "Payload muito grande." }
        return bytes.toString(StandardCharsets.UTF_8).trim()
    }

    private fun jsonArray(json: String): ParsedPayload = when {
        json.startsWith("[") -> ParsedPayload(JSONArray(json))
        json.startsWith("{") -> {
            val root = JSONObject(json)
            val items = root.optJSONArray("items")
                ?: root.optJSONArray("alimentos")
                ?: root.optJSONArray("refeicoes")
                ?: JSONArray().put(root)
            ParsedPayload(
                items = items,
                mealType = root.firstText("meal_type", "mealType", "refeicao", "refeição", "meal"),
            )
        }
        else -> throw IllegalArgumentException("Payload não contém JSON válido.")
    }

    private fun parseItem(obj: JSONObject, position: Int, fallbackMealType: String): ImportItem {
        val name = obj.firstText("nome", "name", "alimento")
        require(name.isNotBlank()) { "Item $position sem nome." }

        val mealType = parseMeal(obj.firstText("meal_type", "refeicao", "refeição", "meal").ifBlank { fallbackMealType })
        val grams = obj.number(100.0, "estimated_grams", "porcao_g", "porcao", "grams", "gramas")
        require(grams > 0) { "Porção do item $position deve ser positiva." }

        return ImportItem(
            name = name,
            mealType = mealType,
            mealLabel = mealLabel(mealType),
            kcal = obj.number(0.0, "kcal", "calorias", "calories"),
            carbs = obj.number(0.0, "carbs", "carboidratos", "carbo"),
            protein = obj.number(0.0, "proteina", "proteína", "protein"),
            fat = obj.number(0.0, "gordura", "gorduras", "fat"),
            gramsTotal = grams,
        )
    }

    private fun JSONObject.firstText(vararg keys: String): String =
        keys.firstNotNullOfOrNull { key -> optString(key).trim().takeIf { it.isNotBlank() } }.orEmpty()

    private fun JSONObject.number(default: Double, vararg keys: String): Double {
        val key = keys.firstOrNull { has(it) && !isNull(it) } ?: return default
        val value = optDouble(key, Double.NaN)
        require(value.isFinite() && value >= 0) { "Valor inválido em '$key'." }
        return value
    }

    private fun parseMeal(raw: String): String {
        val normalized = Normalizer.normalize(raw.trim().lowercase(), Normalizer.Form.NFD)
            .filterNot { Character.getType(it) == Character.NON_SPACING_MARK.toInt() }
            .replace('_', ' ')
        return when (normalized) {
            "cafe", "cafe da manha", "breakfast" -> "breakfast"
            "almoco", "lunch" -> "lunch"
            "jantar", "dinner" -> "dinner"
            "lanche", "lanches", "snack" -> "snack"
            else -> throw IllegalArgumentException("Refeição inválida: '$raw'.")
        }
    }

    private fun mealLabel(mealType: String) = when (mealType) {
        "breakfast" -> "Café da manhã"
        "lunch" -> "Almoço"
        "dinner" -> "Jantar"
        else -> "Lanche"
    }

    private data class ParsedPayload(val items: JSONArray, val mealType: String = "")
}

data class ImportItem(
    val name: String,
    val mealType: String,
    val mealLabel: String,
    val kcal: Double,
    val carbs: Double,
    val protein: Double,
    val fat: Double,
    val gramsTotal: Double,
)

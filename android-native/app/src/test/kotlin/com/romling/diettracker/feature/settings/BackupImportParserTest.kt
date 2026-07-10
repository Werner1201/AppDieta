package com.romling.diettracker.feature.settings

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BackupImportParserTest {
    @Test
    fun parsesExportedDiaryBackup() {
        val json = """{"app":"AppDieta","entries":[{"date":"2026-07-01","meal":"lunch","name":"Arroz","grams":0.5,"kcal":1,"carbs":0.1,"protein":0.1,"fat":0}]}"""
        val entry = BackupImportParser.parse(json).single()

        assertEquals("2026-07-01", entry.date)
        assertEquals("lunch", entry.mealType)
        assertEquals("Arroz", entry.name)
        assertEquals(0.5, entry.grams)
    }

    @Test
    fun rejectsInvalidBackupValues() {
        assertFailsWith<IllegalArgumentException> {
            BackupImportParser.parse("""[{"date":"invalida","meal":"lunch","name":"Arroz","grams":100}]""")
        }
        assertFailsWith<IllegalArgumentException> {
            BackupImportParser.parse("""[{"date":"2026-07-01","meal":"ceia","name":"Arroz","grams":100}]""")
        }
        assertFailsWith<IllegalArgumentException> {
            BackupImportParser.parse("""[{"date":"2026-07-01","meal":"lunch","name":"Arroz","grams":-1}]""")
        }
    }
}

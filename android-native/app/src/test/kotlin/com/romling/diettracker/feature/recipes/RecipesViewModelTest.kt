package com.romling.diettracker.feature.recipes

import com.romling.diettracker.data.local.entity.RecipeIngredientEntity
import kotlin.test.Test
import kotlin.test.assertEquals

class RecipesViewModelTest {
    @Test
    fun recipeTotalsSumAllIngredients() {
        val totals = recipeTotals(
            listOf(
                ingredient(grams = 100.0, kcal = 120.0, carbs = 10.0, protein = 20.0, fat = 3.0),
                ingredient(grams = 50.0, kcal = 80.0, carbs = 5.0, protein = 4.0, fat = 6.0),
            ),
        )

        assertEquals(RecipeTotals(150.0, 200.0, 15.0, 24.0, 9.0), totals)
    }
}

private fun ingredient(
    grams: Double,
    kcal: Double,
    carbs: Double,
    protein: Double,
    fat: Double,
) = RecipeIngredientEntity(
    recipeId = 1,
    foodNameSnapshot = "Ingrediente",
    grams = grams,
    kcal = kcal,
    carbs = carbs,
    protein = protein,
    fat = fat,
)

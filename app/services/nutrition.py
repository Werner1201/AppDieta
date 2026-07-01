from __future__ import annotations

MACROS = ("kcal", "carbs", "protein", "fat", "fiber", "sugar", "sodium_mg")


def totals_for_food(food: dict, grams: float) -> dict[str, float]:
    if grams <= 0:
        raise ValueError("grams must be positive")
    return {
        "kcal": round(food["kcal_100g"] * grams / 100),
        "carbs": round(food["carbs_100g"] * grams / 100, 1),
        "protein": round(food["protein_100g"] * grams / 100, 1),
        "fat": round(food["fat_100g"] * grams / 100, 1),
        "fiber": round(food["fiber_100g"] * grams / 100, 1),
        "sugar": round(food["sugar_100g"] * grams / 100, 1),
        "sodium_mg": round(food["sodium_mg_100g"] * grams / 100, 1),
    }


def sum_entries(entries: list[dict]) -> dict[str, float]:
    return {key: round(sum(float(e[key]) for e in entries), 1) for key in MACROS}

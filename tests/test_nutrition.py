from pathlib import Path

from app.db import connect, init_db
from app.services.diary import add_entry, day_summary, is_green_day, streak
from app.services.nutrition import totals_for_food


def db(tmp_path: Path):
    path = tmp_path / "test.sqlite"
    init_db(path)
    conn = connect(path)
    conn.execute(
        "INSERT INTO foods(name, category, aliases, kcal_100g, carbs_100g, protein_100g, fat_100g, fiber_100g, sugar_100g, sodium_mg_100g, default_unit, grams_per_default_unit, source) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)",
        ("Frango", "Proteínas", "frango", 165, 0, 31, 3.6, 0, 0, 75, "100 g", 100, "teste"),
    )
    conn.commit()
    return conn


def test_macro_calculation():
    food = {"kcal_100g": 200, "carbs_100g": 10, "protein_100g": 20, "fat_100g": 5, "fiber_100g": 2, "sugar_100g": 1, "sodium_mg_100g": 30}
    assert totals_for_food(food, 50)["protein"] == 10


def test_day_and_meal_totals(tmp_path):
    conn = db(tmp_path)
    add_entry(conn, "2026-07-01", "lunch", 1, 1, 200)
    summary = day_summary(conn, "2026-07-01")
    assert summary["totals"]["kcal"] == 330
    assert next(m for m in summary["meals"] if m["key"] == "lunch")["totals"]["protein"] == 62


def test_green_day_and_streak(tmp_path):
    conn = db(tmp_path)
    add_entry(conn, "2026-06-30", "lunch", 1, 1, 300)
    add_entry(conn, "2026-07-01", "lunch", 1, 1, 300)
    assert is_green_day(conn, "2026-07-01")
    assert streak(conn, "2026-07-01")["current"] == 2


def test_water_and_custom_food(tmp_path):
    conn = db(tmp_path)
    conn.execute("INSERT INTO water_entries(date, amount_ml) VALUES('2026-07-01', 250)")
    conn.execute("INSERT INTO foods(name, category, aliases, kcal_100g, carbs_100g, protein_100g, fat_100g, default_unit, grams_per_default_unit, source, is_custom) VALUES('Meu alimento','Personalizado','meu',100,10,5,3,'100 g',100,'usuário',1)")
    water = conn.execute("SELECT SUM(amount_ml) AS total FROM water_entries").fetchone()["total"]
    custom = conn.execute("SELECT is_custom FROM foods WHERE name='Meu alimento'").fetchone()["is_custom"]
    assert water == 250
    assert custom == 1

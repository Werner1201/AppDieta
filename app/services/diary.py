from __future__ import annotations

from datetime import date, timedelta
from sqlite3 import Connection

from app.services.nutrition import sum_entries, totals_for_food

MEALS = {
    "breakfast": ("Café da manhã", "☕", "meal_breakfast_kcal"),
    "lunch": ("Almoço", "🍲", "meal_lunch_kcal"),
    "dinner": ("Jantar", "🥗", "meal_dinner_kcal"),
    "snack": ("Lanches", "⌛", "meal_snack_kcal"),
}


def settings(conn: Connection) -> dict[str, float]:
    rows = conn.execute("SELECT key, value FROM settings").fetchall()
    return {r["key"]: float(r["value"]) for r in rows}


def entries_for_date(conn: Connection, day: str) -> list[dict]:
    rows = conn.execute(
        """
        SELECT diary_entries.*, foods.name AS food_name
        FROM diary_entries JOIN foods ON foods.id = diary_entries.food_id
        WHERE date = ?
        ORDER BY created_at, id
        """,
        (day,),
    ).fetchall()
    return [dict(r) for r in rows]


def add_entry(conn: Connection, day: str, meal_type: str, food_id: int, quantity: float, grams: float) -> None:
    if meal_type not in MEALS:
        raise ValueError("invalid meal")
    if quantity <= 0 or grams <= 0:
        raise ValueError("quantity and grams must be positive")
    food = dict(conn.execute("SELECT * FROM foods WHERE id = ?", (food_id,)).fetchone())
    totals = totals_for_food(food, grams)
    conn.execute(
        """
        INSERT INTO diary_entries(date, meal_type, food_id, quantity, unit_label, grams_total,
        kcal, carbs, protein, fat, fiber, sugar, sodium_mg)
        VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        (
            day,
            meal_type,
            food_id,
            quantity,
            food["default_unit"],
            grams,
            totals["kcal"],
            totals["carbs"],
            totals["protein"],
            totals["fat"],
            totals["fiber"],
            totals["sugar"],
            totals["sodium_mg"],
        ),
    )


def remove_entry(conn: Connection, entry_id: int) -> None:
    conn.execute("DELETE FROM diary_entries WHERE id = ?", (entry_id,))


def day_summary(conn: Connection, day: str) -> dict:
    cfg = settings(conn)
    entries = entries_for_date(conn, day)
    totals = sum_entries(entries)
    meals = []
    for key, (label, icon, goal_key) in MEALS.items():
        meal_entries = [e for e in entries if e["meal_type"] == key]
        meal_total = sum_entries(meal_entries)
        meals.append({
            "key": key,
            "label": label,
            "icon": icon,
            "goal": cfg[goal_key],
            "totals": meal_total,
            "items": ", ".join(e["food_name"] for e in meal_entries[:3]),
        })
    return {
        "date": day,
        "week": date.fromisoformat(day).isocalendar().week,
        "settings": cfg,
        "entries": entries,
        "totals": totals,
        "remaining": max(0, round(cfg["daily_kcal"] - totals["kcal"])),
        "meals": meals,
    }


def is_green_day(conn: Connection, day: str) -> bool:
    summary = day_summary(conn, day)
    return bool(
        summary["entries"]
        and summary["totals"]["kcal"] <= summary["settings"]["daily_kcal"]
        and summary["totals"]["protein"] >= summary["settings"]["daily_protein"] * 0.8
    )


def streak(conn: Connection, end_day: str) -> dict:
    active = {r["date"] for r in conn.execute("SELECT DISTINCT date FROM diary_entries").fetchall()}
    current = 0
    cursor = date.fromisoformat(end_day)
    while cursor.isoformat() in active:
        current += 1
        cursor -= timedelta(days=1)
    best = run = 0
    for day in sorted(active):
        d = date.fromisoformat(day)
        run = run + 1 if "prev" in locals() and d == prev + timedelta(days=1) else 1
        best = max(best, run)
        prev = d
    return {"current": current, "best": best, "active_days": len(active)}

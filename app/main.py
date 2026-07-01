from __future__ import annotations

import calendar as cal
from datetime import date, datetime

from fastapi import FastAPI, Form, Request
from fastapi.responses import RedirectResponse
from fastapi.staticfiles import StaticFiles
from fastapi.templating import Jinja2Templates

from app.db import connect, init_db
from app.services.diary import MEALS, add_entry, day_summary, is_green_day, remove_entry, settings, streak
from app.services.tips import tips

app = FastAPI(title="Dieta Local")
app.mount("/static", StaticFiles(directory="app/static"), name="static")
templates = Jinja2Templates(directory="app/templates")


@app.on_event("startup")
def startup() -> None:
    init_db()


def today() -> str:
    return date.today().isoformat()


def redirect(path: str = "/") -> RedirectResponse:
    return RedirectResponse(path, status_code=303)


def food_search_sql(q: str, category: str) -> tuple[str, tuple]:
    like = f"%{q}%"
    return (
        """
        SELECT * FROM foods
        WHERE (?='' OR name LIKE ? OR aliases LIKE ?)
        AND (?='' OR category=?)
        GROUP BY name
        ORDER BY name
        LIMIT 100
        """,
        (q, like, like, category, category),
    )


def frequent_foods(conn, meal_type: str, q: str, category: str):
    sql, params = food_search_sql(q, category)
    rows = [dict(r) for r in conn.execute(sql, params).fetchall()]
    counts = {
        r["food_id"]: r["uses"]
        for r in conn.execute(
            "SELECT food_id, COUNT(*) AS uses FROM diary_entries WHERE meal_type=? GROUP BY food_id",
            (meal_type,),
        )
    }
    fallback = {
        "breakfast": ("Cafe", "Pao", "Ovo", "Leite", "Banana", "Aveia"),
        "lunch": ("Arroz", "Feijao", "Frango", "Carne", "Batata", "Salada"),
        "dinner": ("Frango", "Ovo", "Tilapia", "Salada", "Sopa", "Legumes"),
        "snack": ("Banana", "Iogurte", "Pao", "Chocolate", "Pao De Queijo", "Granola"),
    }.get(meal_type, ())

    def rank(food: dict) -> tuple:
        name = food["name"].lower()
        fallback_rank = next((i for i, item in enumerate(fallback) if item.lower() in name), 99)
        return (-counts.get(food["id"], 0), fallback_rank, food["name"])

    return sorted(rows, key=rank)


@app.get("/")
def home(request: Request, day: str | None = None):
    day = day or today()
    with connect() as conn:
        summary = day_summary(conn, day)
        water_ml = sum(r["amount_ml"] for r in conn.execute("SELECT amount_ml FROM water_entries WHERE date=?", (day,)))
        weight = conn.execute("SELECT weight_kg FROM weight_entries ORDER BY date DESC, id DESC LIMIT 1").fetchone()
        return templates.TemplateResponse("today.html", {
            "request": request,
            "summary": summary,
            "water_ml": water_ml,
            "water_l": water_ml / 1000,
            "weight": weight["weight_kg"] if weight else summary["settings"]["weight_current"],
            "tips": tips(summary, water_ml),
        })


@app.get("/meal/{meal_type}")
def meal_detail(request: Request, meal_type: str, day: str | None = None):
    day = day or today()
    with connect() as conn:
        summary = day_summary(conn, day)
        meal = next(m for m in summary["meals"] if m["key"] == meal_type)
        entries = [e for e in summary["entries"] if e["meal_type"] == meal_type]
        return templates.TemplateResponse("meal_detail.html", {
            "request": request,
            "day": day,
            "meal": meal,
            "entries": entries,
        })


@app.get("/meal/{meal_type}/add")
def add_food_view(request: Request, meal_type: str, day: str | None = None, q: str = "", category: str = "", sort: str = "frequent"):
    day = day or today()
    with connect() as conn:
        summary = day_summary(conn, day)
        meal = next(m for m in summary["meals"] if m["key"] == meal_type)
        categories = conn.execute("SELECT DISTINCT category FROM foods ORDER BY category").fetchall()
        foods = frequent_foods(conn, meal_type, q, category) if sort == "frequent" else [dict(r) for r in conn.execute(*food_search_sql(q, category)).fetchall()]
        return templates.TemplateResponse("add_food.html", {
            "request": request,
            "day": day,
            "meal": meal,
            "foods": foods,
            "q": q,
            "category": category,
            "sort": sort,
            "categories": categories,
        })


@app.get("/meal/{meal_type}/food/{food_id}")
def food_detail(request: Request, meal_type: str, food_id: int, day: str | None = None):
    day = day or today()
    with connect() as conn:
        summary = day_summary(conn, day)
        meal = next(m for m in summary["meals"] if m["key"] == meal_type)
        food = conn.execute("SELECT * FROM foods WHERE id=?", (food_id,)).fetchone()
        recent = conn.execute(
            "SELECT COUNT(*) AS c FROM diary_entries WHERE food_id=? AND meal_type=?",
            (food_id, meal_type),
        ).fetchone()["c"]
        return templates.TemplateResponse("food_detail.html", {
            "request": request,
            "day": day,
            "meal": meal,
            "food": food,
            "recent": recent,
        })


@app.post("/meal/{meal_type}/add")
def add_food(meal_type: str, day: str = Form(...), food_id: int = Form(...), quantity: float = Form(...), grams: float = Form(...)):
    with connect() as conn:
        add_entry(conn, day, meal_type, food_id, quantity, grams)
        conn.commit()
    return redirect(f"/meal/{meal_type}/add?day={day}")


@app.post("/entry/{entry_id}/delete")
def delete_food(entry_id: int, meal_type: str = Form(...), day: str = Form(...)):
    with connect() as conn:
        remove_entry(conn, entry_id)
        conn.commit()
    return redirect(f"/meal/{meal_type}?day={day}")


@app.get("/foods")
def foods(request: Request, q: str = "", category: str = ""):
    with connect() as conn:
        rows = conn.execute(*food_search_sql(q, category)).fetchall()
        categories = conn.execute("SELECT DISTINCT category FROM foods ORDER BY category").fetchall()
    return templates.TemplateResponse("foods.html", {"request": request, "foods": rows, "q": q, "category": category, "categories": categories})


@app.post("/foods/custom")
def custom_food(name: str = Form(...), category: str = Form("Personalizado"), kcal_100g: float = Form(...), carbs_100g: float = Form(0), protein_100g: float = Form(0), fat_100g: float = Form(0), grams_per_default_unit: float = Form(100)):
    with connect() as conn:
        conn.execute(
            "INSERT INTO foods(name, category, aliases, kcal_100g, carbs_100g, protein_100g, fat_100g, default_unit, grams_per_default_unit, source, is_custom) VALUES(?,?,?,?,?,?,?,?,?,?,1)",
            (name, category, name.lower(), kcal_100g, carbs_100g, protein_100g, fat_100g, "100 g", grams_per_default_unit, "usuario"),
        )
        conn.commit()
    return redirect("/foods")


@app.post("/water")
def water(amount_ml: int = Form(...), day: str = Form(...)):
    with connect() as conn:
        conn.execute("INSERT INTO water_entries(date, amount_ml) VALUES(?, ?)", (day, amount_ml))
        conn.commit()
    return redirect(f"/?day={day}")


@app.post("/weight")
def weight(weight_kg: float = Form(...), day: str = Form(...)):
    with connect() as conn:
        conn.execute("INSERT INTO weight_entries(date, weight_kg) VALUES(?, ?)", (day, weight_kg))
        conn.execute("UPDATE settings SET value=? WHERE key='weight_current'", (str(weight_kg),))
        conn.commit()
    return redirect(f"/?day={day}")


@app.post("/commit")
def commit(day: str = Form(...)):
    with connect() as conn:
        conn.execute("INSERT OR REPLACE INTO daily_commitments(date, committed) VALUES(?, 1)", (day,))
        conn.commit()
    return redirect("/streak")


@app.get("/calendar")
def calendar_view(request: Request, year: int | None = None, month: int | None = None):
    now = date.today()
    year = year or now.year
    month = month or now.month
    days = [d for week in cal.Calendar(6).monthdatescalendar(year, month) for d in week if d.month == month]
    with connect() as conn:
        green = {d.isoformat() for d in days if is_green_day(conn, d.isoformat())}
        s = streak(conn, today())
        weights = conn.execute("SELECT weight_kg FROM weight_entries WHERE date LIKE ? ORDER BY date", (f"{year:04d}-{month:02d}%",)).fetchall()
    delta = round(weights[-1]["weight_kg"] - weights[0]["weight_kg"], 1) if len(weights) > 1 else 0
    return templates.TemplateResponse("calendar.html", {"request": request, "year": year, "month": month, "month_name": datetime(year, month, 1).strftime("%B"), "days": days, "green": green, "streak": s, "delta": delta})


@app.get("/streak")
def streak_view(request: Request):
    with connect() as conn:
        s = streak(conn, today())
        recent = [(date.today()).toordinal() - i for i in range(6, -1, -1)]
        recent_days = [date.fromordinal(o) for o in recent]
        active = {r["date"] for r in conn.execute("SELECT DISTINCT date FROM diary_entries").fetchall()}
    return templates.TemplateResponse("streak.html", {"request": request, "streak": s, "recent_days": recent_days, "active": active, "today": today()})


@app.get("/settings")
def settings_view(request: Request):
    with connect() as conn:
        cfg = settings(conn)
    return templates.TemplateResponse("settings.html", {"request": request, "settings": cfg})


@app.post("/settings")
def save_settings(
    daily_kcal: float = Form(...),
    daily_carbs: float = Form(...),
    daily_protein: float = Form(...),
    daily_fat: float = Form(...),
    daily_water_ml: float = Form(...),
    weight_goal: float = Form(...),
):
    values = {
        "daily_kcal": daily_kcal,
        "daily_carbs": daily_carbs,
        "daily_protein": daily_protein,
        "daily_fat": daily_fat,
        "daily_water_ml": daily_water_ml,
        "weight_goal": weight_goal,
    }
    with connect() as conn:
        conn.executemany("UPDATE settings SET value=? WHERE key=?", [(str(v), k) for k, v in values.items()])
        conn.commit()
    return redirect("/settings")

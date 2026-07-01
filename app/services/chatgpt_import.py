from __future__ import annotations

import base64
import json

from sqlite3 import Connection

from app.services.diary import MEALS

MEAL_SLUGS = {
    "breakfast": "cafe_da_manha",
    "lunch": "almoco",
    "dinner": "jantar",
    "snack": "lanches",
    "cafe_da_manha": "breakfast",
    "almoco": "lunch",
    "jantar": "dinner",
    "lanches": "snack",
}

MAX_PAYLOAD_LEN = 30000


def encode_chatgpt_import_payload(json_obj: dict) -> str:
    raw = json.dumps(json_obj, ensure_ascii=False, separators=(",", ":")).encode("utf-8")
    return base64.urlsafe_b64encode(raw).decode("ascii").rstrip("=")


def decode_chatgpt_import_payload(payload: str) -> dict:
    if len(payload) > MAX_PAYLOAD_LEN:
        raise ValueError("Payload muito grande.")
    padded = payload + "=" * (-len(payload) % 4)
    try:
        return json.loads(base64.urlsafe_b64decode(padded.encode("ascii")).decode("utf-8"))
    except Exception as exc:
        raise ValueError("Payload inválido.") from exc


def extract_payload(value: str) -> dict:
    value = value.strip()
    if not value:
        raise ValueError("Cole um JSON, link ou payload.")
    if "payload=" in value:
        value = value.split("payload=", 1)[1].split("&", 1)[0]
    if value.startswith("{"):
        try:
            return json.loads(value)
        except json.JSONDecodeError as exc:
            raise ValueError("JSON inválido.") from exc
    return decode_chatgpt_import_payload(value)


def render_prompt(template: str, day: str, meal_type: str) -> str:
    return template.replace("{{date}}", day).replace("{{meal_type}}", MEAL_SLUGS.get(meal_type, meal_type))


def validate_import(data: dict) -> tuple[dict, list[str]]:
    warnings = []
    raw_meal_type = str(data.get("meal_type", ""))
    meal_type = raw_meal_type if raw_meal_type in MEALS else MEAL_SLUGS.get(raw_meal_type)
    if meal_type not in MEALS:
        raise ValueError("Refeição inválida.")
    items = data.get("items")
    if not isinstance(items, list) or not items:
        raise ValueError("Inclua ao menos um alimento.")
    clean_items = []
    for item in items:
        name = str(item.get("name", "")).strip()
        if not name:
            raise ValueError("Alimento sem nome.")
        grams = float(item.get("estimated_grams", 0))
        kcal = float(item.get("kcal", 0))
        carbs = float(item.get("carbs", 0))
        protein = float(item.get("protein", 0))
        fat = float(item.get("fat", 0))
        fiber = float(item.get("fiber", 0))
        sugar = float(item.get("sugar", 0))
        sodium_mg = float(item.get("sodium_mg", 0))
        if min(grams, kcal, carbs, protein, fat, fiber, sugar, sodium_mg) < 0:
            raise ValueError("Valores negativos não são permitidos.")
        clean_items.append({
            "name": name,
            "estimated_grams": grams,
            "kcal": kcal,
            "carbs": carbs,
            "protein": protein,
            "fat": fat,
            "fiber": fiber,
            "sugar": sugar,
            "sodium_mg": sodium_mg,
            "confidence": item.get("confidence", "medium"),
            "notes": item.get("notes", ""),
        })
    totals = {
        "kcal": round(sum(i["kcal"] for i in clean_items)),
        "carbs": round(sum(i["carbs"] for i in clean_items), 1),
        "protein": round(sum(i["protein"] for i in clean_items), 1),
        "fat": round(sum(i["fat"] for i in clean_items), 1),
    }
    provided = data.get("totals") or {}
    if provided and any(round(float(provided.get(k, totals[k])), 1) != round(totals[k], 1) for k in totals):
        warnings.append("Totais recalculados a partir dos alimentos.")
    return ({
        "source": data.get("source", "chatgpt_photo_estimate"),
        "date": data.get("date"),
        "meal_type": meal_type,
        "dish_name": data.get("dish_name", "Refeição estimada"),
        "confidence": data.get("confidence", "medium"),
        "items": clean_items,
        "totals": totals,
        "notes": data.get("notes", "Estimativa visual. Revise antes de salvar."),
    }, warnings)


def find_or_create_food(conn: Connection, item: dict) -> int:
    name = item["name"].strip()
    row = conn.execute("SELECT id FROM foods WHERE lower(name)=lower(?) LIMIT 1", (name,)).fetchone()
    if row:
        return row["id"]
    row = conn.execute("SELECT id FROM foods WHERE lower(aliases) LIKE lower(?) LIMIT 1", (f"%{name}%",)).fetchone()
    if row:
        return row["id"]
    first = name.split()[0]
    row = conn.execute("SELECT id FROM foods WHERE lower(name) LIKE lower(?) LIMIT 1", (f"%{first}%",)).fetchone()
    if row:
        return row["id"]
    grams = max(float(item["estimated_grams"]), 1)
    factor = 100 / grams
    cur = conn.execute(
        """
        INSERT INTO foods(name, category, aliases, kcal_100g, carbs_100g, protein_100g, fat_100g,
        fiber_100g, sugar_100g, sodium_mg_100g, default_unit, grams_per_default_unit, source, is_custom)
        VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,1)
        """,
        (
            name,
            "Estimados",
            name.lower(),
            item["kcal"] * factor,
            item["carbs"] * factor,
            item["protein"] * factor,
            item["fat"] * factor,
            item["fiber"] * factor,
            item["sugar"] * factor,
            item["sodium_mg"] * factor,
            "porção",
            grams,
            "Estimado por ChatGPT/foto",
        ),
    )
    return cur.lastrowid

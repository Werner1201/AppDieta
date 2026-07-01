from __future__ import annotations

import sqlite3
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DATA_DIR = ROOT / "data"
DB_PATH = DATA_DIR / "diet.sqlite"


def connect(path: Path = DB_PATH) -> sqlite3.Connection:
    DATA_DIR.mkdir(exist_ok=True)
    conn = sqlite3.connect(path)
    conn.row_factory = sqlite3.Row
    conn.execute("PRAGMA foreign_keys = ON")
    return conn


SCHEMA = """
CREATE TABLE IF NOT EXISTS settings (
  key TEXT PRIMARY KEY,
  value TEXT NOT NULL
);
CREATE TABLE IF NOT EXISTS foods (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  category TEXT NOT NULL,
  aliases TEXT NOT NULL DEFAULT '',
  kcal_100g REAL NOT NULL,
  carbs_100g REAL NOT NULL,
  protein_100g REAL NOT NULL,
  fat_100g REAL NOT NULL,
  fiber_100g REAL NOT NULL DEFAULT 0,
  sugar_100g REAL NOT NULL DEFAULT 0,
  sodium_mg_100g REAL NOT NULL DEFAULT 0,
  default_unit TEXT NOT NULL DEFAULT '100 g',
  grams_per_default_unit REAL NOT NULL DEFAULT 100,
  source TEXT NOT NULL DEFAULT 'aproximado',
  is_custom INTEGER NOT NULL DEFAULT 0,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS food_portions (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  food_id INTEGER NOT NULL REFERENCES foods(id) ON DELETE CASCADE,
  label TEXT NOT NULL,
  grams REAL NOT NULL CHECK (grams > 0),
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS diary_entries (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  date TEXT NOT NULL,
  meal_type TEXT NOT NULL,
  food_id INTEGER NOT NULL REFERENCES foods(id),
  quantity REAL NOT NULL CHECK (quantity > 0),
  unit_label TEXT NOT NULL,
  grams_total REAL NOT NULL,
  kcal REAL NOT NULL,
  carbs REAL NOT NULL,
  protein REAL NOT NULL,
  fat REAL NOT NULL,
  fiber REAL NOT NULL,
  sugar REAL NOT NULL,
  sodium_mg REAL NOT NULL,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS water_entries (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  date TEXT NOT NULL,
  amount_ml INTEGER NOT NULL CHECK (amount_ml > 0),
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS weight_entries (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  date TEXT NOT NULL,
  weight_kg REAL NOT NULL CHECK (weight_kg > 0),
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS daily_commitments (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  date TEXT NOT NULL UNIQUE,
  committed INTEGER NOT NULL DEFAULT 1,
  created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);
"""

DEFAULT_SETTINGS = {
    "daily_kcal": "2333",
    "daily_carbs": "284",
    "daily_protein": "114",
    "daily_fat": "75",
    "daily_water_ml": "2000",
    "weight_current": "108",
    "weight_goal": "80",
    "meal_breakfast_kcal": "816",
    "meal_lunch_kcal": "816",
    "meal_dinner_kcal": "700",
    "meal_snack_kcal": "250",
    "chatgpt_gpt_url": "https://chatgpt.com/g/g-6a4594e4a6c88191b132ffc25a95ff0d-importador-de-refeicoes-para-app-local",
    "chatgpt_import_prompt_template": """Vou enviar uma foto do meu prato. Analise a imagem e gere um JSON para importar no meu app local de dieta. Use exatamente o schema de importação abaixo. Estime alimentos, gramas, calorias, carboidratos, proteínas, gorduras, fibras, açúcar e sódio quando possível. Marque confidence como low, medium ou high. Não invente precisão absoluta. No final, gere também um link local de importação no formato http://127.0.0.1:8000/import/chatgpt?payload=<base64url-do-json>.

Data: {{date}}
Refeição: {{meal_type}}

Schema:
{
  "source": "chatgpt_photo_estimate",
  "date": "YYYY-MM-DD",
  "meal_type": "cafe_da_manha | almoco | jantar | lanches",
  "dish_name": "string",
  "confidence": "low | medium | high",
  "items": [
    {
      "name": "string",
      "estimated_grams": 100,
      "kcal": 130,
      "carbs": 28.0,
      "protein": 2.7,
      "fat": 0.3,
      "fiber": 1.0,
      "sugar": 0.0,
      "sodium_mg": 1,
      "confidence": "low | medium | high",
      "notes": "string opcional"
    }
  ],
  "totals": {
    "kcal": 0,
    "carbs": 0,
    "protein": 0,
    "fat": 0
  },
  "notes": "Estimativa visual. O usuário deve revisar antes de salvar."
}""",
}


def init_db(path: Path = DB_PATH) -> None:
    with connect(path) as conn:
        conn.executescript(SCHEMA)
        conn.executemany(
            "INSERT OR IGNORE INTO settings(key, value) VALUES(?, ?)",
            DEFAULT_SETTINGS.items(),
        )

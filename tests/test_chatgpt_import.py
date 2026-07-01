from fastapi.testclient import TestClient

from app.db import connect, init_db
import app.main as main_module
from app.main import app
from app.services.chatgpt_import import decode_chatgpt_import_payload, encode_chatgpt_import_payload, find_or_create_food, render_prompt, validate_import


SAMPLE = {
    "source": "chatgpt_photo_estimate",
    "date": "2026-07-01",
    "meal_type": "almoco",
    "dish_name": "Prato feito",
    "confidence": "medium",
    "items": [{
        "name": "Arroz branco cozido",
        "estimated_grams": 100,
        "kcal": 128,
        "carbs": 28.1,
        "protein": 2.5,
        "fat": 0.2,
        "fiber": 1.6,
        "sugar": 0,
        "sodium_mg": 1,
        "confidence": "medium",
    }],
    "totals": {"kcal": 128, "carbs": 28.1, "protein": 2.5, "fat": 0.2},
}


def test_prompt_and_payload_roundtrip():
    prompt = render_prompt("Data: {{date}} Refeição: {{meal_type}}", "2026-07-01", "lunch")
    assert "2026-07-01" in prompt
    assert "almoco" in prompt
    assert decode_chatgpt_import_payload(encode_chatgpt_import_payload(SAMPLE))["dish_name"] == "Prato feito"


def test_rejects_negative_values():
    bad = dict(SAMPLE)
    bad["items"] = [dict(SAMPLE["items"][0], kcal=-1)]
    try:
        validate_import(bad)
    except ValueError as exc:
        assert "negativos" in str(exc)
    else:
        raise AssertionError("negative import accepted")


def test_preview_does_not_save_automatically():
    client = TestClient(app)
    before = client.get("/meal/lunch").text.count("Arroz")
    response = client.get(f"/import/chatgpt?payload={encode_chatgpt_import_payload(SAMPLE)}")
    after = client.get("/meal/lunch").text.count("Arroz")
    assert response.status_code == 200
    assert "Salvar no diário" in response.text
    assert before == after


def test_existing_food_reused_and_new_food_becomes_custom(tmp_path):
    path = tmp_path / "diet.sqlite"
    init_db(path)
    conn = connect(path)
    conn.execute(
        "INSERT INTO foods(name, category, aliases, kcal_100g, carbs_100g, protein_100g, fat_100g, default_unit, grams_per_default_unit, source) VALUES(?,?,?,?,?,?,?,?,?,?)",
        ("Arroz branco cozido", "Básicos", "arroz", 128, 28, 2.5, 0.2, "100 g", 100, "teste"),
    )
    conn.commit()
    assert find_or_create_food(conn, SAMPLE["items"][0]) == 1
    new_id = find_or_create_food(conn, dict(SAMPLE["items"][0], name="Comida inventada"))
    custom = conn.execute("SELECT is_custom FROM foods WHERE id=?", (new_id,)).fetchone()["is_custom"]
    assert custom == 1


def test_confirmation_saves_to_temp_diary(tmp_path, monkeypatch):
    path = tmp_path / "diet.sqlite"
    init_db(path)

    def temp_connect():
        return connect(path)

    monkeypatch.setattr(main_module, "connect", temp_connect)
    response = TestClient(app).post("/import/chatgpt/save", data={
        "date": "2026-07-01",
        "meal_type": "lunch",
        "item_name": "Comida teste",
        "grams": "100",
        "kcal": "100",
        "carbs": "10",
        "protein": "5",
        "fat": "2",
        "fiber": "1",
        "sugar": "0",
        "sodium_mg": "10",
    }, follow_redirects=False)
    with connect(path) as conn:
        count = conn.execute("SELECT COUNT(*) AS c FROM diary_entries").fetchone()["c"]
    assert response.status_code == 303
    assert count == 1

from fastapi.testclient import TestClient

from app.main import app


def test_home_renders():
    response = TestClient(app).get("/")
    assert response.status_code == 200
    assert "Hoje" in response.text
    assert "built-in method" not in response.text
    assert "{{" not in response.text
    assert "kcal</span><span>-</span><span>" in response.text


def test_add_food_page_renders():
    response = TestClient(app).get("/meal/breakfast/add")
    assert response.status_code == 200
    assert "Pesquisar" in response.text
    assert "Frequentes" in response.text

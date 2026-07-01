from fastapi.testclient import TestClient

from app.main import app


def test_home_renders():
    response = TestClient(app).get("/")
    assert response.status_code == 200
    assert "Hoje" in response.text

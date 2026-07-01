# Dieta Local

App local-first de registro alimentar para Android/Termux, com FastAPI, SQLite e telas mobile-first.

## Rodar no Termux

```bash
pkg update && pkg upgrade
pkg install python git sqlite
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python scripts/init_db.py
python scripts/seed_foods.py
uvicorn app.main:app --host 127.0.0.1 --port 8000
```

Abra no navegador:

```text
http://127.0.0.1:8000
```

Ou use:

```bash
bash scripts/run_termux.sh
```

## Testes

```bash
pytest
```

## Stack

- Python + FastAPI
- SQLite local em `data/diet.sqlite`
- Jinja2 + CSS próprio
- PWA simples com manifest e service worker

O servidor escuta em `127.0.0.1` por padrão. Para expor na rede, rode o `uvicorn` explicitamente com outro host.

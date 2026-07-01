#!/data/data/com.termux/files/usr/bin/bash
set -e
python scripts/init_db.py
python scripts/seed_foods.py
uvicorn app.main:app --host 127.0.0.1 --port 8000

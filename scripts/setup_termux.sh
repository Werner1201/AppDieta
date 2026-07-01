#!/data/data/com.termux/files/usr/bin/bash
set -e

pkg update
pkg install -y python git sqlite rust clang binutils

python -m venv .venv
source .venv/bin/activate
python -m pip install --upgrade pip setuptools wheel
pip install -r requirements.txt

python scripts/init_db.py
python scripts/seed_foods.py
python scripts/normalize_food_text.py

echo "Pronto. Rode: source .venv/bin/activate && bash scripts/run_termux.sh"

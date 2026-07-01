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
python scripts/normalize_food_text.py
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

## Importar refeição com ChatGPT

Na tela de adicionar alimentos de uma refeição, toque em `Câmera`.

Fluxo:

```text
app local -> copiar prompt -> abrir GPT -> enviar foto -> receber JSON/link -> importar -> revisar -> salvar
```

O app não usa API key, não envia o banco para fora e nunca salva a estimativa sem confirmação. O link do GPT fica em `Metas > Link do GPT`.

## Stack

- Python + FastAPI
- SQLite local em `data/diet.sqlite`
- Jinja2 + CSS próprio
- PWA simples com manifest e service worker

O servidor escuta em `127.0.0.1` por padrão. Para expor na rede, rode o `uvicorn` explicitamente com outro host.

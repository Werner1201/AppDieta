# Dieta Local

App local-first de dieta e atividades. O produto principal e o Android nativo em `android-native/`; a versao FastAPI/Termux permanece como implementacao legada.

## Android nativo

Abra `android-native/` no Android Studio e execute a configuracao `app`. Para build por linha de comando:

```powershell
cd android-native
.\gradlew.bat lintDebug testDebugUnitTest assembleDebug
```

O APK e gerado em `android-native/app/build/outputs/apk/debug/app-debug.apk`. Consulte [android-native/README.md](android-native/README.md) para instalacao via ADB, deep link, backup e solucao de problemas.

As referencias concisas de produto e interface estao em [PRODUCT.md](PRODUCT.md) e [DESIGN.md](DESIGN.md).

## Versao legada no Termux

```bash
pkg update && pkg upgrade
pkg install python git sqlite rust clang binutils
python -m venv .venv
source .venv/bin/activate
python -m pip install --upgrade pip setuptools wheel
pip install -r requirements.txt
python scripts/init_db.py
python scripts/seed_foods.py
python scripts/normalize_food_text.py
uvicorn app.main:app --host 127.0.0.1 --port 8000
```

Se o erro for `Failed to build 'pydantic-core'`, instale `rust clang binutils` e rode o `pip install -r requirements.txt` de novo. No Termux, esse pacote pode precisar compilar.

Atalho:

```bash
bash scripts/setup_termux.sh
```

Abra no navegador:

```text
http://127.0.0.1:8000
```

Ou use:

```bash
bash scripts/run_termux.sh
```

## Testes da versao legada

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

## Stack legada

- Python + FastAPI
- SQLite local em `data/diet.sqlite`
- Jinja2 + CSS próprio
- PWA simples com manifest e service worker

O servidor escuta em `127.0.0.1` por padrão. Para expor na rede, rode o `uvicorn` explicitamente com outro host.

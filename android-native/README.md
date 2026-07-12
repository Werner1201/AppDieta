# Dieta Local Android Nativo

Aplicativo local-first de dieta, alimentos, receitas, agua, peso, atividades e importacao semiautomatica via ChatGPT.

## Requisitos

- Android Studio com JDK 17.
- Android SDK 35 e plataforma-tools (ADB).
- Android 8.0/API 26 ou superior no dispositivo.

## Abrir

1. Abra a pasta `android-native/` no Android Studio.
2. Aguarde o sync do Gradle.
3. Rode `app` em um emulador ou celular.

## Build e testes

```powershell
.\gradlew.bat lintDebug testDebugUnitTest assembleDebug
```

No macOS/Linux, use `./gradlew`. O APK e gerado em:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Instalar com ADB

```powershell
adb devices
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

`-r` atualiza o app preservando o banco local. Migracoes Room atualizam o schema incrementalmente.

## Dados e backup

- Banco Room no armazenamento privado do app; nenhum servidor e necessario.
- Metas armazenadas localmente em SharedPreferences.
- Exportar/importar diario fica em `Perfil`, usando o compartilhamento nativo.
- A importacao e aditiva; revise a previa antes de confirmar.

## Importador ChatGPT

Na refeicao, abra `Camera / ChatGPT`, copie o prompt, use o GPT configurado e cole o JSON ou link retornado. Nenhuma API key e necessaria.

Deep link:

```text
romlingdiet://import?data=<payload-base64url>
```

Teste por ADB:

```powershell
adb shell am start -a android.intent.action.VIEW -d "romlingdiet://import?data=<payload>" com.romling.diettracker
```

## Solucao de problemas

- Sync preso: confirme JDK 17 e SDK 35 instalados.
- Dispositivo ausente: rode `adb devices` e aceite a autorizacao USB.
- APK antigo: rode `adb install -r` novamente apos `assembleDebug`.
- Build inconsistente: use `.\gradlew.bat clean assembleDebug`; `clean` remove apenas artefatos de build.
- O aviso de compatibilidade futura com Gradle 10 nao bloqueia o build atual e esta registrado no plano.

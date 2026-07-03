# Dieta Local Android Nativo

Projeto Android nativo da migração controlada.

## Abrir

1. Abra a pasta `android-native/` no Android Studio.
2. Aguarde o sync do Gradle.
3. Rode `app` em um emulador ou celular.

## Linha de comando

Depois que o Android Studio gerar/sincronizar o Gradle Wrapper:

```bash
./gradlew test
./gradlew assembleDebug
```

Neste repositório ainda não há Gradle Wrapper gerado. Sem Android Studio ou uma instalação local do Gradle, o build por linha de comando fica bloqueado.

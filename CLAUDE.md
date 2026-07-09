# CLAUDE.md

## Como continuar este projeto

- Fonte da verdade: leia `MIGRATION_PLAN.md` antes de mexer em código.
- O projeto ativo é a migração Android nativa em `android-native/`.
- Use sempre comandos shell com prefixo `rtk`, conforme `C:\Users\Werner\.codex\RTK.md`.
- Faça ciclos pequenos: Arquiteto -> Dev -> QA -> commit -> push -> próximo ciclo.
- Não misture ciclos. Se um ciclo está em andamento, termine, valide, registre no plano, commite e envie antes de abrir outro.
- Use `apply_patch` para edições manuais.
- Não reverta mudanças que você não fez.
- Mantenha fora dos commits as imagens de referência untracked na raiz:
  - `229260 (1).jpg`
  - `229260.jpg`
  - `229262 (1).jpg`
  - `229262.jpg`
  - `229264.jpg`
  - `229266.jpg`
  - `229270.jpg`
  - `229272.jpg`
  - `229320.jpeg`
  - `229322.jpeg`

## Loop com agentes

Use esta thread como orquestradora.

Para cada ciclo:
1. Arquiteto: registre no `MIGRATION_PLAN.md` nome, motivo, arquivos prováveis, critérios de aceite, riscos e instrução objetiva.
2. Dev: implemente a menor fatia útil, reaproveitando padrões existentes.
3. Validação local: rode pelo menos `.\gradlew.bat test` e `.\gradlew.bat assembleDebug` em `android-native` quando tocar código Android.
4. QA: chame um subagente de leitura para validar só o ciclo atual. Ele não deve editar arquivos.
5. Plano: atualize a seção QA com APROVADO ou corrija antes de seguir.
6. Git: stage só os arquivos do ciclo, commit com mensagem curta e `git push origin master`.

Modelo de prompt para QA:

```text
Repo: C:\Users\Werner\Desktop\AI\Projetos\AppDieta. Use rtk para shell.
Você é QA de ciclo para o repo AppDieta. Verifique somente o ciclo atual.
Não edite arquivos. Confirme os critérios de aceite do ciclo no MIGRATION_PLAN.md.
Ignore imagens untracked na raiz. Responda com APROVADO ou REPROVADO e achados objetivos.
```

## Estado atual

Últimos ciclos concluídos e enviados:
- Ciclo 24: revisão de acentos (nenhum mojibake encontrado).
- Ciclo 25: monitor de água na tela Hoje.
- Ciclo 26: card de valores corporais (peso) na tela Hoje.
- Ciclo 27: navegação inferior visual com cinco abas (Diário funcional, demais placeholders).

Ciclo em andamento:
- Nenhum ciclo de código aberto após o Ciclo 27.
- Próximo ciclo sugerido no `MIGRATION_PLAN.md`: escolher entre calendário, streak, configurações de meta ou tela de detalhe de refeição separada.

## Estilo de implementação

- Solução mínima que funciona.
- Reuse helpers, entidades, DAOs e ViewModels existentes.
- Não adicione dependência sem necessidade clara.
- Não implemente importação, câmera, código de barras, edição avançada ou porção customizada dentro de ciclos que não pedem isso.
- Se mexer em UI Compose, verifique com build debug.
- Se mexer em lógica, deixe um teste pequeno que falhe se a lógica quebrar.

## Comandos úteis

```powershell
rtk git status --short
rtk git diff --check
rtk proxy powershell -NoProfile -Command ".\gradlew.bat test"
rtk proxy powershell -NoProfile -Command ".\gradlew.bat assembleDebug"
rtk git add MIGRATION_PLAN.md <arquivos-do-ciclo>
rtk git commit -m "feat: mensagem curta"
rtk git push origin master
```

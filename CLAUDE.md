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
- Ciclo 28: correção de bugs de layout na TodayScreen (tela estreita 322 dp).
- Ciclo 29: sistema de dimensões responsivas `AppDimensions` via `CompositionLocal`.
- Ciclo 30: correção de bugs visuais críticos na tela AddFood (título e nomes de alimentos).
- Ciclo 31: calendário de dias verdes com navegação por swipe e tap em dias.
- Ciclo 32: streak atual no header da tela Hoje.
- Ciclo 33: tela simples de sequência.
- Ciclo 34: tela de configurações somente leitura.
- Ciclo 35: edição persistida de metas via SharedPreferences.
- Ciclo 36: metas por refeição proporcionais à meta calórica diária.
- Ciclo 37: tela de detalhe de refeição (`MealDetailScreen`) com macros, lista de itens e Adicionar mais.
- Ciclo 38: tela de detalhe de alimento (`FoodDetailScreen`) com macros por 100g, porções e botão Adicionar.
- Ciclo 39: cadastro de alimento customizado (`CreateFoodScreen`) acessível via botão "+ Criar" no AddFoodScreen.
- Ciclo 40: tela dedicada de peso com histórico (`WeightScreen`) acessível por "Ver histórico" na seção Valores corporais.
- Ciclo 41: lista e exclusão de alimentos customizados (`CustomFoodsScreen`) acessível via "Meus alimentos" em Configurações.
- Ciclo 42: importador ChatGPT (`ChatGptImportScreen`) — cola JSON → prévia → salva no diário.
- Ciclo 43: prompt copiável para o ChatGPT com formato JSON esperado (card + botão "Copiar" com feedback).
- Ciclo 44: exportação de diário completo via share sheet JSON (`TodayViewModel.exportJson` + `Intent.ACTION_SEND`).
- Ciclo 45: JSON escaping seguro em `exportJson` via `org.json.JSONObject`/`JSONArray` + teste unitário de round-trip.
- Ciclo 46: edição de gramas de entrada no diário via `AlertDialog` em `MealDetailScreen`; nutrição recalculada proporcionalmente.
- Ciclo 47: tela Receitas funcional (tab RECIPES); `RecipeEntity`, `RecipeDao`, `MIGRATION_1_2` (v1→v2), `RecipeRepository`, `RecipesViewModel`, `RecipesScreen` com lista e AlertDialog de criação.
- Ciclo 48: ingredientes em receitas; `RecipeIngredientEntity`, `RecipeIngredientDao`, `MIGRATION_2_3` (v2→v3); `RecipeDetailScreen` com busca 2 passos, lista de ingredientes e totais macro.
- Ciclo 49: receita adicionada ao diário com escolha da refeição e totais agregados.
- Ciclo 50: importador ChatGPT robusto para JSON/Markdown/base64url/link, compatível com o schema web e API 26.
- Ciclo 51: deep link `romlingdiet://`, importação explícita do clipboard e abertura do GPT personalizado.

Ciclo em andamento:
- Nenhum ciclo aberto após o Ciclo 51.
- Próximo ciclo sugerido: tornar URL e prompt do GPT editáveis nas Configurações e remover as constantes da tela de importação.

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

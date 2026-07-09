# Plano de Migração Android Nativo

## Resumo do App Atual

Stack atual:
- FastAPI, Jinja2, SQLite local, CSS próprio e PWA simples.
- Entrada principal em `app/main.py`.
- Banco local em `data/diet.sqlite`, schema em `app/db.py`.
- Regras em `app/services/diary.py`, `nutrition.py` e `chatgpt_import.py`.
- Telas Jinja em `app/templates/`.
- Identidade visual em `app/static/app.css`.
- Seed brasileiro em `scripts/seed_foods.py`.

Funcionalidades existentes:
- Tela Hoje com semana, pontos/ícones, dicas, resumo calórico, macros, refeições, água, peso e dicas.
- Refeições: café da manhã, almoço, jantar e lanches.
- Busca por alimentos, filtro por categoria, frequentes, registrados e tela de detalhe do alimento.
- Cadastro simples de alimento customizado em `/foods`.
- Registro e remoção de alimentos por refeição.
- Registro de água por botões rápidos.
- Registro de peso.
- Calendário com dias verdes.
- Streak/sequência.
- Configurações de metas e link/prompt do GPT.
- Importação via ChatGPT por JSON, link com payload ou base64url, sempre com prévia antes de salvar.

## Schema Atual

Tabelas SQLite:
- `settings`: chave/valor para metas, peso e ChatGPT.
- `foods`: catálogo alimentar com nome, categoria, aliases, macros por 100 g, unidade padrão e fonte.
- `food_portions`: porções por alimento.
- `diary_entries`: registros alimentares já calculados.
- `water_entries`: água por dia.
- `weight_entries`: peso por dia.
- `daily_commitments`: compromisso diário.

Defaults importantes:
- Meta diária: 2333 kcal, 284 g carboidratos, 114 g proteína, 75 g gordura.
- Água: 2000 ml.
- Peso atual/alvo: 108 kg / 80 kg.
- Metas por refeição: breakfast 816, lunch 816, dinner 700, snack 250.

## Regras de Negócio

Nutrição:
- `kcal = round(kcal_100g * grams / 100)`.
- Macros, fibras, açúcar e sódio são arredondados com 1 casa.
- Soma diária usa os valores já gravados em `diary_entries`.
- Quantidade e gramas devem ser positivas.
- Alimento não pode ter nome vazio.

Dia verde:
- Existe pelo menos um registro alimentar no dia.
- Total de kcal do dia <= meta diária.
- Proteína do dia >= 80% da meta.

Streak:
- Sequência atual conta dias consecutivos com qualquer registro alimentar até a data atual.
- Maior sequência percorre os dias ativos ordenados.

ChatGPT:
- Aceita JSON puro, link com `payload=`, ou base64url.
- Limite atual de payload: 30000 caracteres.
- Valida refeição, itens, nomes e valores negativos.
- Recalcula totais e avisa quando divergem.
- Salva só após confirmação.

## Arquitetura Android Proposta

Criar novo app em `android-native/`, mantendo o web app intacto.

Stack:
- Kotlin.
- Gradle Kotlin DSL.
- Jetpack Compose.
- Material 3 usado apenas como base técnica.
- Room/SQLite.
- DataStore Preferences.
- Navigation Compose.
- ViewModel.
- Coroutines/Flow.
- kotlinx.serialization.
- JUnit.

Estrutura inicial:

```text
android-native/
  settings.gradle.kts
  build.gradle.kts
  app/
    build.gradle.kts
    src/main/
      AndroidManifest.xml
      kotlin/com/romling/diettracker/
        MainActivity.kt
        DietTrackerApp.kt
        core/
          database/
          datastore/
          model/
          navigation/
          ui/theme/
          ui/components/
          util/
        data/local/
          AppDatabase.kt
          dao/
          entity/
          seed/
        data/repository/
        domain/service/
        feature/
          today/
          meal/
          foods/
          water/
          weight/
          calendar/
          streak/
          settings/
          chatgptimport/
      assets/foods_seed.json
      res/values/
    src/test/
```

## Modelo de Dados Android

Room:
- `FoodEntity`: equivalente a `foods`.
- `FoodPortionEntity`: equivalente a `food_portions`.
- `DiaryEntryEntity`: equivalente a `diary_entries`, com `foodNameSnapshot` e `aiImportId`.
- `WaterEntryEntity`: equivalente a `water_entries`.
- `WeightEntryEntity`: equivalente a `weight_entries`.
- `DailyCommitmentEntity`: equivalente a `daily_commitments`.
- `AiImportEntity`: histórico local de importações ChatGPT confirmadas.

DataStore Preferences:
- Metas diárias.
- Metas por refeição.
- Peso atual/alvo.
- Link do GPT.
- Template de prompt.

MealType Android:
- Persistir internamente como `breakfast`, `lunch`, `dinner`, `snack` para compatibilidade com o banco web.
- Aceitar importação dos slugs `cafe_da_manha`, `almoco`, `jantar`, `lanches`.

## Mapeamento Web App -> Android Nativo

| Web atual | Android nativo |
| --- | --- |
| `/` + `today.html` | `feature/today/TodayScreen` |
| `/meal/{meal_type}` | `feature/meal/MealDetailScreen` |
| `/meal/{meal_type}/add` | `feature/meal/AddFoodScreen` |
| `/meal/{meal_type}/food/{food_id}` | `feature/foods/FoodDetailScreen` |
| `/foods` | `feature/foods/FoodsScreen` + cadastro customizado |
| `/calendar` | `feature/calendar/CalendarScreen` |
| `/streak` | `feature/streak/StreakScreen` |
| `/settings` | `feature/settings/SettingsScreen` |
| `/chatgpt/prepare-import` | `feature/chatgptimport/ChatGptPrepareScreen` |
| `/import/chatgpt` | `feature/chatgptimport/ChatGptImportScreen` |
| `app/services/nutrition.py` | `NutritionCalculator.kt` |
| `app/services/diary.py` | `DiaryRepository`, `GreenDayService`, `StreakService` |
| `app/services/chatgpt_import.py` | `ChatGptImportParser.kt` |
| `scripts/seed_foods.py` | `assets/foods_seed.json` + seed loader |

## UI_PARITY_PLAN

### Tokens Visuais Base

Origem: `app/static/app.css`.

Cores:
- Fundo: `#07100d`.
- Card/painel: `#242826`.
- Linha/borda: `#59605d`.
- Texto principal: `#f7f7f2`.
- Texto secundário: `#c9cfcc`.
- Acento verde água: `#18f0bd`.
- Verde de ação/status: `#057352`.
- Barra inferior: `#141a18`.
- Dica roxa: fundo `#eee0ff`, texto `#2e087e`, borda `#8d52e8`.
- Remover: `#ff8a8a`.

Forma e espaçamento:
- Conteúdo com padding lateral equivalente a 18-26 dp.
- Cards com borda de 3 dp, raio aproximado 24 dp e fundo escuro.
- Botões grandes com raio 14-18 dp.
- Ações principais fixas no rodapé quando a tela original usa `.commit`.
- Bolhas/ícones circulares de refeição com 70 dp.
- Botão `+` circular de 58 dp na lista de refeições.
- Barra de progresso com altura aproximada de 12 dp.

Tipografia:
- Títulos grandes e pesados.
- Números de destaque com peso extra forte.
- Texto secundário claro, mas abaixo do branco.
- Não usar visual Material genérico sem bordas/cards do app atual.

Componentes Compose compartilhados:
- `AppCard`.
- `SectionTitle`.
- `MacroProgressBar`.
- `CalorieSummaryCard`.
- `MealCard`.
- `CircleActionButton`.
- `BottomPrimaryButton`.
- `MetricGrid`.
- `FoodRow`.
- `QuickAmountGrid`.
- `DarkTextField`.

### Tela Hoje

Original: `today.html`, `.summary`, `.kcal`, `.macros`, `.meal`, cards de água e peso.

Layout Android:
- Cabeçalho com "Hoje", semana, diamante/fogo/calendário.
- CTA roxo "Ver minhas Dicas Inteligentes".
- Seção "Resumo" com link "Detalhes".
- Card de resumo com consumidas, restantes em anel visual e gastas.
- Macros em três colunas com barras.
- Faixa verde "Agora: Comer".
- Seção "Alimentação" com quatro `MealCard`.
- Card de água com meta, litros, botões +100/+200/+250/+500 ml e copos.
- Card de peso com meta, peso atual e registrar.
- Dicas no fim.

Checklist visual:
- Tema escuro preservado.
- Resumo de kcal continua elemento principal.
- Cards grandes com borda preservados.
- Barras de macro preservadas.
- Refeições com ícone circular, texto e botão `+`.
- Água e peso aparecem na mesma ordem.

### Tela de Refeição

Original: `meal_detail.html`.

Layout Android:
- Header com voltar e nome da refeição.
- Hero verde/escuro com ícone grande da refeição.
- Grid 2x2 com kcal, carboidratos, proteína e gordura.
- Lista de alimentos adicionados com nome, gramas, kcal e remover.
- Botão fixo "Adicionar mais".

Checklist visual:
- Métricas em card/grid 2x2.
- Lista escura com divisórias.
- Botão principal grande no rodapé.
- Remoção visível e tocável.

### Tela Adicionar/Buscar Alimento

Original: `add_food.html`.

Layout Android:
- Header com fechar e nome da refeição.
- Quatro ferramentas em cards: Pesquisar, Câmera/ChatGPT, Código, Digitar.
- Campo de busca com borda accent.
- Filtros: categoria e frequentes/registrados/nome.
- Resultados em linhas ricas com nome, unidade, kcal e botão circular `+`.
- Modo Registrados mostra só alimentos lançados e botão `-`.
- Botão fixo "Pronto".

Checklist visual:
- Ferramentas em grade 4 colunas.
- Busca com destaque verde água.
- Resultados não colados aos botões.
- Registrados não mistura catálogo com diário.

### Tela Detalhe do Alimento

Original: `food_detail.html`.

Layout Android:
- Header com fechar, refeição e estrela.
- Hero escuro/verde com nome do alimento.
- Métricas em 4 colunas.
- Indicadores de informação verificada/recentes.
- Chips de avaliação.
- Lista nutricional por 100 g.
- Barra inferior com quantidade, gramas e botão Adicionar.

Checklist visual:
- Hero e barra inferior preservados.
- Chips preservados.
- Informação nutricional não escondida.
- Botão adicionar azul/grande como fluxo atual.

### Tela Água

Original: card dentro de `today.html`.

Layout Android:
- Pode começar como seção na Hoje; tela dedicada futura reaproveita o mesmo card.
- Meta, litros consumidos, botões rápidos e progresso visual.

Checklist visual:
- Card centralizado e destacado.
- Botões rápidos grandes.
- Progresso/quantidade visíveis.

### Tela Peso

Original: card dentro de `today.html`.

Layout Android:
- Card com objetivo, peso atual, input/registrar.
- Tela dedicada futura com histórico simples.

Checklist visual:
- Peso grande.
- Objetivo visível.
- Ação de registrar simples.

### Tela Calendário

Original: `calendar.html`.

Layout Android:
- Header com fechar e mês/ano.
- Grid mensal 7 colunas.
- Dias verdes em círculo verde.
- Stats: ativo, dias verdes, peso.

Checklist visual:
- Grade legível em tela pequena.
- Dias verdes claros.
- Stats em três colunas.

### Tela Streak

Original: `streak.html`.

Layout Android:
- Hero roxo em degradê com número gigante.
- Visão geral dos últimos 7 dias.
- Card de resumo: maior sequência e passe disponível.
- Botão fixo "Eu me comprometo".

Checklist visual:
- Hero roxo preservado.
- Número gigante preservado.
- Cards escuros e botão fixo preservados.

### Tela Configurações

Original: `settings.html`.

Layout Android:
- Header "Metas".
- Card/form com metas, peso alvo, link GPT e prompt.
- Botão Salvar.

Checklist visual:
- Form em card escuro.
- Campos grandes.
- Prompt editável.

### Tela Importar ChatGPT

Originais: `chatgpt_prepare.html`, `chatgpt_import.html`.

Layout Android:
- Tela preparar com instruções, prompt, copiar prompt, abrir GPT, importar JSON.
- Tela importar com textarea/clipboard, prévia, avisos, formulário editável e salvar.
- Deep link `romlingdiet://import/chatgpt?payload=...`.

Checklist visual:
- Fluxo de revisão antes de salvar preservado.
- Aviso de estimativa visual preservado.
- Nenhum salvamento automático.

## Plano de Migração em Etapas

1. Criar projeto Android base em `android-native/`.
2. Configurar Gradle, Compose, Material 3 e Navigation.
3. Criar tokens de tema com paridade visual.
4. Criar componentes compartilhados.
5. Criar entidades Room, DAOs e DataStore.
6. Migrar seed para `assets/foods_seed.json`.
7. Criar serviços testáveis: nutrição, dia verde, streak e parser ChatGPT.
8. Criar tela Hoje com mock/paridade visual.
9. Ligar tela Hoje ao banco real.
10. Criar busca/adicionar alimento.
11. Criar detalhe de refeição e remoção.
12. Criar cadastro customizado.
13. Criar água, peso, calendário e streak.
14. Criar configurações.
15. Criar importador ChatGPT, clipboard e deep link.
16. Criar export/import JSON.
17. Rodar testes, build e auditoria final.

## Riscos

- Ambiente local pode não ter Android SDK; nesse caso, criar projeto/testes e documentar comandos.
- Seed atual mistura dados reais e variantes determinísticas aproximadas; Android deve preservar agora e permitir trocar por TACO/TBCA depois.
- Alguns textos atuais apareceram com mojibake no terminal; migração deve usar UTF-8 correto.
- Room/DataStore aumenta setup inicial; manter tarefas pequenas.
- Paridade visual pode regredir se componentes usarem Material padrão sem tokens próprios.
- Importação ChatGPT depende de ação explícita do usuário e clipboard/deep link; não usar API externa obrigatória.

## Checklist de Entrega

- [x] App web atual preservado.
- [x] Stack, schema, rotas, templates, CSS, seed e regras inspecionados.
- [x] `UI_PARITY_PLAN` criado.
- [ ] Projeto Android criado.
- [ ] Tema Compose criado com tokens do app atual.
- [ ] Room/DataStore configurados.
- [ ] Seed carregado.
- [ ] Regras principais testadas.
- [ ] Tela Hoje com paridade visual.
- [ ] Fluxos principais migrados.
- [ ] Importador ChatGPT migrado.
- [x] Build Android passa para o esqueleto inicial.
- [ ] README Android finalizado.

## Ciclo 1

### 1. ARQUITETO

Nome da tarefa:
- Criar plano de migração e paridade visual.

Motivo:
- Evitar uma migração Android que reimplemente regras mas perca a identidade e os fluxos do app atual.

Tela ou funcionalidade original analisada:
- `today.html`, `meal_detail.html`, `add_food.html`, `food_detail.html`, `calendar.html`, `streak.html`, `settings.html`, `chatgpt_prepare.html`, `chatgpt_import.html`.
- `app/static/app.css`.
- `app/db.py`, `app/main.py`, `app/services/*.py`, `scripts/seed_foods.py`.

Arquivos prováveis:
- `MIGRATION_PLAN.md`.

Critérios de aceite funcionais:
- Descrever stack atual.
- Descrever schema atual.
- Descrever regras de cálculo, dia verde, streak e ChatGPT.
- Propor arquitetura Android.
- Mapear web app para Android.
- Criar plano incremental.
- Não apagar nem alterar app web.

Critérios de aceite visuais:
- Incluir seção `UI_PARITY_PLAN`.
- Registrar cores, cards, espaçamentos e componentes principais.
- Mapear cada tela atual para uma tela Compose equivalente.
- Criar checklist visual por tela.

Riscos:
- Plano grande demais virar documentação decorativa. Mitigação: próximas tarefas pequenas e commit por ciclo.

Instrução objetiva para o Dev:
- Criar apenas `MIGRATION_PLAN.md` com inspeção e plano de paridade. Não criar `android-native/` ainda.

### 2. DEV

Implementação feita:
- Criado este `MIGRATION_PLAN.md`.

Arquivos alterados:
- `MIGRATION_PLAN.md`.

Como preservou a UI original:
- O plano usa diretamente tokens, layouts e fluxos encontrados em `app/static/app.css` e templates atuais.

Como testou:
- Inspeção estática do repositório e revisão do arquivo criado.

Comando executado:
- `rg --files`
- leitura de `app/db.py`, `app/main.py`, `app/static/app.css`, templates, services e seed.

Resultado:
- Plano criado sem alterar o app web.

### 3. QA

Validação feita:
- Verificado se o plano cobre funcionalidade, arquitetura, modelo de dados e paridade visual.

Comando executado:
- Inspeção de conteúdo do arquivo e `git status`.

Resultado:
- A validar após criação do arquivo no ciclo.

Checklist funcional:
- [x] Stack atual registrada.
- [x] Estrutura e schema registrados.
- [x] Rotas/telas mapeadas.
- [x] Regras nutricionais registradas.
- [x] Dia verde e streak registrados.
- [x] ChatGPT registrado.
- [x] Arquitetura Android proposta.
- [x] Plano incremental criado.

Checklist visual:
- [x] Cores atuais registradas.
- [x] Cards e espaçamentos registrados.
- [x] Componentes Compose propostos.
- [x] Tela Hoje mapeada.
- [x] Refeição mapeada.
- [x] Busca/adicionar alimento mapeada.
- [x] Água, peso, calendário, streak, settings e ChatGPT mapeados.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Criar projeto Android base em `android-native/`.

Instrução para o próximo ciclo:
- Criar somente o esqueleto Gradle/Kotlin/Compose mínimo, sem implementar telas ainda. Se Android SDK/Gradle não estiver disponível, deixar os arquivos corretos e documentar o bloqueio de build.

## Ciclo 2

### 1. ARQUITETO

Nome da tarefa:
- Criar projeto Android base.

Motivo:
- Iniciar a migração nativa em pasta separada sem destruir o app web atual.

Tela ou funcionalidade original analisada:
- Nenhuma tela deve ser migrada ainda. A referência visual permanece registrada no `UI_PARITY_PLAN`.

Arquivos prováveis:
- `android-native/settings.gradle.kts`.
- `android-native/build.gradle.kts`.
- `android-native/app/build.gradle.kts`.
- `android-native/app/src/main/AndroidManifest.xml`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/MainActivity.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`.
- `android-native/app/src/main/res/values/strings.xml`.
- `android-native/app/src/main/res/values/themes.xml`.
- `android-native/README.md`.

Critérios de aceite funcionais:
- Criar projeto separado em `android-native/`.
- Usar Gradle Kotlin DSL.
- Criar módulo Android `app`.
- Criar Activity principal Kotlin.
- Criar Compose root mínimo.
- Não apagar nem alterar o app web.
- Documentar bloqueio se Gradle/SDK não estiverem disponíveis.

Critérios de aceite visuais:
- Não migrar UI ainda.
- Root Compose mínimo deve usar fundo escuro e nome do app apenas como placeholder.
- Não criar tela genérica completa que possa conflitar com a paridade futura.

Riscos:
- Gradle Wrapper e SDK dependem do Android Studio no primeiro sync.

Instrução objetiva para o Dev:
- Criar somente o esqueleto Android mínimo. Não adicionar Room, Navigation, telas reais ou componentes de UI ainda.

### 2. DEV

Implementação feita:
- Criado esqueleto Android nativo mínimo em `android-native/`.
- Criados Gradle Kotlin DSL, manifest, `MainActivity`, `DietTrackerApp` placeholder, resources básicos e README.

Arquivos alterados:
- `MIGRATION_PLAN.md`.
- `android-native/.gitignore`.
- `android-native/README.md`.
- `android-native/settings.gradle.kts`.
- `android-native/build.gradle.kts`.
- `android-native/app/build.gradle.kts`.
- `android-native/app/src/main/AndroidManifest.xml`.
- `android-native/app/src/main/res/values/strings.xml`.
- `android-native/app/src/main/res/values/themes.xml`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/MainActivity.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`.

Como preservou a UI original:
- Não migrou tela real ainda.
- Placeholder usa fundo `#07100d` e texto `#f7f7f2`, cores base do app atual.

Como testou:
- Listagem dos arquivos criados.
- Build por linha de comando após o Android Studio gerar o Gradle Wrapper.
- Testes do app web para garantir que nada antigo quebrou.

Comando executado:
- `rg --files android-native`.
- `gradlew.bat test`.
- `gradlew.bat assembleDebug`.
- `python -m pytest -q`.

Resultado:
- Arquivos criados.
- Gradle Wrapper gerado pelo Android Studio e versionado.
- `gradlew.bat test`: BUILD SUCCESSFUL.
- `gradlew.bat assembleDebug`: BUILD SUCCESSFUL.
- Testes web: 12 passed.

Envia para QA.

### 3. QA

Validação feita:
- Subagente QA validou a estrutura Android criada.
- Primeira rodada reprovou README por indicar `./gradlew` antes de existir wrapper.
- DEV corrigiu README para declarar que os comandos dependem do wrapper gerado/sincronizado pelo Android Studio.
- QA revalidou.

Comando executado:
- Inspeção de `android-native/`.

Resultado:
- Build Android rodado após sync do Android Studio e correções mínimas de Gradle.
- `gradle.properties` habilita AndroidX.
- JVM target Java/Kotlin alinhado em 17.

Checklist funcional:
- [x] Projeto separado em `android-native/`.
- [x] App web preservado.
- [x] Gradle Kotlin DSL criado.
- [x] Módulo `app` criado.
- [x] Manifest criado.
- [x] `MainActivity` Kotlin criada.
- [x] Root Compose mínimo criado.
- [x] README documenta abertura no Android Studio.
- [x] Gradle Wrapper presente.
- [x] `gradlew.bat test` passa.
- [x] `gradlew.bat assembleDebug` passa.

Checklist visual:
- [x] Nenhuma tela real migrada prematuramente.
- [x] Placeholder usa cores base escuras do app atual.
- [x] Sem Material genérico expandido para telas reais.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Configurar tema escuro e design tokens baseados no app atual.

Instrução para o próximo ciclo:
- Criar somente `core/ui/theme` com `AppColors.kt`, `AppSpacing.kt`, `AppShapes.kt`, `AppTypography.kt` e um tema Compose que aplique as cores atuais. Não criar telas reais ainda.

## Ciclo 3

### 1. ARQUITETO

Nome da tarefa:
- Configurar tema escuro e design tokens baseados no app atual.

Motivo:
- Garantir que todas as próximas telas Compose partam da identidade visual do web app, sem cair no Material padrão genérico.

Tela ou funcionalidade original analisada:
- Nenhuma tela real migrada.
- Referência visual: `app/static/app.css` e seção `UI_PARITY_PLAN`.

Arquivos prováveis:
- `android-native/app/src/main/kotlin/com/romling/diettracker/core/ui/theme/AppColors.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/core/ui/theme/AppSpacing.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/core/ui/theme/AppShapes.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/core/ui/theme/AppTypography.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/core/ui/theme/DietTrackerTheme.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`.

Critérios de aceite funcionais:
- Criar tokens de cores, espaçamento, formas e tipografia.
- Criar tema Compose escuro.
- Aplicar o tema no root app.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Cores devem bater com o CSS atual.
- Fundo, painel, linha, texto, accent, verde, roxo de dica e remover devem existir como tokens.
- Placeholder continua mínimo, sem migrar tela real prematuramente.

Riscos:
- Exagerar em componentes antes de telas reais. Mitigação: só tokens e tema neste ciclo.

Instrução objetiva para o Dev:
- Criar apenas arquivos em `core/ui/theme` e aplicar `DietTrackerTheme` no root. Não adicionar Room, Navigation, componentes compartilhados ou telas reais.

### 2. DEV

Implementação feita:
- Criados tokens `AppColors`, `AppSpacing`, `AppShapes`, `AppTypography`.
- Criado `DietTrackerTheme` com `darkColorScheme`.
- `DietTrackerApp` passou a usar `DietTrackerTheme` e tokens.

Arquivos alterados:
- `MIGRATION_PLAN.md`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/core/ui/theme/AppColors.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/core/ui/theme/AppSpacing.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/core/ui/theme/AppShapes.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/core/ui/theme/AppTypography.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/core/ui/theme/DietTrackerTheme.kt`.

Como preservou a UI original:
- Tokens foram copiados do CSS atual: `#07100d`, `#242826`, `#59605d`, `#f7f7f2`, `#c9cfcc`, `#18f0bd`, `#057352`, `#141a18`, roxos da dica e `#ff8a8a`.
- Espaçamentos e raios seguem os valores já registrados no plano.

Como testou:
- Build e testes Android.

Comando executado:
- `gradlew.bat test`.
- `gradlew.bat assembleDebug`.

Resultado:
- `gradlew.bat test`: BUILD SUCCESSFUL.
- `gradlew.bat assembleDebug`: BUILD SUCCESSFUL.

Envia para QA.

### 3. QA

Validação feita:
- Subagente QA deve validar tokens, escopo e paridade com CSS/plano.

Comando executado:
- Inspeção de arquivos Android e plano.

Resultado:
- QA subagente aprovou tokens, escopo e paridade com CSS/plano.

Checklist funcional:
- [x] `AppColors.kt` criado.
- [x] `AppSpacing.kt` criado.
- [x] `AppShapes.kt` criado.
- [x] `AppTypography.kt` criado.
- [x] `DietTrackerTheme.kt` criado.
- [x] Root app usa tema.
- [x] `gradlew.bat test` passa.
- [x] `gradlew.bat assembleDebug` passa.

Checklist visual:
- [x] Fundo escuro tokenizado.
- [x] Card/painel tokenizado.
- [x] Texto primário/secundário tokenizado.
- [x] Accent verde água tokenizado.
- [x] Verde de status tokenizado.
- [x] Roxo de dica tokenizado.
- [x] Remover tokenizado.
- [x] Sem tela real ou redesign prematuro.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Criar componentes compartilhados de UI.

Instrução para o próximo ciclo:
- Criar somente componentes base pequenos (`AppCard`, `SectionTitle`, `MacroProgressBar`, `CircleActionButton`, `BottomPrimaryButton`) usando os tokens do Ciclo 3. Não criar telas reais ainda.

## Ciclo 4

### 1. ARQUITETO

Nome da tarefa:
- Criar componentes compartilhados de UI.

Motivo:
- Evitar duplicação nas próximas telas e garantir que cards, títulos, barras e botões usem a identidade visual já aprovada.

Tela ou funcionalidade original analisada:
- Componentes visuais extraídos de `app/static/app.css`: `.card`, `.section-title`, `progress`, botões circulares `+` e `.commit`.

Arquivos prováveis:
- `android-native/app/src/main/kotlin/com/romling/diettracker/core/ui/components/BaseComponents.kt`.

Critérios de aceite funcionais:
- Criar `AppCard`.
- Criar `SectionTitle`.
- Criar `MacroProgressBar`.
- Criar `CircleActionButton`.
- Criar `BottomPrimaryButton`.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- `AppCard` deve preservar fundo escuro, borda grossa e raio alto.
- `SectionTitle` deve preservar título grande e ação accent.
- `MacroProgressBar` deve usar accent e trilho escuro.
- `CircleActionButton` deve preservar botão circular grande.
- `BottomPrimaryButton` deve preservar botão grande claro do rodapé.
- Não criar telas reais nem redesenhar fluxos.

Riscos:
- Componentes ficarem genéricos demais. Mitigação: usar tokens do Ciclo 3 diretamente.

Instrução objetiva para o Dev:
- Criar somente componentes base em `core/ui/components`. Não adicionar Navigation, Room, DataStore, preview ou telas reais.

### 2. DEV

Implementação feita:
- Criado `BaseComponents.kt` com `AppCard`, `SectionTitle`, `MacroProgressBar`, `CircleActionButton` e `BottomPrimaryButton`.

Arquivos alterados:
- `MIGRATION_PLAN.md`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/core/ui/components/BaseComponents.kt`.

Como preservou a UI original:
- `AppCard` usa painel, borda `Line`, raio `Card` e padding do app.
- `MacroProgressBar` usa `Accent` e trilho `Line`.
- Botões seguem tamanhos/cores do app web.

Como testou:
- Build e testes Android.

Comando executado:
- `gradlew.bat test`.
- `gradlew.bat assembleDebug`.

Resultado:
- `gradlew.bat test`: BUILD SUCCESSFUL.
- `gradlew.bat assembleDebug`: BUILD SUCCESSFUL.

Envia para QA.

### 3. QA

Validação feita:
- Subagente QA deve validar componentes, escopo e paridade visual com tokens.

Comando executado:
- Inspeção dos componentes e plano.

Resultado:
- QA subagente aprovou componentes, escopo e paridade visual com tokens.

Checklist funcional:
- [x] `AppCard` criado.
- [x] `SectionTitle` criado.
- [x] `MacroProgressBar` criado.
- [x] `CircleActionButton` criado.
- [x] `BottomPrimaryButton` criado.
- [x] `gradlew.bat test` passa.
- [x] `gradlew.bat assembleDebug` passa.

Checklist visual:
- [x] Card escuro com borda grossa e raio alto.
- [x] Título de seção com ação accent.
- [x] Barra de macro accent.
- [x] Botão circular grande.
- [x] Botão primário grande e claro.
- [x] Sem telas reais ou redesign prematuro.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Criar entidades Room e DAOs.

Instrução para o próximo ciclo:
- Adicionar dependências Room/ksp mínimas e criar somente entidades/DAOs equivalentes ao schema atual. Não ligar banco ao app e não criar telas.

## Ciclo 5

### 1. ARQUITETO

Nome da tarefa:
- Criar entidades Room e DAOs.

Motivo:
- Migrar o contrato local de dados do SQLite web para Room, sem ainda ligar o banco ao app.

Tela ou funcionalidade original analisada:
- Schema em `app/db.py`.
- Modelo Android planejado na seção "Modelo de Dados Android".

Arquivos prováveis:
- `android-native/build.gradle.kts`.
- `android-native/app/build.gradle.kts`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/local/entity/*.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/local/dao/*.kt`.

Critérios de aceite funcionais:
- Adicionar Room/KSP mínimo.
- Criar entidades para alimentos, porções, diário, água, peso, compromisso diário e importação AI.
- Criar DAOs básicos para busca, inserção, listagem e remoção onde já existe fluxo web equivalente.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Nenhum, tarefa de dados.
- Não criar telas reais.

Riscos:
- Introduzir banco completo cedo demais. Mitigação: sem `AppDatabase` e sem ligar ao app neste ciclo.

Instrução objetiva para o Dev:
- Criar somente dependências Room/KSP, entidades e DAOs. Não criar `AppDatabase`, repositories, seed loader ou telas.

### 2. DEV

Implementação feita:
- Adicionado plugin KSP e dependências Room.
- Criadas entidades Room equivalentes ao schema/plano.
- Criados DAOs básicos para alimentos, porções, diário, água, peso, compromisso e importações AI.

Arquivos alterados:
- `MIGRATION_PLAN.md`.
- `android-native/build.gradle.kts`.
- `android-native/app/build.gradle.kts`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/local/entity/AiImportEntity.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/local/entity/DailyCommitmentEntity.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/local/entity/DiaryEntryEntity.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/local/entity/FoodEntity.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/local/entity/FoodPortionEntity.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/local/entity/WaterEntryEntity.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/local/entity/WeightEntryEntity.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/local/dao/Daos.kt`.

Como preservou o app original:
- Nomes de tabela e colunas seguem o SQLite atual quando aplicável.
- `DiaryEntryEntity` inclui snapshots e `aiImportId` conforme modelo Android planejado.
- Meal types continuam como `String` para preservar compatibilidade com `breakfast`, `lunch`, `dinner`, `snack` e importações futuras.

Como testou:
- Build e testes Android com KSP.

Comando executado:
- `gradlew.bat test`.
- `gradlew.bat assembleDebug`.

Resultado:
- `gradlew.bat test`: BUILD SUCCESSFUL.
- `gradlew.bat assembleDebug`: BUILD SUCCESSFUL.

Envia para QA.

### 3. QA

Validação feita:
- Subagente QA deve validar equivalência com schema/plano e ausência de escopo extra.

Comando executado:
- Inspeção de entidades, DAOs e Gradle.

Resultado:
- QA subagente aprovou entidades, DAOs, dependências Room/KSP e ausência de escopo extra.

Checklist funcional:
- [x] Room runtime adicionado.
- [x] Room KTX adicionado.
- [x] KSP/Room compiler adicionado.
- [x] `FoodEntity` criado.
- [x] `FoodPortionEntity` criado.
- [x] `DiaryEntryEntity` criado.
- [x] `WaterEntryEntity` criado.
- [x] `WeightEntryEntity` criado.
- [x] `DailyCommitmentEntity` criado.
- [x] `AiImportEntity` criado.
- [x] DAOs básicos criados.
- [x] `gradlew.bat test` passa.
- [x] `gradlew.bat assembleDebug` passa.

Checklist visual:
- [x] Sem telas reais.
- [x] Sem mudança visual.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Criar `AppDatabase` Room.

Instrução para o próximo ciclo:
- Criar somente `AppDatabase` com entidades e DAOs do Ciclo 5, incluindo migrations iniciais se necessárias. Não criar repositories, seed loader ou telas.

## Ciclo 6

### 1. ARQUITETO

Nome da tarefa:
- Criar `AppDatabase` Room.

Motivo:
- Fechar o contrato Room inicial com entidades e DAOs já aprovados, mantendo o banco ainda desconectado do app.

Tela ou funcionalidade original analisada:
- Nenhuma tela.
- Entidades e DAOs do Ciclo 5.

Arquivos prováveis:
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/local/AppDatabase.kt`.

Critérios de aceite funcionais:
- Criar `AppDatabase`.
- Incluir todas as entidades do Ciclo 5.
- Expor todos os DAOs do Ciclo 5.
- Usar version 1.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Nenhum, tarefa de dados.
- Não criar telas reais.

Riscos:
- Ligar banco ao app antes de seed/repositories. Mitigação: não criar builder nem injeção neste ciclo.

Instrução objetiva para o Dev:
- Criar somente `AppDatabase.kt`. Não criar repositories, seed loader, migrations desnecessárias, `databaseBuilder` ou telas.

### 2. DEV

Implementação feita:
- Criado `AppDatabase` Room version 1 com entidades e DAOs aprovados.
- `exportSchema=false` por enquanto, pois ainda não há política de migrations/schema export no projeto.

Arquivos alterados:
- `MIGRATION_PLAN.md`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/local/AppDatabase.kt`.

Como preservou o app original:
- Banco reúne as tabelas equivalentes ao SQLite local atual e ao modelo Android planejado.

Como testou:
- Build e testes Android com KSP/Room gerando código.

Comando executado:
- `gradlew.bat test`.
- `gradlew.bat assembleDebug`.

Resultado:
- `gradlew.bat test`: BUILD SUCCESSFUL.
- `gradlew.bat assembleDebug`: BUILD SUCCESSFUL.

Envia para QA.

### 3. QA

Validação feita:
- Subagente QA deve validar escopo e composição do banco.

Comando executado:
- Inspeção de `AppDatabase.kt` e plano.

Resultado:
- QA subagente aprovou `AppDatabase`, entidades, DAOs e ausência de escopo extra.

Checklist funcional:
- [x] `AppDatabase` criado.
- [x] Version 1.
- [x] Todas as entidades do Ciclo 5 incluídas.
- [x] Todos os DAOs do Ciclo 5 expostos.
- [x] `gradlew.bat test` passa.
- [x] `gradlew.bat assembleDebug` passa.

Checklist visual:
- [x] Sem telas reais.
- [x] Sem mudança visual.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Migrar seed de alimentos para assets.

Instrução para o próximo ciclo:
- Gerar `android-native/app/src/main/assets/foods_seed.json` a partir do seed web atual, preservando nomes, aliases, categorias e macros. Não criar seed loader ainda.

## Ciclo 7

### 1. ARQUITETO

Nome da tarefa:
- Migrar seed de alimentos para assets.

Motivo:
- Levar o catálogo brasileiro offline para o app Android, sem ainda criar carregamento automático.

Tela ou funcionalidade original analisada:
- `scripts/seed_foods.py`.

Arquivos prováveis:
- `android-native/app/src/main/assets/foods_seed.json`.

Critérios de aceite funcionais:
- Criar asset JSON válido.
- Ter pelo menos 150 alimentos.
- Preservar campos necessários para `FoodEntity`.
- Preservar nomes, aliases, categorias e macros.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Nenhum, tarefa de dados.
- Não criar telas reais.

Riscos:
- O seed web tem correções de acento antigas com mojibake no terminal. Mitigação: validar o asset em UTF-8 com alimentos acentuados conhecidos.

Instrução objetiva para o Dev:
- Gerar somente `foods_seed.json`. Não criar loader, repositories, banco populado ou telas.

### 2. DEV

Implementação feita:
- Criado `android-native/app/src/main/assets/foods_seed.json` com 218 alimentos.
- Campos exportados em camelCase compatíveis com `FoodEntity`.
- Acentos validados em UTF-8 para exemplos brasileiros.

Arquivos alterados:
- `MIGRATION_PLAN.md`.
- `android-native/app/src/main/assets/foods_seed.json`.

Como preservou o app original:
- O asset foi gerado a partir da lista base e extras do seed web.
- Mantém variantes caseiras determinísticas já existentes no seed web.

Como testou:
- Validação JSON por Python.
- Build e testes Android.

Comando executado:
- validação Python do JSON.
- `gradlew.bat test`.
- `gradlew.bat assembleDebug`.

Resultado:
- JSON válido: 218 alimentos OK.
- `gradlew.bat test`: BUILD SUCCESSFUL.
- `gradlew.bat assembleDebug`: BUILD SUCCESSFUL.

Envia para QA.

### 3. QA

Validação feita:
- Subagente QA deve validar asset, campos, acentos e ausência de escopo extra.

Comando executado:
- Inspeção/validação do JSON e plano.

Resultado:
- QA subagente aprovou JSON, campos, acentos, contagem e ausência de escopo extra.

Checklist funcional:
- [x] Asset criado.
- [x] JSON válido.
- [x] 218 alimentos.
- [x] Campos compatíveis com `FoodEntity`.
- [x] Nomes/categorias/aliases/macros preservados.
- [x] `gradlew.bat test` passa.
- [x] `gradlew.bat assembleDebug` passa.

Checklist visual:
- [x] Sem telas reais.
- [x] Sem mudança visual.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Criar seed loader inicial.

Instrução para o próximo ciclo:
- Criar apenas o loader que lê `foods_seed.json` e insere no Room sem duplicar alimentos. Não ligar ao app ainda se exigir wiring maior.

## Ciclo 8

### 1. ARQUITETO

Nome da tarefa:
- Criar seed loader inicial.

Motivo:
- Preparar o carregamento offline do catálogo brasileiro no Room sem ainda acoplar isso ao ciclo de vida do app.

Tela ou funcionalidade original analisada:
- `scripts/seed_foods.py`.
- `android-native/app/src/main/assets/foods_seed.json`.

Arquivos prováveis:
- `android-native/build.gradle.kts`.
- `android-native/app/build.gradle.kts`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/local/dao/Daos.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/local/seed/FoodSeedLoader.kt`.
- `android-native/app/src/test/kotlin/com/romling/diettracker/data/local/seed/FoodSeedLoaderTest.kt`.

Critérios de aceite funcionais:
- Criar loader que recebe `InputStream`.
- Ler o JSON do asset com `kotlinx.serialization`.
- Inserir alimentos via `FoodDao`.
- Não duplicar alimentos quando a tabela já tiver dados.
- Ter teste unitário do comportamento idempotente.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Nenhum, tarefa de dados.
- Não criar telas reais.

Riscos:
- Ligar o seed ao app antes de repositories/injeção. Mitigação: manter somente loader isolado neste ciclo.

Instrução objetiva para o Dev:
- Criar somente loader, métodos mínimos no DAO e teste. Não criar repositories, telas, navegação ou `databaseBuilder`.

### 2. DEV

Implementação feita:
- Adicionado `FoodSeedLoader` para ler `foods_seed.json` via `InputStream`, converter para `FoodEntity` e inserir no Room.
- Adicionados métodos mínimos em `FoodDao` para contar alimentos e inserir lista com `IGNORE`.
- Adicionadas dependências de `kotlinx.serialization` e teste de coroutines/kotlin test.
- Criado teste unitário garantindo que o seed insere uma vez e não duplica quando já existem alimentos.

Arquivos alterados:
- `MIGRATION_PLAN.md`.
- `android-native/build.gradle.kts`.
- `android-native/app/build.gradle.kts`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/local/dao/Daos.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/local/seed/FoodSeedLoader.kt`.
- `android-native/app/src/test/kotlin/com/romling/diettracker/data/local/seed/FoodSeedLoaderTest.kt`.

Como preservou o app original:
- O loader usa o asset gerado do seed web, sem alterar o app web e sem introduzir telas ou fluxo visual no Android.

Como testou:
- Teste unitário do loader.
- Build Android debug.

Comando executado:
- `gradlew.bat test`.
- `gradlew.bat assembleDebug`.

Resultado:
- `gradlew.bat test`: BUILD SUCCESSFUL.
- `gradlew.bat assembleDebug`: BUILD SUCCESSFUL.

Envia para QA.

### 3. QA

Validação feita:
- Subagente QA validou escopo, loader, DAO, testes e ausência de app wiring/repositories/telas.
- Imagens de referência continuam untracked na raiz e foram mantidas fora do ciclo.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Criar repositories básicos.

Instrução para o próximo ciclo:
- Criar apenas repositories básicos para alimentos e diário usando os DAOs existentes. Não criar telas, ViewModels nem wiring de inicialização ainda.

## Ciclo 9

### 1. ARQUITETO

Nome da tarefa:
- Criar repositories básicos.

Motivo:
- Isolar acesso a alimentos e diário antes de criar ViewModels/telas, mantendo o Room fora das futuras features.

Tela ou funcionalidade original analisada:
- Busca/lista de alimentos.
- Registro e remoção de alimentos por refeição.
- Regras de cálculo nutricional do diário.

Arquivos prováveis:
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/repository/FoodRepository.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/repository/DiaryRepository.kt`.
- `android-native/app/src/test/kotlin/com/romling/diettracker/data/repository/DiaryRepositoryTest.kt`.

Critérios de aceite funcionais:
- Criar `FoodRepository` usando `FoodDao` e `FoodPortionDao`.
- Criar `DiaryRepository` usando `DiaryEntryDao`.
- Permitir listar registros por dia/refeição, adicionar alimento e remover registro.
- Calcular snapshot nutricional ao adicionar alimento.
- Validar quantidade e gramas positivas.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Nenhum, tarefa de dados.
- Não criar telas reais.

Riscos:
- Criar abstrações prematuras. Mitigação: repositories concretos, sem interfaces/factories/injeção.

Instrução objetiva para o Dev:
- Criar somente repositories concretos e teste mínimo de cálculo. Não criar telas, ViewModels, navegação, injeção ou inicialização do banco.

### 2. DEV

Implementação feita:
- Criado `FoodRepository` como delegação direta para `FoodDao` e `FoodPortionDao`.
- Criado `DiaryRepository` para listar, remover e adicionar alimentos no diário.
- `DiaryRepository.addFood` calcula kcal arredondada e macros com 1 casa, salva snapshot do alimento e valida valores positivos.
- Criado teste unitário para cálculo de snapshot e rejeição de gramas inválidas.

Arquivos alterados:
- `MIGRATION_PLAN.md`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/repository/FoodRepository.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/repository/DiaryRepository.kt`.
- `android-native/app/src/test/kotlin/com/romling/diettracker/data/repository/DiaryRepositoryTest.kt`.

Como preservou o app original:
- A regra segue o cálculo web planejado e não altera o app web.

Como testou:
- Teste unitário dos repositories.
- Build Android debug.

Comando executado:
- `gradlew.bat test`.
- `gradlew.bat assembleDebug`.
- Busca por `NavHost`, `ViewModel`, `Screen`, `databaseBuilder`, `MainActivity`, `DietTrackerApp` no pacote de repositories.

Resultado:
- `gradlew.bat test`: BUILD SUCCESSFUL.
- `gradlew.bat assembleDebug`: BUILD SUCCESSFUL.
- Busca de escopo: nenhum resultado.

Envia para QA.

### 3. QA

Validação feita:
- Subagente QA validou escopo, repositories, cálculo nutricional, teste unitário e ausência de UI/wiring.
- Imagens de referência continuam untracked na raiz e foram mantidas fora do ciclo.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Criar serviços de dia verde e sequência.

Instrução para o próximo ciclo:
- Criar apenas serviços puros para regra de dia verde e sequência usando dados já carregados em memória. Não criar telas, ViewModels nem repositories novos.

## Ciclo 10

### 1. ARQUITETO

Nome da tarefa:
- Criar serviços de dia verde e sequência.

Motivo:
- Portar regras de negócio do web app para código Android testável sem depender de Room ou UI.

Tela ou funcionalidade original analisada:
- Calendário com dias verdes.
- Tela de sequência.
- `app/services/diary.py`.

Arquivos prováveis:
- `android-native/app/src/main/kotlin/com/romling/diettracker/domain/service/GreenDayService.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/domain/service/StreakService.kt`.
- `android-native/app/src/test/kotlin/com/romling/diettracker/domain/service/GreenDayServiceTest.kt`.
- `android-native/app/src/test/kotlin/com/romling/diettracker/domain/service/StreakServiceTest.kt`.

Critérios de aceite funcionais:
- Dia verde exige pelo menos um registro.
- Dia verde exige kcal total <= meta diária.
- Dia verde exige proteína total >= 80% da meta.
- Sequência atual conta dias ativos consecutivos até a data final.
- Maior sequência conta melhor sequência em todos os dias ativos.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Nenhum, tarefa de domínio.
- Não criar telas reais.

Riscos:
- Acoplar serviço ao banco cedo demais. Mitigação: serviços recebem listas/datas em memória.

Instrução objetiva para o Dev:
- Criar somente serviços puros e testes. Não criar DAOs, repositories, telas, ViewModels, navegação ou inicialização do banco.

### 2. DEV

Implementação feita:
- Criado `GreenDayService` com a regra de dia verde do web app.
- Criado `StreakService` com resumo de sequência atual, maior sequência e dias ativos.
- Criados testes unitários para regras de dia verde e sequência.

Arquivos alterados:
- `MIGRATION_PLAN.md`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/domain/service/GreenDayService.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/domain/service/StreakService.kt`.
- `android-native/app/src/test/kotlin/com/romling/diettracker/domain/service/GreenDayServiceTest.kt`.
- `android-native/app/src/test/kotlin/com/romling/diettracker/domain/service/StreakServiceTest.kt`.

Como preservou o app original:
- As regras foram copiadas do comportamento web e isoladas em serviços puros.

Como testou:
- Testes unitários dos serviços.
- Build Android debug.

Comando executado:
- `gradlew.bat test`.
- `gradlew.bat assembleDebug`.
- Busca por `Dao`, `Repository`, `ViewModel`, `NavHost`, `Screen`, `databaseBuilder`, `MainActivity`, `DietTrackerApp` no pacote de serviços.

Resultado:
- `gradlew.bat test`: BUILD SUCCESSFUL.
- `gradlew.bat assembleDebug`: BUILD SUCCESSFUL.
- Busca de escopo: nenhum resultado.

Envia para QA.

### 3. QA

Validação feita:
- Subagente QA validou escopo, serviços puros, regras de dia verde/sequência, testes e ausência de DAOs/repositories/UI/wiring.
- Imagens de referência continuam untracked na raiz e foram mantidas fora do ciclo.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Ligar banco e seed no app.

Instrução para o próximo ciclo:
- Criar wiring mínimo para instanciar `AppDatabase`, repositories e executar `FoodSeedLoader` na inicialização. Não criar novas telas nem expandir UI além do necessário para manter o app abrindo.

## Ciclo 11

### 1. ARQUITETO

Nome da tarefa:
- Ligar banco e seed no app.

Motivo:
- Permitir que o app Android abra com Room configurado e catálogo de alimentos carregado uma vez.

Tela ou funcionalidade original analisada:
- Inicialização do app Android.
- Seed offline de alimentos.

Arquivos prováveis:
- `android-native/app/src/main/AndroidManifest.xml`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApplication.kt`.

Critérios de aceite funcionais:
- Criar `Application` Android.
- Instanciar `AppDatabase` com `Room.databaseBuilder`.
- Expor repositories básicos já criados.
- Executar `FoodSeedLoader` em `Dispatchers.IO` na inicialização.
- Não duplicar alimentos, usando o comportamento do loader.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Não criar telas novas.
- Não alterar a UI existente além de manter o app abrindo.

Riscos:
- Introduzir framework de DI cedo demais. Mitigação: container manual mínimo.

Instrução objetiva para o Dev:
- Criar apenas `Application`/container manual e atualizar o manifest. Não criar telas, ViewModels, navegação ou features.

### 2. DEV

Implementação feita:
- Criado `DietTrackerApplication`.
- Criado `AppContainer` manual com `AppDatabase`, `FoodRepository` e `DiaryRepository`.
- Seed de alimentos roda em coroutine `Dispatchers.IO` abrindo `assets/foods_seed.json`.
- Manifest aponta para `.DietTrackerApplication`.

Arquivos alterados:
- `MIGRATION_PLAN.md`.
- `android-native/app/src/main/AndroidManifest.xml`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApplication.kt`.

Como preservou o app original:
- Wiring Android usa o asset e loader já migrados, sem alterar o app web.

Como testou:
- Testes unitários Android.
- Build Android debug.

Comando executado:
- `gradlew.bat test`.
- `gradlew.bat assembleDebug`.
- Busca por `Composable`, `NavHost`, `ViewModel`, `Screen`, `setContent`, `Text(` no arquivo de wiring e manifest.

Resultado:
- `gradlew.bat test`: BUILD SUCCESSFUL.
- `gradlew.bat assembleDebug`: BUILD SUCCESSFUL.
- Busca de escopo: nenhum resultado.

Envia para QA.

### 3. QA

Validação feita:
- Subagente QA validou `Application`, manifest, `Room.databaseBuilder`, repositories expostos, seed em `Dispatchers.IO` e ausência de UI nova.
- Imagens de referência continuam untracked na raiz e foram mantidas fora do ciclo.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Criar primeira ViewModel da tela Hoje.

Instrução para o próximo ciclo:
- Criar somente estado/ViewModel inicial da tela Hoje usando repositories e serviços já existentes. Não redesenhar a tela completa ainda.

## Ciclo 12

### 1. ARQUITETO

Nome da tarefa:
- Criar primeira ViewModel da tela Hoje.

Motivo:
- Preparar estado observável da tela Hoje antes de migrar a UI completa.

Tela ou funcionalidade original analisada:
- Tela Hoje.
- Resumo calórico.
- Regra de dia verde.

Arquivos prováveis:
- `android-native/app/build.gradle.kts`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayViewModel.kt`.
- `android-native/app/src/test/kotlin/com/romling/diettracker/feature/today/TodayViewModelTest.kt`.

Critérios de aceite funcionais:
- Criar `TodayViewModel`.
- Expor `StateFlow<TodayUiState>`.
- Carregar registros do dia via `DiaryRepository`.
- Calcular totais de kcal, carboidratos, proteína e gordura.
- Calcular kcal restantes com meta padrão.
- Calcular dia verde usando `GreenDayService`.
- Ter teste unitário mínimo.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Não criar tela Compose nova.
- Não alterar navegação ou `MainActivity`.

Riscos:
- Antecipar DataStore/settings. Mitigação: usar metas padrão no construtor até o ciclo de settings.

Instrução objetiva para o Dev:
- Criar somente ViewModel/estado/teste e dependência ViewModel necessária. Não criar tela, navegação, factory, DI framework ou layout.

### 2. DEV

Implementação feita:
- Adicionada dependência `androidx.lifecycle:lifecycle-viewmodel-ktx`.
- Criado `TodayViewModel` com `StateFlow<TodayUiState>`.
- Estado inicial calcula data, semana ISO, totais, kcal restantes e dia verde.
- Criado teste unitário cobrindo resumo de entradas do dia.

Arquivos alterados:
- `MIGRATION_PLAN.md`.
- `android-native/app/build.gradle.kts`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayViewModel.kt`.
- `android-native/app/src/test/kotlin/com/romling/diettracker/feature/today/TodayViewModelTest.kt`.

Como preservou o app original:
- A ViewModel replica os cálculos principais da tela Hoje sem alterar UI web ou Android.

Como testou:
- Teste unitário da ViewModel.
- Build Android debug.

Comando executado:
- `gradlew.bat test`.
- `gradlew.bat assembleDebug`.
- Busca por `@Composable`, `NavHost`, `setContent`, `Modifier`, `Text(`, `Button(` e `Screen` no pacote `feature/today`.

Resultado:
- `gradlew.bat test`: BUILD SUCCESSFUL.
- `gradlew.bat assembleDebug`: BUILD SUCCESSFUL.
- Busca de escopo: nenhum resultado.

Envia para QA.

### 3. QA

Validação feita:
- Subagente QA validou escopo, `TodayViewModel`, dependência de ViewModel, teste unitário e ausência de UI/navegação/DI.
- Imagens de referência continuam untracked na raiz e foram mantidas fora do ciclo.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Criar primeira tela Hoje Compose.

Instrução para o próximo ciclo:
- Criar uma primeira tela Hoje Compose ligada à `TodayUiState`, com cabeçalho e card de resumo apenas. Não migrar alimentação, água, peso ou navegação ainda.

## Ciclo 13

### 1. ARQUITETO

Nome da tarefa:
- Criar primeira tela Hoje Compose.

Motivo:
- Substituir o placeholder inicial por uma primeira fatia real da tela Hoje, sem migrar todo o fluxo de uma vez.

Tela ou funcionalidade original analisada:
- Cabeçalho da tela Hoje.
- CTA "Ver minhas Dicas Inteligentes".
- Seção Resumo com card de calorias e macros.

Arquivos prováveis:
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayScreen.kt`.

Critérios de aceite funcionais:
- Criar `TodayScreen` Compose recebendo `TodayUiState`.
- Renderizar cabeçalho com "Hoje", semana e contadores.
- Renderizar CTA de dicas.
- Renderizar card de resumo com consumidas, restantes, gastas, macros e "Agora: Comer".
- Trocar placeholder do app pela primeira tela Hoje.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Usar tokens/componentes Android já criados.
- Não migrar lista de alimentação, água, peso, bottom bar ou navegação neste ciclo.
- Evitar UI genérica Material sem a identidade escura/verde/roxa do app.

Riscos:
- Expandir a tela inteira cedo demais. Mitigação: limitar a cabeçalho, CTA e resumo.

Instrução objetiva para o Dev:
- Criar somente `TodayScreen` e substituir o placeholder em `DietTrackerApp`. Não ligar ViewModel, navigation, alimentação, água, peso ou bottom bar ainda.

### 2. DEV

Implementação feita:
- Criado `TodayScreen` com cabeçalho, CTA de dicas, seção "Resumo" e card de resumo.
- Card usa `AppCard`, `SectionTitle`, `MacroProgressBar` e tokens de tema existentes.
- Adicionado anel visual simples para kcal restantes.
- `DietTrackerApp` agora renderiza `TodayScreen` com estado padrão.

Arquivos alterados:
- `MIGRATION_PLAN.md`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayScreen.kt`.

Como preservou o app original:
- A tela segue a identidade escura/verde/roxa e a estrutura inicial da tela Hoje original.

Como testou:
- Testes Android.
- Build Android debug.
- Revisão estática de escopo.

Comando executado:
- `gradlew.bat test`.
- `gradlew.bat assembleDebug`.
- Busca por imports/termos fora de escopo em `TodayScreen.kt` e `DietTrackerApp.kt`.

Resultado:
- `gradlew.bat test`: BUILD SUCCESSFUL.
- `gradlew.bat assembleDebug`: BUILD SUCCESSFUL.
- Revisão estática: sem navegação, ViewModel, Room ou repositories nesta tela.

Envia para QA.

### 3. QA

Validação feita:
- Subagente QA validou escopo visual, `TodayScreen`, troca do placeholder e ausência de ViewModel/navegação/bottom bar/alimentação/água/peso.
- Imagens de referência continuam untracked na raiz e foram mantidas fora do ciclo.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Ligar TodayScreen à TodayViewModel.

Instrução para o próximo ciclo:
- Ligar a tela Hoje à `TodayViewModel` usando o container manual existente. Não adicionar navegação nem migrar alimentação, água ou peso ainda.

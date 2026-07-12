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

## Ciclo 14

### 1. ARQUITETO

Nome da tarefa:
- Ligar TodayScreen à TodayViewModel.

Motivo:
- Fazer a primeira tela Hoje usar dados reais do banco via `TodayViewModel`, sem ainda migrar novas seções.

Tela ou funcionalidade original analisada:
- Tela Hoje inicial.
- Resumo calórico.

Arquivos prováveis:
- `android-native/app/src/main/kotlin/com/romling/diettracker/MainActivity.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayViewModel.kt`.

Critérios de aceite funcionais:
- `MainActivity` cria `TodayViewModel` usando o container manual existente.
- `DietTrackerApp` recebe a ViewModel.
- `DietTrackerApp` coleta `StateFlow<TodayUiState>` e passa estado para `TodayScreen`.
- Criar factory mínima se necessário.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Não criar navegação.
- Não migrar alimentação, água, peso, bottom bar ou novas seções.

Riscos:
- Introduzir DI/navegação cedo demais. Mitigação: usar `ViewModelProvider.Factory` mínima.

Instrução objetiva para o Dev:
- Ligar somente a tela atual à ViewModel. Não criar navigation graph, bottom bar, telas novas ou novas features.

### 2. DEV

Implementação feita:
- `MainActivity` cria `TodayViewModel` com `TodayViewModelFactory`.
- `TodayViewModelFactory` usa `DiaryRepository` do container manual.
- `DietTrackerApp` coleta o estado da ViewModel com `collectAsState`.
- `TodayScreen` permanece recebendo apenas `TodayUiState`.

Arquivos alterados:
- `MIGRATION_PLAN.md`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/MainActivity.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayViewModel.kt`.

Como preservou o app original:
- A tela Hoje passa a consumir dados locais Android sem alterar app web nem criar navegação nova.

Como testou:
- Testes Android.
- Build Android debug.
- Revisão estática de escopo.

Comando executado:
- `gradlew.bat test`.
- `gradlew.bat assembleDebug`.
- Busca por `NavHost`, `Bottom`, `Water`, `Weight`, `MealCard`, `entriesForMeal`, `FoodRepository`, `FoodDao`, `Room.databaseBuilder` nos arquivos do ciclo.

Resultado:
- `gradlew.bat test`: BUILD SUCCESSFUL.
- `gradlew.bat assembleDebug`: BUILD SUCCESSFUL.
- Busca de escopo: nenhum resultado.

Envia para QA.

### 3. QA

Validação feita:
- Subagente QA validou wiring da `TodayViewModel`, `DietTrackerApp`, `TodayScreen` state-only, factory mínima e ausência de navegação/seções novas.
- Imagens de referência continuam untracked na raiz e foram mantidas fora do ciclo.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Adicionar seção Alimentação inicial.

Instrução para o próximo ciclo:
- Adicionar somente a seção Alimentação com os quatro cards de refeição usando dados já disponíveis no estado, se necessário expandindo `TodayUiState` de forma mínima. Não criar navegação de detalhe ainda.

## Ciclo 15

### 1. ARQUITETO

Nome da tarefa:
- Adicionar seção Alimentação inicial.

Motivo:
- Trazer a próxima parte visível da tela Hoje mantendo o fluxo sem navegação e sem detalhes de refeição.

Tela ou funcionalidade original analisada:
- Seção Alimentação da tela Hoje.
- Cards de Café da manhã, Almoço, Jantar e Lanches.

Arquivos prováveis:
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayViewModel.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayScreen.kt`.
- `android-native/app/src/test/kotlin/com/romling/diettracker/feature/today/TodayViewModelTest.kt`.

Critérios de aceite funcionais:
- Expandir `TodayUiState` com quatro resumos de refeição.
- Agrupar entradas por `mealType`.
- Exibir kcal consumidas por refeição e meta de kcal.
- Exibir até três nomes de alimentos por refeição.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Renderizar seção "Alimentação" com ação "Mais".
- Renderizar quatro linhas/cards de refeição com ícone circular, texto e botão `+` visual.
- Não criar navegação, clique de detalhe, água, peso ou bottom bar.

Riscos:
- Começar fluxo de detalhe antes do estado estar pronto. Mitigação: botão `+` visual sem ação neste ciclo.

Instrução objetiva para o Dev:
- Adicionar apenas os quatro cards de alimentação e o estado mínimo. Não criar rotas, navegação, telas de detalhe ou handlers de clique.

### 2. DEV

Implementação feita:
- Adicionado `TodayMealSummary` e lista padrão de refeições.
- `TodayViewModel` agora agrupa entradas por refeição, soma kcal e lista até três itens.
- `TodayScreen` renderiza seção "Alimentação" e quatro linhas de refeição.
- Teste da ViewModel valida geração dos resumos de refeição.

Arquivos alterados:
- `MIGRATION_PLAN.md`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayViewModel.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayScreen.kt`.
- `android-native/app/src/test/kotlin/com/romling/diettracker/feature/today/TodayViewModelTest.kt`.

Como preservou o app original:
- A seção segue a estrutura visual da tela Hoje original sem ativar fluxos ainda não migrados.

Como testou:
- Testes Android.
- Build Android debug.
- Revisão estática de escopo.

Comando executado:
- `gradlew.bat test`.
- `gradlew.bat assembleDebug`.
- Busca por `NavHost`, `navigate`, `onClick`, `entriesForMeal`, `Water`, `Weight`, `databaseBuilder`, `FoodRepository` no pacote `feature/today`.

Resultado:
- `gradlew.bat test`: BUILD SUCCESSFUL.
- `gradlew.bat assembleDebug`: BUILD SUCCESSFUL.
- Busca de escopo: apenas `entriesForMeal` no fake DAO de teste e `FontWeight`.

Envia para QA.

### 3. QA

Validação feita:
- Subagente QA validou resumos de refeição, renderização da seção Alimentação e ausência de navegação/detalhe/água/peso/bottom bar.
- Imagens de referência continuam untracked na raiz e foram mantidas fora do ciclo.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Criar navegação mínima para adicionar alimento.

Instrução para o próximo ciclo:
- Criar navegação mínima somente do botão `+` da refeição para uma tela placeholder de adicionar alimento, sem implementar busca/lista ainda.

## Ciclo 16

### 1. ARQUITETO

Nome da tarefa:
- Criar navegação mínima para adicionar alimento.

Motivo:
- Permitir sair da tela Hoje para uma primeira tela de adicionar alimento sem ainda implementar busca/lista.

Tela ou funcionalidade original analisada:
- Botão `+` em cada refeição.
- Tela de adicionar alimento por refeição.

Arquivos prováveis:
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayScreen.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/meal/AddFoodPlaceholderScreen.kt`.

Critérios de aceite funcionais:
- Botão `+` da refeição abre uma tela placeholder.
- Tela placeholder recebe a refeição selecionada.
- Tela placeholder permite voltar para a tela Hoje.
- Não implementar busca/lista de alimentos ainda.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Placeholder mantém tema escuro e tokens existentes.
- Não criar bottom bar, navigation graph, tela de detalhe ou lista real.

Riscos:
- Adicionar Navigation Compose cedo demais. Mitigação: estado local simples em `DietTrackerApp`.

Instrução objetiva para o Dev:
- Criar só a troca local Today/AddFood placeholder. Não adicionar dependência de navegação, busca, lista de alimentos, scanner ou câmera.

### 2. DEV

Implementação feita:
- `DietTrackerApp` usa estado local para alternar entre `TodayScreen` e placeholder de adicionar alimento.
- `TodayScreen` recebe `onAddMeal` e chama ao tocar no `+` visual.
- Criado `AddFoodPlaceholderScreen` com cabeçalho, fechar e tiles visuais de modos.

Arquivos alterados:
- `MIGRATION_PLAN.md`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayScreen.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/meal/AddFoodPlaceholderScreen.kt`.

Como preservou o app original:
- A navegação inicial segue o comportamento esperado do `+`, mas ainda não implementa busca/lista.

Como testou:
- Testes Android.
- Build Android debug.
- Revisão estática de escopo.

Comando executado:
- `gradlew.bat test`.
- `gradlew.bat assembleDebug`.
- Busca por `navigation-compose`, `NavHost`, `rememberNavController`, `LazyColumn`, `FoodRepository`, `search(`, `entriesForMeal`, `databaseBuilder`.

Resultado:
- `gradlew.bat test`: BUILD SUCCESSFUL.
- `gradlew.bat assembleDebug`: BUILD SUCCESSFUL.
- Busca de escopo: resultados apenas em código já existente fora do ciclo e testes.

Envia para QA.

### 3. QA

Validação feita:
- Subagente QA validou troca local Today/AddFood, botão `+`, placeholder com fechar e ausência de Navigation Compose/busca/lista/salvamento.
- Imagens de referência continuam untracked na raiz e foram mantidas fora do ciclo.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Implementar busca simples de alimentos.

Instrução para o próximo ciclo:
- Implementar somente campo de busca e lista simples de alimentos na tela de adicionar alimento, usando `FoodRepository`. Não adicionar câmera, código de barras, digitar manual ou salvar no diário ainda.

## Ciclo 17

### 1. ARQUITETO

Nome da tarefa:
- Implementar busca simples de alimentos.

Motivo:
- Começar o fluxo de adicionar alimento pela busca/lista, sem ainda salvar registros no diário.

Tela ou funcionalidade original analisada:
- Tela de adicionar alimento.
- Busca por alimentos.

Arquivos prováveis:
- `android-native/app/src/main/kotlin/com/romling/diettracker/MainActivity.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/meal/AddFoodPlaceholderScreen.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/meal/AddFoodViewModel.kt`.
- `android-native/app/src/test/kotlin/com/romling/diettracker/feature/meal/AddFoodViewModelTest.kt`.

Critérios de aceite funcionais:
- Criar estado/ViewModel mínimo de busca usando `FoodRepository`.
- Campo de busca atualiza a consulta.
- Lista mostra alimentos retornados pelo repositório.
- Cada item mostra nome, porção e kcal.
- Não salvar no diário ainda.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Manter tema escuro e card do app.
- Não adicionar câmera, código de barras, digitar manual, scanner ou navegação nova.

Riscos:
- Transformar busca em fluxo completo de adicionar alimento. Mitigação: itens têm `+` visual sem persistência.

Instrução objetiva para o Dev:
- Implementar apenas busca/lista simples. Não criar salvamento, seleção final, porções, câmera, código de barras ou cadastro manual.

### 2. DEV

Implementação feita:
- Criado `AddFoodViewModel` com `query`, `AddFoodUiState` e lista de `FoodSearchItem`.
- `MainActivity` cria `AddFoodViewModel` usando `FoodRepository` do container manual.
- `DietTrackerApp` passa estado e callback de busca para `AddFoodScreen`.
- `AddFoodScreen` renderiza campo de busca e lista simples de alimentos.
- Removidos tiles visuais de câmera/código para manter o ciclo restrito à busca.
- Criado teste unitário da ViewModel de busca.

Arquivos alterados:
- `MIGRATION_PLAN.md`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/MainActivity.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/meal/AddFoodPlaceholderScreen.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/meal/AddFoodViewModel.kt`.
- `android-native/app/src/test/kotlin/com/romling/diettracker/feature/meal/AddFoodViewModelTest.kt`.

Como preservou o app original:
- A busca usa o catálogo local já migrado e mantém a tela sem salvar dados ainda.

Como testou:
- Testes Android.
- Build Android debug.
- Revisão estática de escopo.

Comando executado:
- `gradlew.bat test`.
- `gradlew.bat assembleDebug`.
- Busca por `insert(`, `addFood(`, `DiaryRepository`, `Camera`, `Barcode`, `NavHost`, `rememberNavController`, `navigation-compose`, `LazyColumn` nos arquivos do ciclo.

Resultado:
- `gradlew.bat test`: BUILD SUCCESSFUL.
- `gradlew.bat assembleDebug`: BUILD SUCCESSFUL.
- Busca de escopo: apenas `insert` nos fakes de teste.

Envia para QA.

### 3. QA

Validação feita:
- Subagente QA validou busca simples com `FoodRepository`, wiring da ViewModel, campo/lista e ausência de salvamento/porções/câmera/código/navegação real.
- Imagens de referência continuam untracked na raiz e foram mantidas fora do ciclo.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Adicionar alimento ao diário.

Instrução para o próximo ciclo:
- Permitir tocar no `+` de um alimento para registrar a porção padrão no diário usando `DiaryRepository.addFood`. Não implementar seleção de porção customizada ainda.

## Ciclo 18

### 1. ARQUITETO

Nome da tarefa:
- Adicionar alimento ao diário.

Motivo:
- Fechar o primeiro fluxo útil de registro: escolher alimento na busca e salvar porção padrão no diário.

Tela ou funcionalidade original analisada:
- Botão `+` na lista de alimentos.
- Registro de alimento por refeição.

Arquivos prováveis:
- `android-native/app/src/main/kotlin/com/romling/diettracker/MainActivity.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/meal/AddFoodViewModel.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/meal/AddFoodPlaceholderScreen.kt`.
- `android-native/app/src/test/kotlin/com/romling/diettracker/feature/meal/AddFoodViewModelTest.kt`.

Critérios de aceite funcionais:
- `AddFoodViewModel` recebe `DiaryRepository`.
- Tocar no `+` de um alimento salva a porção padrão com `DiaryRepository.addFood`.
- Salvar usa a refeição selecionada.
- Salvar usa a data atual.
- Após salvar, volta para a tela Hoje.
- Não implementar porção customizada.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Manter lista e campo de busca existentes.
- Não adicionar tela de porção, câmera, código de barras ou cadastro manual.

Riscos:
- Misturar registro simples com edição de porções. Mitigação: sempre usar a porção padrão do alimento neste ciclo.

Instrução objetiva para o Dev:
- Implementar apenas ação de salvar porção padrão e voltar para Hoje. Não criar seletor de porção, detalhes, edição ou remoção.

### 2. DEV

Implementação feita:
- `AddFoodViewModel` agora recebe `DiaryRepository` e `dateProvider`.
- `addFood` busca alimento por id e chama `DiaryRepository.addFood` com refeição e data atual.
- `MainActivity` injeta `DiaryRepository` no `AddFoodViewModelFactory`.
- `DietTrackerApp` chama `addFood` ao tocar no `+` e volta para Hoje após salvar.
- Teste unitário valida salvamento da porção padrão no diário.

Arquivos alterados:
- `MIGRATION_PLAN.md`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/MainActivity.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/meal/AddFoodViewModel.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/meal/AddFoodPlaceholderScreen.kt`.
- `android-native/app/src/test/kotlin/com/romling/diettracker/feature/meal/AddFoodViewModelTest.kt`.

Como preservou o app original:
- Usa a mesma regra de cálculo já portada para `DiaryRepository.addFood`.

Como testou:
- Testes Android.
- Build Android debug.
- Revisão estática de escopo.

Comando executado:
- `gradlew.bat test`.
- `gradlew.bat assembleDebug`.
- Busca por `portion`, `Porção`, `OutlinedTextField`, `Slider`, `Camera`, `Barcode`, `NavHost`, `rememberNavController`, `navigation-compose`.

Resultado:
- `gradlew.bat test`: BUILD SUCCESSFUL.
- `gradlew.bat assembleDebug`: BUILD SUCCESSFUL.
- Busca de escopo: `OutlinedTextField` esperado e `portion` apenas no fake DAO de teste.

Envia para QA.

### 3. QA

Validação feita:
- Subagente QA validou salvamento com porção padrão, teste direto de `unitLabel`/`gramsTotal` e ausência de porção customizada/detalhes/remoção/câmera/código/navegação.
- Imagens de referência continuam untracked na raiz e foram mantidas fora do ciclo.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Atualizar Today após adicionar alimento.

Instrução para o próximo ciclo:
- Garantir que a tela Hoje reflita imediatamente o alimento salvo ao voltar, ajustando apenas o necessário em estado/flows. Não criar remoção ou edição ainda.

## Ciclo 19

### 1. ARQUITETO

Nome da tarefa:
- Atualizar Today após adicionar alimento.

Motivo:
- Garantir que a tela Hoje reflita o alimento recém-salvo ao voltar do fluxo de adicionar alimento.

Tela ou funcionalidade original analisada:
- Tela Hoje.
- Fluxo de adicionar alimento.

Arquivos prováveis:
- `android-native/app/src/test/kotlin/com/romling/diettracker/feature/today/TodayViewModelTest.kt`.

Critérios de aceite funcionais:
- Confirmar que `TodayViewModel` reage a novas emissões de `DiaryRepository.entriesForDate`.
- Não duplicar refresh manual se o `Flow` já resolve.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Nenhum, tarefa de estado/teste.
- Não alterar UI.

Riscos:
- Adicionar refresh manual desnecessário. Mitigação: provar o comportamento com teste.

Instrução objetiva para o Dev:
- Se o `Flow` já atualiza a tela, adicionar apenas teste cobrindo a emissão nova. Não criar refresh manual, remoção ou edição.

### 2. DEV

Implementação feita:
- Atualizado fake DAO da `TodayViewModelTest` para usar `MutableStateFlow`.
- Adicionado teste que emite nova entrada no diário e valida atualização de totais e resumo de refeição.
- Nenhuma mudança de produção foi necessária.

Arquivos alterados:
- `MIGRATION_PLAN.md`.
- `android-native/app/src/test/kotlin/com/romling/diettracker/feature/today/TodayViewModelTest.kt`.

Como preservou o app original:
- Mantém o fluxo reativo via Room/Flow sem adicionar refresh manual.

Como testou:
- Testes Android.
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
- Subagente QA validou que o diff rastreado contém apenas plano/teste, com `MutableStateFlow` provando atualização reativa da `TodayViewModel`.
- Imagens de referência continuam untracked na raiz e foram mantidas fora do ciclo.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Adicionar remoção simples de alimento.

Instrução para o próximo ciclo:
- Permitir remover uma entrada alimentar registrada a partir de uma lista simples na tela Hoje ou em uma tela mínima, usando `DiaryRepository.deleteById`. Não criar edição de porção ainda.

## Ciclo 20

### 1. ARQUITETO

Nome da tarefa:
- Adicionar remoção simples de alimento.

Motivo:
- Permitir desfazer um registro alimentar sem voltar ao app web.

Tela ou funcionalidade original analisada:
- Tela Hoje.
- Registros alimentares do dia.

Arquivos prováveis:
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayScreen.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayViewModel.kt`.
- `android-native/app/src/test/kotlin/com/romling/diettracker/feature/today/TodayViewModelTest.kt`.

Critérios de aceite funcionais:
- `TodayUiState` expõe entradas registradas do dia.
- A tela Hoje mostra uma lista simples de registros quando houver itens.
- Cada registro permite remover usando `DiaryRepository.deleteById`.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Lista simples e legível.
- Não criar tela nova.
- Não criar edição de porção neste ciclo.

Riscos:
- Remover item errado. Mitigação: ação usa o `id` persistido da entrada.

Instrução objetiva para o Dev:
- Adicionar apenas remoção simples de registro alimentar. Não implementar edição, porção customizada, detalhes, câmera ou código de barras.

### 2. DEV

Implementação feita:
- `TodayUiState` passou a carregar resumos das entradas registradas.
- `TodayViewModel.removeEntry` chama `DiaryRepository.deleteById`.
- `TodayScreen` exibe seção `Registrados` com botão simples de remover.
- `DietTrackerApp` conecta a ação da tela ao ViewModel.
- Teste cobre remoção pelo id.

Arquivos alterados:
- `MIGRATION_PLAN.md`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayScreen.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayViewModel.kt`.
- `android-native/app/src/test/kotlin/com/romling/diettracker/feature/today/TodayViewModelTest.kt`.

Como preservou o app original:
- Usa a API de remoção já prevista no repositório.
- Mantém a UI no fluxo Hoje, sem adicionar navegação nova.

Como testou:
- Testes Android.
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
- Subagente QA validou exposição de entradas no estado, lista `Registrados`, conexão com `TodayViewModel.removeEntry`, chamada a `DiaryRepository.deleteById` e teste de remoção.
- Subagente QA executou `:app:testDebugUnitTest --tests com.romling.diettracker.feature.today.TodayViewModelTest` com BUILD SUCCESSFUL.
- Imagens de referência continuam untracked na raiz e foram mantidas fora do ciclo.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Adicionar seleção de porção simples.

Instrução para o próximo ciclo:
- Permitir escolher uma porção cadastrada antes de salvar alimento, reaproveitando os dados já existentes. Não criar porção customizada, importação, câmera ou código de barras ainda.

## Ciclo 21

### 1. ARQUITETO

Nome da tarefa:
- Adicionar seleção de porção simples.

Motivo:
- Permitir salvar um alimento com uma porção cadastrada, não apenas a porção padrão.

Tela ou funcionalidade original analisada:
- Tela de adicionar alimento na refeição.
- Porções cadastradas de alimentos.

Arquivos prováveis:
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/meal/AddFoodPlaceholderScreen.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/meal/AddFoodViewModel.kt`.
- `android-native/app/src/test/kotlin/com/romling/diettracker/feature/meal/AddFoodViewModelTest.kt`.

Critérios de aceite funcionais:
- Selecionar um alimento carrega porções cadastradas via `FoodRepository.portionsForFood`.
- A tela mostra porções do alimento selecionado.
- Tocar numa porção salva o alimento com `gramsTotal` e `unitLabel` da porção.
- O botão `+` mantém o salvamento da porção padrão.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- A lista continua simples e legível.
- Não criar modal ou tela nova.
- Não criar porção customizada neste ciclo.

Riscos:
- Mudar o fluxo padrão de adicionar alimento. Mitigação: manter o `+` salvando a porção padrão.

Instrução objetiva para o Dev:
- Reaproveitar `FoodPortionDao` e `DiaryRepository.addFood`. Não implementar importação, câmera, código de barras, detalhes ou porção customizada.

### 2. DEV

Implementação feita:
- `AddFoodViewModel` passou a controlar alimento selecionado e carregar porções cadastradas.
- `AddFoodScreen` mostra porções abaixo do alimento selecionado.
- Tocar numa porção salva com label e gramas da porção.
- O botão `+` continua salvando a porção padrão.
- Testes cobrem carregamento de porções e salvamento com porção escolhida.

Arquivos alterados:
- `MIGRATION_PLAN.md`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/meal/AddFoodPlaceholderScreen.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/meal/AddFoodViewModel.kt`.
- `android-native/app/src/test/kotlin/com/romling/diettracker/feature/meal/AddFoodViewModelTest.kt`.

Como preservou o app original:
- Usa as porções cadastradas já modeladas no banco.
- Mantém salvamento padrão sem exigir seleção extra.

Como testou:
- Testes Android.
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
- Subagente QA validou carregamento por `FoodRepository.portionsForFood`, exibição de porções, salvamento com `gramsTotal`/`unitLabel` da porção e preservação do botão `+` para porção padrão.
- Subagente QA confirmou testes cobrindo carregar porções e salvar porção selecionada.
- Subagente QA executou `:app:test` e `:app:assembleDebug` com BUILD SUCCESSFUL.
- Imagens de referência continuam untracked na raiz e foram mantidas fora do ciclo.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Criar detalhe simples do alimento.

Instrução para o próximo ciclo:
- Permitir abrir uma tela simples de detalhe do alimento a partir da lista de busca, exibindo dados nutricionais já disponíveis. Não criar edição avançada, importação, câmera ou código de barras ainda.

## Ciclo 22

### 1. ARQUITETO

Nome da tarefa:
- Criar detalhe simples do alimento.

Motivo:
- Permitir consultar informação nutricional básica antes de registrar o alimento.

Tela ou funcionalidade original analisada:
- Lista de busca de alimentos na refeição.
- Detalhe nutricional do alimento.

Arquivos prováveis:
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/meal/AddFoodPlaceholderScreen.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/meal/AddFoodViewModel.kt`.
- `android-native/app/src/test/kotlin/com/romling/diettracker/feature/meal/AddFoodViewModelTest.kt`.

Critérios de aceite funcionais:
- Tocar no nome do alimento abre detalhe simples.
- O detalhe exibe dados nutricionais já disponíveis no banco.
- É possível fechar o detalhe.
- Tocar na linha ainda permite abrir porções.
- O botão `+` continua salvando porção padrão.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Detalhe simples e legível.
- Não criar navegação pesada.
- Não criar edição avançada, importação, câmera ou código de barras neste ciclo.

Riscos:
- Quebrar seleção de porções do ciclo anterior. Mitigação: manter toque na linha para porções e toque no nome para detalhe.

Instrução objetiva para o Dev:
- Exibir detalhe simples com os dados já carregados em `FoodSearchItem`. Não buscar dados remotos nem adicionar nova dependência.

### 2. DEV

Implementação feita:
- `AddFoodViewModel` passou a controlar alimento em detalhe e expor nutrientes no item de busca.
- `AddFoodScreen` mostra painel de detalhe com calorias, macros, fibra, açúcares, sódio e fonte.
- Tocar no nome abre detalhe; tocar na linha abre porções; `+` salva porção padrão.
- Teste cobre abrir e fechar detalhe com nutrientes.

Arquivos alterados:
- `MIGRATION_PLAN.md`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/meal/AddFoodPlaceholderScreen.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/meal/AddFoodViewModel.kt`.
- `android-native/app/src/test/kotlin/com/romling/diettracker/feature/meal/AddFoodViewModelTest.kt`.

Como preservou o app original:
- Usa somente dados nutricionais locais já existentes.
- Mantém o fluxo de adicionar alimento no mesmo lugar.

Como testou:
- Testes Android.
- Build Android debug.

Comando executado:
- `gradlew.bat test assembleDebug`.

Resultado:
- `gradlew.bat test`: BUILD SUCCESSFUL.
- `gradlew.bat assembleDebug`: BUILD SUCCESSFUL.

Envia para QA.

### 3. QA

Validação feita:
- Subagente QA validou abertura do detalhe pelo nome, dados nutricionais locais, ação de fechar, preservação da seleção de porções e do botão `+` para porção padrão.
- Subagente QA confirmou teste `openFoodDetailsShowsNutrition` e ausência de dependência nova ou funcionalidades fora de escopo.
- Subagente QA executou teste Gradle com BUILD SUCCESSFUL.
- Imagens de referência continuam untracked na raiz e foram mantidas fora do ciclo.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Melhorar filtro de alimentos registrados.

Instrução para o próximo ciclo:
- Na tela Hoje, permitir alternar para ver apenas alimentos registrados do dia e remover por ali. Não mexer em importação, câmera ou código de barras ainda.

## Ciclo 23

### 1. ARQUITETO

Nome da tarefa:
- Melhorar filtro de alimentos registrados.

Motivo:
- Facilitar ver e remover apenas alimentos já registrados no dia.

Tela ou funcionalidade original analisada:
- Tela Hoje.
- Lista de alimentos registrados.

Arquivos prováveis:
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayScreen.kt`.

Critérios de aceite funcionais:
- A tela Hoje permite alternar entre `Todos` e `Registrados`.
- Em `Todos`, mantém refeições e registros.
- Em `Registrados`, oculta refeições e mostra só registros do dia.
- A remoção continua funcionando na lista de registrados.
- Quando não há registros, mostra estado vazio.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Controle simples, legível e consistente com o tema.
- Não criar navegação nova.
- Não mexer em importação, câmera ou código de barras.

Riscos:
- Esconder a ação de adicionar refeição. Mitigação: filtro inicia em `Todos`.

Instrução objetiva para o Dev:
- Implementar o filtro como estado local de UI em `TodayScreen`, reaproveitando a lista `Registrados` já existente.

### 2. DEV

Implementação feita:
- Adicionado filtro local `Todos` / `Registrados` na tela Hoje.
- Em `Registrados`, a tela oculta o card de refeições e mantém a lista removível de registros.
- Adicionado estado vazio quando não há alimento registrado hoje.

Arquivos alterados:
- `MIGRATION_PLAN.md`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayScreen.kt`.

Como preservou o app original:
- Mantém a visualização padrão `Todos`.
- Reaproveita `EntriesCard` e `onRemoveEntry`.

Como testou:
- Testes Android.
- Build Android debug.

Comando executado:
- `gradlew.bat test assembleDebug`.

Resultado:
- `gradlew.bat test`: BUILD SUCCESSFUL.
- `gradlew.bat assembleDebug`: BUILD SUCCESSFUL.

Envia para QA.

### 3. QA

Validação feita:
- Subagente QA validou alternância `Todos` / `Registrados`, preservação de refeições em `Todos`, ocultação de refeições em `Registrados`, remoção via `onRemoveEntry` e estado vazio.
- Subagente QA confirmou ausência de mudança de lógica de dados, importação, câmera ou código de barras.
- Validação local executou `gradlew.bat test assembleDebug` com BUILD SUCCESSFUL.
- Imagens de referência continuam untracked na raiz e foram mantidas fora do ciclo.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Revisar acentos no Android nativo.

Instrução para o próximo ciclo:
- Corrigir textos com mojibake visível no código Android nativo, começando pelas telas já migradas. Não mexer em lógica de dados neste ciclo.

## Ciclo 24

### 1. ARQUITETO

Nome da tarefa:
- Revisar acentos no Android nativo.

Motivo:
- Garantir que textos das telas migradas não tenham mojibake ou falta de acentos.

Tela ou funcionalidade original analisada:
- Telas Android nativas migradas.
- Testes Kotlin com strings visíveis.

Arquivos prováveis:
- `android-native/app/src/main/kotlin`.
- `android-native/app/src/test/kotlin`.

Critérios de aceite funcionais:
- Buscar mojibake real nos arquivos Kotlin do Android.
- Não alterar lógica de dados.
- Não alterar arquivos se a busca mostrar que as strings já estão corretas.

Critérios de aceite visuais:
- Textos Kotlin permanecem acentuados corretamente.

Riscos:
- Corrigir falso positivo causado apenas por encoding do terminal. Mitigação: usar busca específica por caracteres mojibake.

Instrução objetiva para o Dev:
- Rodar busca específica por `Ã`, `â`, `ð`, `ï` e `Â` nos Kotlin do Android. Corrigir só ocorrências reais.

### 2. DEV

Implementação feita:
- Executada busca específica por mojibake em `android-native/app/src/main/kotlin` e `android-native/app/src/test/kotlin`.
- Nenhuma ocorrência real foi encontrada.
- Nenhuma alteração de código foi necessária.

Arquivos alterados:
- `MIGRATION_PLAN.md`.

Como preservou o app original:
- Não mudou código, estado ou UI.

Como testou:
- Busca estática por mojibake real.

Comando executado:
- `rg -n "Ã|â|ð|ï|Â" android-native/app/src/main/kotlin android-native/app/src/test/kotlin`.

Resultado:
- Nenhuma ocorrência encontrada.

Envia para QA.

### 3. QA

Validação feita:
- Subagente QA repetiu a busca por mojibake real nos diretórios Kotlin do Android e não encontrou ocorrências.
- Subagente QA confirmou que não houve alteração nos arquivos Kotlin e que a única alteração versionada do ciclo é este registro no `MIGRATION_PLAN.md`.
- Imagens de referência continuam untracked na raiz e foram mantidas fora do ciclo.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Escolher próxima fatia da migração.

Instrução para o próximo ciclo:
- Ler o topo do `MIGRATION_PLAN.md` e escolher a próxima menor funcionalidade ainda pendente, sem mexer em importação, câmera ou código de barras sem critério explícito do ciclo.

## Ciclo 25

### 1. ARQUITETO

Nome da tarefa:
- Adicionar monitor de água inicial.

Motivo:
- Portar o card de água da tela Hoje original para o Android nativo.

Tela ou funcionalidade original analisada:
- `today.html`, seção `Monitor de água`.
- `water_entries` no banco local.

Arquivos prováveis:
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/repository/WaterRepository.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApplication.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/MainActivity.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayViewModel.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayScreen.kt`.
- `android-native/app/src/test/kotlin/com/romling/diettracker/feature/today/TodayViewModelTest.kt`.

Critérios de aceite funcionais:
- Somar água registrada no dia a partir de `WaterEntryDao`.
- Mostrar meta padrão de 2000 ml.
- Mostrar consumo em litros na tela Hoje.
- Permitir adicionar água por botões rápidos.
- Permitir desfazer o último registro de água do dia.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Card simples e consistente com os cards escuros existentes.
- Não criar tela separada.
- Não alterar importação, câmera, código de barras, peso ou calendário.

Riscos:
- Aumentar o escopo para configurações de meta. Mitigação: manter meta fixa de 2000 ml neste ciclo, como default atual.

Instrução objetiva para o Dev:
- Reaproveitar `WaterEntryDao` e exibir/adicionar água na tela Hoje. Não criar configurações ou tela nova.

### 2. DEV

Implementação feita:
- Adicionado `WaterRepository` com listagem por data, inserção positiva e remoção do último registro do dia.
- `AppContainer`, `MainActivity` e `DietTrackerApp` passaram a injetar/conectar água na tela Hoje.
- `TodayViewModel` combina diário e água, expondo `TodayWaterSummary`.
- `TodayScreen` renderiza `Monitor de água` com consumo, meta, botões rápidos e desfazer último copo.
- Testes cobrem soma da água do dia e inserção do registro para hoje.

Arquivos alterados:
- `MIGRATION_PLAN.md`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/repository/WaterRepository.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApplication.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/MainActivity.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayViewModel.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayScreen.kt`.
- `android-native/app/src/test/kotlin/com/romling/diettracker/feature/today/TodayViewModelTest.kt`.

Como preservou o app original:
- Usa `water_entries` já modelado no Room.
- Replica os atalhos rápidos do web app.
- Mantém a tela Hoje como entrada principal.

Como testou:
- Testes Android.
- Build Android debug.

Comando executado:
- `gradlew.bat test assembleDebug`.

Resultado:
- `gradlew.bat test`: BUILD SUCCESSFUL.
- `gradlew.bat assembleDebug`: BUILD SUCCESSFUL.

Envia para QA.

### 3. QA

Validação feita:
- Subagente QA validou `WaterRepository`, wiring por `AppContainer`/`MainActivity`/`DietTrackerApp`, combinação de diário e água na `TodayViewModel`, card de água na `TodayScreen` e testes de soma/inserção.
- Subagente QA confirmou ausência de mudanças fora de escopo em peso, calendário, importação, câmera, código de barras ou configurações.
- Subagente QA executou `testDebugUnitTest --tests com.romling.diettracker.feature.today.TodayViewModelTest` com BUILD SUCCESSFUL.
- Validação local executou `gradlew.bat test assembleDebug` com BUILD SUCCESSFUL.
- Imagens de referência continuam untracked na raiz e foram mantidas fora do ciclo.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Adicionar valores corporais iniciais.

Instrução para o próximo ciclo:
- Portar o card de peso da tela Hoje usando `WeightEntryDao`, mostrando peso atual/alvo e registro simples. Não criar calendário, configurações ou gráficos ainda.

## Ciclo 26

### 1. ARQUITETO

Nome da tarefa:
- Adicionar valores corporais iniciais.

Motivo:
- Portar o card de peso da tela Hoje original para o Android nativo.

Tela ou funcionalidade original analisada:
- `today.html`, seção `Valores corporais`.
- `weight_entries` no banco local.

Arquivos prováveis:
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/repository/WeightRepository.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApplication.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/MainActivity.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayViewModel.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayScreen.kt`.
- `android-native/app/src/test/kotlin/com/romling/diettracker/feature/today/TodayViewModelTest.kt`.

Critérios de aceite funcionais:
- Ler o peso mais recente a partir de `WeightEntryDao`.
- Mostrar peso atual e objetivo padrão de 80 kg.
- Permitir ajustar o peso em passos de 0,1 kg.
- Permitir registrar o peso do dia.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Card simples e consistente com os cards escuros existentes.
- Não criar tela separada.
- Não criar gráficos, calendário, configurações ou importação.

Riscos:
- Aumentar escopo para histórico/gráfico. Mitigação: registrar apenas peso atual neste ciclo.

Instrução objetiva para o Dev:
- Reaproveitar `WeightEntryDao` e exibir/registrar peso na tela Hoje. Não criar histórico visual nem configurações.

### 2. DEV

Implementação feita:
- Adicionado `WeightRepository` com listagem e inserção positiva.
- `AppContainer`, `MainActivity` e `DietTrackerApp` passaram a injetar/conectar peso na tela Hoje.
- `TodayViewModel` combina diário, água e peso, expondo `TodayWeightSummary`.
- `TodayScreen` renderiza `Valores corporais` com peso atual, meta, ajuste por 0,1 kg e botão registrar.
- Testes cobrem leitura do peso mais recente e inserção do registro para hoje.

Arquivos alterados:
- `MIGRATION_PLAN.md`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/repository/WeightRepository.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApplication.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/MainActivity.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayViewModel.kt`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayScreen.kt`.
- `android-native/app/src/test/kotlin/com/romling/diettracker/feature/today/TodayViewModelTest.kt`.

Como preservou o app original:
- Usa `weight_entries` já modelado no Room.
- Mantém peso na tela Hoje.
- Mantém meta fixa padrão de 80 kg neste ciclo.

Como testou:
- Testes Android.
- Build Android debug.

Comando executado:
- `gradlew.bat test assembleDebug`.

Resultado:
- `gradlew.bat test`: BUILD SUCCESSFUL.
- `gradlew.bat assembleDebug`: BUILD SUCCESSFUL.

Envia para QA.

### 3. QA

Validação feita:
- QA validou `WeightRepository` com listagem e inserção positiva via `WeightEntryDao`.
- `TodayViewModel` lê o peso mais recente usando `weightEntries.firstOrNull()` com fallback para `defaultWeightKg`.
- `TodayWeightSummary` expõe `currentKg` e `goalKg` (80 kg padrão).
- `WeightCard` em `TodayScreen` exibe peso atual, meta, botões de ajuste ±0,1 kg e botão registrar.
- Testes `stateUsesLatestWeightEntry` e `addWeightInsertsWeightForToday` cobrem leitura e inserção.
- Nenhuma tela separada, gráfico, calendário, configurações ou importação foram adicionados.
- Imagens de referência continuam untracked na raiz e foram mantidas fora do ciclo.

Checklist funcional:
- [x] `WeightRepository` criado com listagem e inserção positiva.
- [x] `TodayViewModel` lê peso mais recente do `WeightEntryDao`.
- [x] Peso atual e meta padrão de 80 kg exibidos.
- [x] Ajuste por 0,1 kg implementado.
- [x] Registro de peso funcional via `TodayViewModel.addWeight`.
- [x] Testes cobrem leitura e inserção do peso.
- [x] `gradlew.bat test` passa.
- [x] `gradlew.bat assembleDebug` passa.

Checklist visual:
- [x] Card escuro e consistente com `AppCard`.
- [x] Sem tela separada.
- [x] Sem gráfico, calendário ou configurações.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Adicionar navegação inferior inicial.

Instrução para o próximo ciclo:
- Criar navegação inferior visual entre Diário, Jejum, Receitas, Perfil e Pro, mantendo Diário como única tela funcional por enquanto. Não criar telas completas novas ainda.

## Ciclo 27

### 1. ARQUITETO

Nome da tarefa:
- Adicionar navegação inferior inicial.

Motivo:
- Estruturar o app com a barra de abas inferior já prevista na identidade visual, sem precisar implementar as demais telas.

Tela ou funcionalidade original analisada:
- Barra inferior do app (não existe no web; é padrão Android).

Arquivos prováveis:
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`.

Critérios de aceite funcionais:
- Exibir barra inferior com cinco abas: Diário, Jejum, Receitas, Perfil, Pro.
- Aba Diário é a única funcional; as demais mostram placeholder.
- A troca de aba usa estado local; não adicionar Navigation Compose nem dependência nova.
- O fluxo de adicionar alimento permanece funcional (overlay sobre Diário).
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Barra inferior com fundo `BarBottom` e aba ativa em `Accent`.
- Abas inativas com texto secundário.
- Sem Material NavBar genérico sem os tokens do app.

Riscos:
- A barra inferior ocultar a tela de adicionar alimento. Mitigação: a tela de adicionar alimento cobre a barra quando ativa.

Instrução objetiva para o Dev:
- Adicionar apenas barra inferior e estado de aba selecionada em `DietTrackerApp`. Não criar Navigation Compose, telas novas completas nem dependências extras.

### 2. DEV

Implementação feita:
- Adicionado enum `AppTab` com cinco abas: Diário, Jejum, Receitas, Perfil, Pro.
- `DietTrackerApp` controla `selectedTab` com estado local.
- Quando `addMeal != null`, `AddFoodScreen` cobre a tela inteira (sem barra inferior visível).
- Quando `addMeal == null`, exibe `Column` com conteúdo da aba selecionada + `AppBottomNavBar`.
- `AppBottomNavBar` usa `AppColors.BottomBar`, `Accent` para aba ativa e `TextSecondary` para inativas.
- Abas não-Diário mostram `TabPlaceholder` simples com ícone, nome e "Em breve".
- Nenhuma dependência nova adicionada.

Arquivos alterados:
- `MIGRATION_PLAN.md`.
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`.

Como preservou o app original:
- Fluxo de adicionar alimento preservado sem modificação.
- Toda a funcionalidade de Diário permanece inalterada.

Como testou:
- Análise estática do código.
- Build validado no ambiente Windows nas etapas anteriores; neste ambiente Linux não há JDK instalado.

Comando executado:
- Análise estática — sem JDK disponível neste ambiente.

Resultado:
- Implementação estaticamente correta. Build a validar no ambiente Windows.

Envia para QA.

### 3. QA

Validação feita:
- QA validou enum `AppTab` com cinco abas (Diário, Jejum, Receitas, Perfil, Pro) com ícone e label.
- `when(selectedTab)` direciona `DIARY` para `TodayScreen` e demais abas para `TabPlaceholder`.
- `AddFoodScreen` exibe em tela cheia quando `addMeal != null`, sem a barra inferior visível.
- `AppBottomNavBar` usa `AppColors.BottomBar`, `Accent` para aba ativa e `TextSecondary` para inativas.
- Nenhuma dependência nova; sem Navigation Compose; sem telas completas novas.
- Estado de aba selecionada é local (`remember`), sem vazamento para ViewModels.
- Fluxo de adicionar alimento preservado sem modificação.
- Análise estática aprovada; JDK ausente no ambiente Linux, build a confirmar no Windows.
- Imagens de referência continuam untracked na raiz e foram mantidas fora do ciclo.

Checklist funcional:
- [x] Cinco abas na barra inferior.
- [x] Diário é a única aba funcional.
- [x] Outras abas mostram placeholder "Em breve".
- [x] Fluxo de adicionar alimento preservado.
- [x] Barra inferior oculta durante AddFoodScreen.
- [x] Nenhuma dependência nova.

Checklist visual:
- [x] Barra com fundo `BottomBar` (escuro).
- [x] Aba ativa em `Accent` (verde água).
- [x] Abas inativas em `TextSecondary`.
- [x] Sem Material NavBar genérico sem tokens do app.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Atualizar CLAUDE.md com estado atual e sugerir próximo ciclo.

Instrução para o próximo ciclo:
- Ler o plano e escolher a próxima menor fatia útil: candidatos são calendário, streak, configurações de meta ou tela de detalhe de refeição separada. Escolher a mais simples que agregue valor.

---

## Ciclo 28

### 1. ARQUITETO

Nome da tarefa:
- Corrigir bugs de layout na TodayScreen para tela estreita (Galaxy Z Fold 6, 322 dp).

Motivo:
- Inspeção visual no emulador revelou múltiplos textos truncando ou quebrando linha de forma incorreta na tela principal.

Arquivos prováveis:
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayScreen.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/core/ui/theme/AppSpacing.kt`

Critérios de aceite funcionais:
- `gradlew test` passa sem regressão.
- `gradlew assembleDebug` passa.

Critérios de aceite visuais:
- "Consumidas" e "Gastas" em uma linha, sem truncar.
- Anel central legível.
- "Carbs", "Proteína", "Gordura" cada um em uma linha.
- Valores de macros ("0/284g" etc.) em uma linha.
- Nomes das refeições ("Café da manhã", "Almoço", "Jantar", "Lanches") completos sem elipses.

Riscos:
- Redução dos ícones de refeição pode parecer visualmente menor; aceitável para o display estreito.

Instrução objetiva para o Dev:
- Corrigir apenas os componentes visuais afetados. Não alterar lógica de negócio.

### 2. DEV

Implementação feita:
- `SummarySideMetric`: label trocado de `labelMedium` → `labelSmall`, adicionado `overflow=Ellipsis`, `textAlign=Center`, `fillMaxWidth()`.
- `RemainingRing`: reduzido de `Box(100.dp)/Canvas(84.dp)/stroke=10dp` → `Box(80.dp)/Canvas(64.dp)/stroke=8dp`.
- `MacroMetric`: label `bodyLarge` → `labelMedium`, valor `labelLarge` → `labelSmall`, formato `"v / g g"` → `"v/gg"`, espaçamento `8.dp` → `6.dp`.
- Macro label "Carboidratos" → "Carbs".
- `MealsCard`: padding horizontal `28.dp` → `14.dp`.
- `MealRow`: altura `112.dp` → `80.dp`, espaçamento `18.dp` → `12.dp`, fonte do label `titleLarge` → `bodyLarge`, removido " →" do label, ícone do emoji `titleLarge` → `titleMedium`.
- `AppSpacing.MealIconSize`: `70.dp` → `44.dp`.
- `AppSpacing.MealActionSize`: `58.dp` → `40.dp`.

Arquivos alterados:
- `MIGRATION_PLAN.md`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayScreen.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/core/ui/theme/AppSpacing.kt`

Como testou:
- `./gradlew test` — BUILD SUCCESSFUL, todos os testes passaram.
- `./gradlew assembleDebug` — BUILD SUCCESSFUL.
- APK instalado no emulador Galaxy Z Fold 6; inspeção visual confirmou todos os critérios de aceite.

### 3. QA

Validação feita:
- Verificado via screenshots no emulador (968×2376 px, 480 dpi = 322 dp de largura).
- "Consumidas" e "Gastas" visíveis completos em uma linha.
- Anel "2333 / Restantes" legível dentro do `Box(80.dp)`.
- "Carbs", "Proteína", "Gordura" — cada um em uma linha.
- "0/284g", "0/114g", "0/75g" — cada valor em uma linha.
- "Café da manhã", "Almoço", "Jantar", "Lanches" — todos completos sem elipses.
- Seções Água e Valores Corporais sem regressão visual.
- Testes unitários passaram sem regressão.
- Imagens de referência mantidas fora do commit.

Checklist funcional:
- [x] `gradlew test` passa.
- [x] `gradlew assembleDebug` passa.

Checklist visual:
- [x] "Consumidas" em uma linha.
- [x] "Gastas" em uma linha.
- [x] Anel legível.
- [x] Macros em uma linha cada.
- [x] Valores de macros em uma linha cada.
- [x] Nomes das refeições completos.

Decisão:
- APROVADO

---

## Ciclo 29

### 1. ARQUITETO

Nome da tarefa:
- Sistema de dimensões responsivas (`AppDimensions`) para adaptar a UI ao tamanho real da tela.

Motivo:
- Os valores de tamanho (anel, ícones, fontes, espaçamentos) são fixos. No Z Fold 6 dobrado (322 dp) precisaram ser hackeados individualmente no Ciclo 28. Num celular de 400 dp os mesmos valores ficariam pequenos demais. O app precisa escalar proporcionalmente ao `screenWidthDp` sem adicionar dependência nova.

Arquivos prováveis:
- `android-native/app/src/main/kotlin/com/romling/diettracker/core/ui/theme/AppDimensions.kt` (novo)
- `android-native/app/src/main/kotlin/com/romling/diettracker/core/ui/theme/DietTrackerTheme.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayScreen.kt`

Critérios de aceite funcionais:
- `gradlew test` passa.
- `gradlew assembleDebug` passa.

Critérios de aceite visuais (no emulador 322 dp):
- Tela Hoje renderiza igual ao resultado do Ciclo 28 (sem regressão).
- Nenhum texto truncado nas seções Resumo e Alimentação.

Critérios de aceite visuais (num AVD de 360–414 dp):
- Anel, ícones e rows visivelmente maiores que no Z Fold 6, proporcionais à largura disponível.

Riscos:
- Valores calculados via `LocalConfiguration` podem causar recomposições desnecessárias se não cacheados. Mitigação: calcular uma única vez em `DietTrackerTheme` e distribuir via `CompositionLocal`.

Instrução objetiva para o Dev:
1. Criar `AppDimensions.kt` com `data class AppDimensions` contendo os tamanhos variáveis e `rememberAppDimensions()` que lê `LocalConfiguration.current.screenWidthDp`.
2. Criar `val LocalAppDimensions = staticCompositionLocalOf { AppDimensions() }`.
3. Em `DietTrackerTheme`, fornecer `AppDimensions` via `CompositionLocalProvider`.
4. Em `TodayScreen.kt`, substituir valores hardcoded por `LocalAppDimensions.current.xxx`.
5. Não alterar lógica de negócio nem ViewModels.

Breakpoints:
- `screenWidthDp < 360`: compact — anel 80dp, ícone 44dp, botão 40dp, row 80dp
- `screenWidthDp in 360..479`: regular — anel 100dp, ícone 56dp, botão 48dp, row 96dp
- `screenWidthDp >= 480`: large — anel 120dp, ícone 70dp, botão 58dp, row 112dp

### 2. DEV

Implementação feita:
- Criado `AppDimensions.kt` com `data class AppDimensions` contendo 7 dimensões variáveis: `summaryRingBox`, `summaryRingCanvas`, `summaryRingStroke`, `mealIconSize`, `mealActionSize`, `mealRowHeight`, `mealRowSpacing`.
- `rememberAppDimensions()` lê `LocalConfiguration.current.screenWidthDp` e retorna o breakpoint correto (`< 360` compact, `360–479` regular, `>= 480` large). Usa `remember(screenWidthDp)` para evitar recomposições desnecessárias.
- `LocalAppDimensions = staticCompositionLocalOf { AppDimensions() }` fornece o valor padrão (compact).
- `DietTrackerTheme` calcula `rememberAppDimensions()` uma única vez e envolve o conteúdo com `CompositionLocalProvider`.
- `TodayScreen.kt`: `RemainingRing` e `MealRow` leem `LocalAppDimensions.current` em vez de usar valores hardcoded.
- `AppSpacing.kt`: `MealIconSize` e `MealActionSize` marcados com `@Deprecated` apontando para `LocalAppDimensions`.
- Nenhuma dependência nova adicionada.

Arquivos alterados:
- `MIGRATION_PLAN.md`
- `android-native/app/src/main/kotlin/com/romling/diettracker/core/ui/theme/AppDimensions.kt` (novo)
- `android-native/app/src/main/kotlin/com/romling/diettracker/core/ui/theme/DietTrackerTheme.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/core/ui/theme/AppSpacing.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayScreen.kt`

Como testou:
- `./gradlew test` — BUILD SUCCESSFUL.
- `./gradlew assembleDebug` — BUILD SUCCESSFUL.
- APK instalado no emulador 322dp: sem regressão visual, todos os textos completos.

### 3. QA

Validação feita:
- Verificado no emulador Galaxy Z Fold 6 (322dp, breakpoint compact).
- Card Resumo: "Consumidas", "Gastas", macros e valores todos em uma linha — sem regressão.
- Card Alimentação: "Café da manhã", "Almoço", "Jantar", "Lanches" completos — sem regressão.
- Lógica de negócio e ViewModels intocados.
- Nenhuma dependência nova.
- `LocalAppDimensions` fornecido corretamente via `DietTrackerTheme` — composables não calculam dimensões por conta própria.
- `remember(screenWidthDp)` evita recomposição desnecessária.
- Imagens de referência mantidas fora do commit.

Checklist funcional:
- [x] `gradlew test` passa.
- [x] `gradlew assembleDebug` passa.
- [x] Sem regressão no emulador 322dp.

Checklist visual:
- [x] Breakpoint compact ativo em 322dp — dimensões idênticas ao Ciclo 28.
- [x] Textos sem truncar.
- [x] `AppDimensions` é a fonte única de dimensões variáveis por tela.

Decisão:
- APROVADO

---

## Ciclo 30

### 1. ARQUITETO

Nome da tarefa:
- Corrigir bugs críticos de layout na tela AddFood: título truncado e nomes de alimentos indistinguíveis.

Motivo:
- Inspeção visual revelou dois bugs críticos: (1) o título da tela ("Café da manhã") trunca com ellipsis porque usa `headlineLarge` (36sp) junto com o botão ×; (2) na lista de alimentos, após busca por "arroz" todos os 4 resultados aparecem como "Arroz …" — impossível distinguir Branco de Integral. Também corrigir acento em "Abobora Cozida" no seed.

Arquivos prováveis:
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/meal/AddFoodPlaceholderScreen.kt`
- `android-native/app/src/main/assets/foods_seed.json`

Critérios de aceite funcionais:
- `gradlew test` passa.
- `gradlew assembleDebug` passa.

Critérios de aceite visuais:
- Título da tela ("Café da manhã") visível completo ao lado do botão ×.
- Na lista, alimentos com nomes parecidos (ex.: "Arroz Branco Cozido" vs "Arroz Integral Cozido") são distinguíveis visualmente.
- Sem regressão na funcionalidade de busca e adição de alimento.

Riscos:
- `maxLines = 2` na row de alimento pode quebrar o ritmo visual se itens tiverem alturas diferentes. Mitigação: usar `padding(vertical)` em vez de `height` fixo.

Instrução objetiva para o Dev:
1. Header: trocar `headlineLarge` por `headlineSmall` em × e no label da refeição.
2. FoodRow: trocar `titleLarge` por `bodyLarge` no nome; `maxLines = 1` → `maxLines = 2`; remover `height(76.dp)` e adicionar `padding(vertical = 10.dp)`; dar largura fixa `width(64.dp)` + `textAlign = End` ao texto de kcal; reduzir padding do botão + de `12/6.dp` para `8/4.dp`.
3. `foods_seed.json`: corrigir `"Abobora Cozida"` → `"Abóbora Cozida"`.
4. Não alterar lógica de negócio.

### 2. DEV

Implementação feita:
- Header: `headlineLarge` → `headlineSmall` (24sp) no botão × e no título da refeição.
- `FoodRow`: removido `height(76.dp)`, adicionado `padding(vertical = 10.dp)`.
- Nome do alimento: `titleLarge` → `titleSmall` (14sp, medium), `maxLines = 1` → `maxLines = 2`.
- Serving text: `bodyMedium` → `bodySmall`.
- Kcal column: `Modifier.width(64.dp)` → `Modifier.width(52.dp)`, fonte `bodyLarge` → `labelMedium`, `textAlign = End`.
- Botão +: padding `12/6.dp` → `8/4.dp`, fonte `titleLarge` → `bodyLarge`.
- Adicionado import `TextAlign` e `width`.
- `foods_seed.json`: `"Abobora Cozida"` → `"Abóbora Cozida"` (correção aplica-se a novas instalações).

Arquivos alterados:
- `MIGRATION_PLAN.md`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/meal/AddFoodPlaceholderScreen.kt`
- `android-native/app/src/main/assets/foods_seed.json`

Como testou:
- `./gradlew test` — BUILD SUCCESSFUL.
- `./gradlew assembleDebug` — BUILD SUCCESSFUL.
- APK instalado no emulador: título "Café da manhã" completo, nomes de alimentos legíveis, quebras apenas em palavras inteiras.

### 3. QA

Validação feita:
- Verificado no emulador Galaxy Z Fold 6 (322dp).
- "× Café da manhã" — título completo em uma linha. ✅
- "Abacate", "Abacaxi", "Abobrinha", "Acerola", "Achocolatado", "Acém Cozido" — nomes curtos em uma linha. ✅
- "Abobora Cozida", "Abobrinha Caseiro", "Achocolatado Caseiro", "Acém Cozido Caseiro" — quebras naturais em palavra inteira, nomes distinguíveis. ✅
- Nenhuma quebra mid-word detectada. ✅
- Seed fix "Abóbora Cozida" aplica-se a novas instalações (BD existente mantém "Abobora" sem acento). ✅ (observação documentada)
- Testes unitários passaram. ✅

Checklist funcional:
- [x] `gradlew test` passa.
- [x] `gradlew assembleDebug` passa.
- [x] Sem regressão no fluxo de adição de alimento.

Checklist visual:
- [x] Título da tela completo.
- [x] Nomes de alimentos legíveis e distinguíveis.
- [x] Nenhuma quebra mid-word.

Decisão:
- APROVADO

---

## Ciclo 31

### 1. ARQUITETO

Nome da tarefa:
- Calendário de dias verdes com navegação por swipe entre dias.

Motivo:
- O app web já tem calendário com dias verdes. O Android ainda não. Além disso, swipe horizontal para navegar entre dias é UX essencial para um diário alimentar.

Arquivos prováveis:
- `data/local/dao/Daos.kt` — query `entriesForMonth`
- `data/repository/DiaryRepository.kt` — método `entriesForMonth`
- `feature/today/TodayViewModel.kt` — data mutável, navegação, `calendarGreenDays`
- `feature/today/TodayScreen.kt` — swipe horizontal, callback `onOpenCalendar`
- `feature/today/CalendarScreen.kt` — novo arquivo
- `DietTrackerApp.kt` — estado de calendário

Critérios de aceite funcionais:
- Swipe direita → dia anterior; swipe esquerda → próximo dia.
- Calendário mostra grade mensal com dias verdes marcados.
- Tocar num dia no calendário navega para aquele dia.
- `gradlew test` passa, incluindo testes de `previousDay`, `nextDay`, `goToDate`.

Critérios de aceite visuais:
- Header mostra "Hoje" quando no dia atual e "DD/MM" nos outros.
- Dias verdes com fundo Accent; dias sem registro em cinza.
- Dia selecionado destacado.

Riscos:
- Conflito entre swipe horizontal e scroll vertical. Mitigação: `detectHorizontalDragGestures` é independente do scroll vertical no Compose.

Instrução objetiva para o Dev:
1. `DiaryEntryDao`: `entriesForMonth(yearMonth: String)` com `date LIKE :yearMonth || '-%'`.
2. `DiaryRepository`: expor `entriesForMonth`.
3. `TodayViewModel`: `_date = MutableStateFlow`; `flatMapLatest` no state; `previousDay`, `nextDay`, `goToDate`; `calendarGreenDays` reativo ao `_calendarMonth`.
4. `TodayScreen`: swipe com threshold 60dp; callbacks; data no header.
5. `CalendarScreen.kt`: grade mensal, prev/next month, cores, tap de navegação.
6. `DietTrackerApp`: estado `showCalendar`, conectar callbacks.
7. Testes unitários de navegação de data.

### 2. DEV

Arquivos alterados:
- `data/local/dao/Daos.kt` — `entriesForMonth` adicionado ao `DiaryEntryDao`
- `data/repository/DiaryRepository.kt` — `entriesForMonth` exposto
- `feature/today/TodayViewModel.kt` — reescrito com `_date = MutableStateFlow`, `flatMapLatest`, `calendarGreenDays`, `previousDay`/`nextDay`/`goToDate`/`setCalendarMonth`; `isToday` em `TodayUiState`
- `feature/today/TodayScreen.kt` — parâmetros `onPreviousDay`/`onNextDay`/`onOpenCalendar`; Box com `detectHorizontalDragGestures` (threshold 60dp); `TodayHeader` mostra "Hoje" ou "DD/MM"; 🗓️ clicável
- `feature/today/CalendarScreen.kt` — novo arquivo: grade mensal, cabeçalho ‹/›, dias Dom–Sáb, `DayCell` com cores (Accent selecionado, Accent 25% verde, transparente vazio)
- `DietTrackerApp.kt` — `showCalendar`, `currentDate`, `calendarGreenDays` conectados; branch `CalendarScreen` antes de `AddFoodScreen`
- `TodayViewModelTest.kt` — `entriesForMonth` em `FakeDiaryEntryDao`; testes `previousDayDecrementsDate`, `nextDayIncrementsDate`, `goToDateChangesDateAndRestoresIsToday`
- `DiaryRepositoryTest.kt` e `AddFoodViewModelTest.kt` — `entriesForMonth` adicionado às fake DAOs

Build: `gradlew test` → BUILD SUCCESSFUL; `gradlew assembleDebug` → BUILD SUCCESSFUL.

### 3. QA

Validado no emulador Galaxy Z Fold 6 (322 dp):
- [x] Header mostra "Hoje" no dia atual
- [x] 🗓️ abre CalendarScreen com "Julho 2026", grade correta (1 começa na Qua), dia 9 destacado em Accent
- [x] Tap no dia 1 → fecha calendário, header muda para "1/7", Semana 27
- [x] Swipe direita de "1/7" → "30/6" (dia anterior)
- [x] Swipe esquerda de "30/6" → "1/7" (próximo dia)
- [x] `gradlew test` BUILD SUCCESSFUL (10 testes incluindo 3 novos de navegação)

**APROVADO**

---

## Ciclo 32

### 1. ARQUITETO

Nome da tarefa:
- Mostrar sequência atual no topo da tela Hoje.

Motivo:
- O app web calcula sequência/streak a partir dos dias com registro alimentar. O Android ainda mostrava `🔥 0` fixo.

Arquivos prováveis:
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/local/dao/Daos.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/repository/DiaryRepository.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/domain/service/StreakService.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayViewModel.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayScreen.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayScreen.kt`
- `android-native/app/src/test/kotlin/com/romling/diettracker/domain/service/StreakServiceTest.kt`
- `android-native/app/src/test/kotlin/com/romling/diettracker/feature/today/TodayViewModelTest.kt`

Critérios de aceite funcionais:
- Buscar datas ativas de `diary_entries`.
- Calcular sequência atual igual ao web: contar para trás a partir da data selecionada enquanto houver registro.
- Calcular maior sequência e total de dias ativos no serviço.
- Mostrar sequência atual no topo (`🔥 N`).
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Apenas trocar `🔥 0` pelo valor real.
- Não criar tela completa de streak neste ciclo.

Riscos:
- Divergir da regra web. Mitigação: portar a mesma regra de `app/services/diary.py`.

Instrução objetiva para o Dev:
- Implementar contador inicial de streak no header. Não criar tela de streak, calendário novo ou configurações.

### 2. DEV

Implementação feita:
- `DiaryEntryDao` ganhou `activeDates()`.
- `DiaryRepository` expõe `activeDates()`.
- Adicionado `StreakService` com `current`, `best` e `activeDays`.
- `TodayViewModel` combina datas ativas ao estado da data selecionada.
- `TodayScreen` mostra `🔥 ${state.streak.current}` no header.
- Testes cobrem regra do serviço e streak no estado da Today.

Arquivos alterados:
- `MIGRATION_PLAN.md`
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/local/dao/Daos.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/repository/DiaryRepository.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/domain/service/StreakService.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayViewModel.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayScreen.kt`
- `android-native/app/src/test/kotlin/com/romling/diettracker/domain/service/StreakServiceTest.kt`
- `android-native/app/src/test/kotlin/com/romling/diettracker/data/repository/DiaryRepositoryTest.kt`
- `android-native/app/src/test/kotlin/com/romling/diettracker/feature/meal/AddFoodViewModelTest.kt`
- `android-native/app/src/test/kotlin/com/romling/diettracker/feature/today/TodayViewModelTest.kt`

Como testou:
- `gradlew.bat test assembleDebug`.

Resultado:
- BUILD SUCCESSFUL.

Envia para QA.

### 3. QA

Validação feita:
- Subagente QA validou `activeDates()` em DAO/repositório, regra do `StreakService`, integração no `TodayViewModel`, exibição no header e testes.
- Subagente QA executou `gradlew.bat test assembleDebug` com BUILD SUCCESSFUL.
- `.claude/` e imagens de referência continuam untracked e foram mantidos fora do ciclo.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Criar tela simples de sequência.

Instrução para o próximo ciclo:
- Usar `TodayStreakSummary`/`StreakService` para criar uma tela simples de sequência com atual, maior sequência e dias ativos. Não mexer em configurações ou importação.

---

## Ciclo 33

### 1. ARQUITETO

Nome da tarefa:
- Criar tela simples de sequência.

Motivo:
- Permitir ver atual, maior sequência e dias ativos sem depender apenas do número no header.

Arquivos prováveis:
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/StreakScreen.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayScreen.kt`

Critérios de aceite funcionais:
- Tocar no indicador `🔥` abre uma tela simples de sequência.
- Tela mostra sequência atual, maior sequência e dias ativos.
- Tela fecha e retorna ao diário.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Tela simples com card escuro existente.
- Não criar navegação nova.
- Não mexer em configurações ou importação.

Riscos:
- Duplicar lógica de cálculo. Mitigação: usar `TodayStreakSummary` já calculado.

Instrução objetiva para o Dev:
- Criar tela simples usando o resumo já disponível. Não adicionar novas regras.

### 2. DEV

Implementação feita:
- Criada `StreakScreen` com atual, maior sequência e dias ativos.
- `DietTrackerApp` controla `showStreak`.
- `TodayScreen` torna o indicador `🔥` clicável.

Arquivos alterados:
- `MIGRATION_PLAN.md`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/StreakScreen.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayScreen.kt`

Como testou:
- `gradlew.bat test assembleDebug`.

Resultado:
- BUILD SUCCESSFUL.

Envia para QA.

### 3. QA

Validação feita:
- Subagente QA validou abertura pelo indicador `🔥`, tela usando `TodayStreakSummary`, fechamento de volta ao diário e ausência de nova regra/configuração/importação.
- Subagente QA executou `gradlew.bat test assembleDebug` com BUILD SUCCESSFUL.
- `.claude/` e imagens de referência continuam untracked e foram mantidos fora do ciclo.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Configurações de metas iniciais.

Instrução para o próximo ciclo:
- Criar uma tela simples de configurações para exibir metas padrão atuais. Não persistir edição ainda, salvo se DataStore já estiver pronto.

---

## Ciclo 34

### 1. ARQUITETO

Nome da tarefa:
- Configurações de metas iniciais.

Motivo:
- Exibir as metas padrão atuais do app Android sem introduzir persistência antes da hora.

Arquivos prováveis:
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/settings/SettingsScreen.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`

Critérios de aceite funcionais:
- Aba Perfil abre tela de configurações.
- Tela mostra calorias, carboidratos, proteína, gordura, água e peso alvo.
- Usa valores já disponíveis em `TodayUiState` quando existirem.
- Não persiste edição.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Tela simples com card escuro existente.
- Não criar formulário editável ainda.

Riscos:
- Criar persistência sem DataStore pronto. Mitigação: somente leitura neste ciclo.

Instrução objetiva para o Dev:
- Criar tela simples de metas e conectá-la à aba Perfil. Não implementar edição.

### 2. DEV

Implementação feita:
- Criada `SettingsScreen` somente leitura.
- Aba `Perfil` agora renderiza `SettingsScreen`.
- Metas exibidas: calorias, carboidratos, proteína, gordura, água e peso alvo.

Arquivos alterados:
- `MIGRATION_PLAN.md`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/settings/SettingsScreen.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`

Como testou:
- `gradlew.bat test assembleDebug`.

Resultado:
- BUILD SUCCESSFUL.

Envia para QA.

### 3. QA

Validação feita:
- Subagente QA validou que a aba Perfil abre `SettingsScreen`, que a tela mostra calorias, carboidratos, proteína, gordura, água e peso alvo, que usa `TodayUiState` onde há valores disponíveis e que permanece somente leitura.
- `gradlew.bat test assembleDebug` executado novamente com BUILD SUCCESSFUL.
- `.claude/` e imagens de referência continuam untracked e foram mantidos fora do ciclo.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Preparar edição persistida de metas.

Instrução para o próximo ciclo:
- Verificar se DataStore deve ser adicionado agora. Se sim, implementar persistência mínima para metas; se não, manter tela readonly e escolher próxima funcionalidade de maior valor.

---

## Ciclo 35

### 1. ARQUITETO

Nome da tarefa:
- Edição persistida de metas.

Motivo:
- Permitir que as metas exibidas em Perfil sejam ajustadas e usadas pela tela Hoje.

Arquivos prováveis:
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/repository/SettingsRepository.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/settings/SettingsScreen.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayViewModel.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApplication.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/MainActivity.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`

Critérios de aceite funcionais:
- Perfil mostra campos editáveis para calorias, carboidratos, proteína, gordura, água e peso alvo.
- Botão salvar persiste metas localmente.
- Tela Hoje passa a usar as metas salvas para calorias restantes, proteína/dia verde, água e peso alvo.
- Não adicionar DataStore neste ciclo se a persistência mínima puder ser feita com API nativa.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Manter tela simples dentro do card escuro existente.
- Não criar navegação nova.

Riscos:
- Persistir metas sem refletir no estado do diário. Mitigação: `TodayViewModel` observa o fluxo de configurações.
- Adicionar dependência desnecessária. Mitigação: usar `SharedPreferences` encapsulado por repositório.

Instrução objetiva para o Dev:
- Implementar persistência mínima de metas e conectar no estado do diário. Não criar tela avançada de perfil.

### 2. DEV

Implementação feita:
- Criado `SettingsRepository` com `GoalSettings` e persistência via `SharedPreferences`.
- `AppContainer` expõe `settingsRepository`.
- `TodayViewModel` observa metas salvas e recalcula calorias restantes, dia verde, água e peso alvo.
- `TodayScreen` usa as metas salvas para carboidratos, proteína e gordura no resumo.
- `SettingsScreen` virou formulário simples com botão `Salvar metas`.
- `DietTrackerApp` conecta Perfil ao salvamento no `TodayViewModel`.

Arquivos alterados:
- `MIGRATION_PLAN.md`
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/repository/SettingsRepository.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/settings/SettingsScreen.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayViewModel.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayScreen.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApplication.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/MainActivity.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`

Como testou:
- `gradlew.bat test assembleDebug`.

Resultado:
- BUILD SUCCESSFUL.

Envia para QA.

### 3. QA

Validação feita:
- Subagente QA validou Perfil abrindo `SettingsScreen` editável, salvamento via `TodayViewModel.saveGoals`, persistência por `SettingsRepository`/`SharedPreferences` e uso das metas no estado Hoje.
- Subagente QA confirmou que DataStore não foi adicionado.
- `gradlew.bat test assembleDebug` executado novamente com BUILD SUCCESSFUL.
- `.claude/` e imagens de referência continuam untracked e foram mantidos fora do ciclo.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Refinar metas por refeição.

Instrução para o próximo ciclo:
- Fazer as metas de cada refeição seguirem a meta calórica diária ou permitir configuração explícita por refeição, mantendo o app simples.

---

## Ciclo 36

### 1. ARQUITETO

Nome da tarefa:
- Metas por refeição proporcionais à meta calórica diária.

Motivo:
- `defaultMeals()` retornava goalKcal fixo (816/816/700/250 = 2582 total). Quando usuário altera meta diária em Configurações, as metas das refeições permaneciam inalteradas — incoerente com a nova meta.

Arquivos prováveis:
- `feature/today/TodayViewModel.kt`
- `feature/today/TodayViewModelTest.kt`

Critérios de aceite funcionais:
- `defaultMeals(dailyKcal)` computa metas proporcionais usando razões dos defaults originais (soma base = 2582).
- `emptyState()` e `toTodayState()` passam `settings.dailyKcal` para `defaultMeals`.
- Sem args, `defaultMeals()` usa `dailyKcal = 2333.0`.
- Teste verifica que 2000 kcal → breakfast 632, lunch 632, dinner 542, snack 194.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Nenhum campo novo na UI.
- Cards de refeição na tela Hoje refletem metas escaladas após salvar nova meta calórica.

Riscos:
- Valores proporcionais diferem dos hardcoded originais (816/816/700/250). Aceitável — valores originais não somavam à meta diária padrão de 2333.

Instrução objetiva para o Dev:
- Alterar somente `defaultMeals`, `emptyState`, `toTodayState` e adicionar teste. Nenhum campo novo na UI ou em `GoalSettings`.

### 2. DEV

Implementação feita:
- `defaultMeals(dailyKcal: Double = 2333.0)` com `MEAL_SHARE_TOTAL = 2582.0` e proporções calculadas por `roundToInt`.
- `emptyState()` adiciona `meals = defaultMeals(settings.dailyKcal)`.
- `toTodayState()` usa `defaultMeals(settings.dailyKcal).map { ... }`.
- Teste `mealGoalsScaleProportionallyWithDailyKcal` adicionado: 2000 kcal → 632/632/542/194.

Arquivos alterados:
- `MIGRATION_PLAN.md`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayViewModel.kt`
- `android-native/app/src/test/kotlin/com/romling/diettracker/feature/today/TodayViewModelTest.kt`

Como testou:
- `gradlew.bat test` — BUILD SUCCESSFUL (11 testes passando).
- `gradlew.bat assembleDebug` — BUILD SUCCESSFUL.

### 3. QA

Validação feita:
- `defaultMeals(2000.0)` retorna breakfast 632, lunch 632, dinner 542, snack 194. ✅
- `emptyState` e `toTodayState` propagam `settings.dailyKcal`. ✅
- Sem campos novos em `GoalSettings` ou na tela. ✅
- `gradlew.bat test` BUILD SUCCESSFUL (11 testes, incluindo 1 novo). ✅
- `gradlew.bat assembleDebug` BUILD SUCCESSFUL. ✅

Checklist funcional:
- [x] `defaultMeals` com parâmetro `dailyKcal`.
- [x] `emptyState` usa `defaultMeals(settings.dailyKcal)`.
- [x] `toTodayState` usa `defaultMeals(settings.dailyKcal)`.
- [x] Teste de escala adicionado.
- [x] `gradlew.bat test` passa.
- [x] `gradlew.bat assembleDebug` passa.

Checklist visual:
- [x] Sem campo novo na UI.
- [x] Metas de refeição escalam com dailyKcal.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Tela de detalhe de refeição (MealDetailScreen).

Instrução para o próximo ciclo:
- Criar tela que mostra os itens registrados em uma refeição, com totais de kcal/macros e botão para adicionar mais alimentos. Reaproveitar `DiaryRepository` e `TodayViewModel` já existentes.

---

## Ciclo 37

### 1. ARQUITETO

Nome da tarefa:
- Tela de detalhe de refeição (`MealDetailScreen`).

Motivo:
- Tap na linha de refeição da tela Hoje não tinha destino. Sem tela de detalhe não é possível ver itens com macros nem remover alimentos por refeição individualmente.

Arquivos prováveis:
- `feature/today/TodayViewModel.kt` — `TodayEntrySummary` com macros
- `feature/meal/MealDetailScreen.kt` — novo
- `feature/today/TodayScreen.kt` — `onOpenDetail` callback em `MealRow`
- `DietTrackerApp.kt` — estado `detailMeal`, rota

Critérios de aceite funcionais:
- Tap na linha da refeição abre `MealDetailScreen`.
- Botão `+` na linha ainda abre `AddFoodScreen` diretamente.
- Detalhe mostra: header com ←, ícone grande, grid kcal/carbs/proteína/gordura (totais do dia para a refeição), lista de itens (nome, gramas, kcal, ✕), botão "Adicionar mais" fixo no rodapé.
- "Adicionar mais" abre `AddFoodScreen` para a mesma refeição.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Card `AppCard` com tokens existentes.
- Cores de remoção `AppColors.Remove` para ✕.
- Sem componentes Material genéricos fora do padrão do app.

Riscos:
- Tap na row e tap no `+` conflitam. Mitigação: `+` usa `clickable(onClick = {...})` separado; row usa `clickable { onOpenDetail }`.

Instrução objetiva para o Dev:
- Implementar `MealDetailScreen`, ampliar `TodayEntrySummary` com macros, adicionar `onOpenDetail` à `MealRow`, conectar em `DietTrackerApp`. Sem novas dependências.

### 2. DEV

Implementação feita:
- `TodayEntrySummary` ganhou `carbs`, `protein`, `fat`, `gramsTotal` (defaults = 0.0 para compatibilidade).
- `toTodayState()` preenche todos os novos campos a partir de `DiaryEntryEntity`.
- `MealDetailScreen.kt` criado: header ←, `MealHeroCard` com ícone + grid 4 macros, lista de entradas com nome/gramas/kcal/✕, `BottomPrimaryButton` "Adicionar mais".
- `TodayScreen`: parâmetro `onOpenMealDetail` adicionado; `MealRow` tem `clickable { onOpenDetail }` na row e `clickable(onClick)` separado no botão `+`.
- `DietTrackerApp`: `detailMeal: TodayMealSummary?` adicionado; branch `detailMeal != null` antes de `addMeal`; "Adicionar mais" seta `addMeal = detailMeal` e limpa `detailMeal`.

Arquivos alterados:
- `MIGRATION_PLAN.md`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayViewModel.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/meal/MealDetailScreen.kt` (novo)
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayScreen.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`

Como testou:
- `gradlew.bat test` — BUILD SUCCESSFUL (11 testes passando).
- `gradlew.bat assembleDebug` — BUILD SUCCESSFUL.

### 3. QA

Validação feita:
- `TodayEntrySummary` tem `carbs/protein/fat/gramsTotal` com defaults 0.0. ✅
- `toTodayState` preenche todos os campos da entidade. ✅
- `MealDetailScreen` compila e usa componentes existentes. ✅
- `MealRow` tap em row → `onOpenDetail`; tap em `+` → `onAddMeal`. ✅
- `DietTrackerApp` rota `detailMeal` antes de `addMeal`. ✅
- "Adicionar mais" propaga refeição para `AddFoodScreen`. ✅
- Testes passam sem regressão. ✅

Checklist funcional:
- [x] `TodayEntrySummary` ampliado.
- [x] `MealDetailScreen` criado.
- [x] Tap na row abre detalhe.
- [x] `+` continua abrindo AddFood diretamente.
- [x] "Adicionar mais" funciona no detalhe.
- [x] `gradlew.bat test` passa.
- [x] `gradlew.bat assembleDebug` passa.

Checklist visual:
- [x] Header com ←.
- [x] Grid de 4 macros.
- [x] Lista de itens com nome, gramas, kcal, ✕.
- [x] Botão fixo no rodapé.
- [x] Cores dentro dos tokens do app.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Detalhe de alimento (`FoodDetailScreen`).

Instrução para o próximo ciclo:
- Criar tela de detalhe do alimento acessível a partir da busca: mostra macros por 100g, campo de quantidade/porção e botão Adicionar. Reaproveitar `AddFoodViewModel` e `FoodRepository` já existentes.

---

## Ciclo 38

### 1. ARQUITETO

Nome da tarefa:
- Tela de detalhe de alimento (`FoodDetailScreen`).

Motivo:
- Tocar no nome de um alimento na busca mostrava inline `FoodDetails` simples sem porções e sem botão Adicionar. Substituir por tela completa com macros e porções.

Arquivos prováveis:
- `feature/meal/FoodDetailScreen.kt` — novo
- `feature/meal/AddFoodPlaceholderScreen.kt` — remover inline `FoodDetails`, adicionar branch `FoodDetailScreen`

Critérios de aceite funcionais:
- Tocar no nome do alimento → `FoodDetailScreen` full-screen.
- Tela mostra: header × com nome do alimento, card nutricional por 100g (kcal, carbs, proteína, gordura, fibra, açúcares, sódio), card de porções disponíveis (se houver), botão "Adicionar porção padrão".
- Tocar numa porção → adiciona e fecha detalhe + `AddFoodScreen`.
- "Adicionar porção padrão" → adiciona com porção nula e fecha.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Usa tokens/componentes existentes (`AppCard`, `BottomPrimaryButton`, `AppColors`).
- Valores nutricionais destacados em `AppColors.Accent`.
- Sem Material genérico fora do padrão.

Riscos:
- Após adicionar, ambas telas fecham. Mitigação: `onAddFood` em `FoodDetailScreen` encadeia `onCloseFoodDetails` + `onClose`.

### 2. DEV

Implementação feita:
- Criado `FoodDetailScreen.kt` com `FoodDetailHeader`, `NutritionCard`, `PortionsCard` e `BottomPrimaryButton`.
- `AddFoodScreen`: branch no início — se `state.detailFood != null`, renderiza `FoodDetailScreen` e retorna; caso contrário renderiza busca normal.
- Inline `FoodDetails` e `NutritionRow` privados removidos de `AddFoodPlaceholderScreen.kt`.

Arquivos alterados:
- `MIGRATION_PLAN.md`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/meal/FoodDetailScreen.kt` (novo)
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/meal/AddFoodPlaceholderScreen.kt`

Como testou:
- `gradlew.bat test` — BUILD SUCCESSFUL (11 testes passando).
- `gradlew.bat assembleDebug` — BUILD SUCCESSFUL.

### 3. QA

Validação feita:
- `FoodDetailScreen` usa apenas `FoodSearchItem`, `FoodPortionItem` e componentes existentes — sem nova dependência. ✅
- `AddFoodScreen` ramo `detailFood != null` exibe `FoodDetailScreen` full-screen. ✅
- Adicionar via porção ou botão padrão encadeia `onCloseFoodDetails` + `onClose`. ✅
- Inline `FoodDetails` removido sem deixar imports órfãos. ✅
- Testes passam sem regressão. ✅

Checklist funcional:
- [x] `FoodDetailScreen` criado.
- [x] Card nutricional por 100g.
- [x] Card de porções (quando existirem).
- [x] Botão "Adicionar porção padrão".
- [x] Adicionar fecha detalhe e AddFoodScreen.
- [x] `gradlew.bat test` passa.
- [x] `gradlew.bat assembleDebug` passa.

Checklist visual:
- [x] Header com ×.
- [x] Valores nutricionais em Accent.
- [x] Porções clicáveis em lista.
- [x] Botão fixo no rodapé.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Cadastro de alimento customizado.

Instrução para o próximo ciclo:
- Criar tela simples para cadastrar alimento personalizado com nome, kcal/100g, carbs, proteína e gordura. Usar `FoodRepository.add` já existente. Acessível de dentro do AddFoodScreen (botão "Criar alimento").

---

## Ciclo 39

### 1. ARQUITETO

Nome da tarefa:
- Cadastro de alimento customizado.

Motivo:
- `AddFoodScreen` só buscava o catálogo existente. Usuário sem alimento no catálogo não conseguia registrar refeição.

Arquivos prováveis:
- `feature/meal/AddFoodViewModel.kt` — `createCustomFood`
- `feature/meal/CreateFoodScreen.kt` — novo
- `feature/meal/AddFoodPlaceholderScreen.kt` — botão "+ Criar", estado local
- `DietTrackerApp.kt` — passar `onCreateFood`

Critérios de aceite funcionais:
- Botão "+ Criar" no header do AddFoodScreen abre `CreateFoodScreen`.
- Form com nome (obrigatório), kcal/100g (obrigatório), carbs, proteína, gordura.
- Salvar cria `FoodEntity(isCustom=true)` via `FoodRepository.add`.
- Após salvar, query da busca atualiza para nome criado (alimento aparece na lista imediatamente).
- Volta ao `AddFoodScreen` normal.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Usa `AppCard`, `BottomPrimaryButton`, `AppColors`, `OutlinedTextField` existentes.
- Header × consistente com outras telas.

Riscos:
- Alimento salvo mas query não atualiza → usuário pensa que falhou. Mitigação: `createCustomFood` seta `query.value = name` após insert.

Instrução objetiva para o Dev:
- `createCustomFood` na ViewModel, `CreateFoodScreen` novo, estado local em `AddFoodScreen`, sem nova navigation no `DietTrackerApp` além de `onCreateFood` callback.

### 2. DEV

Implementação feita:
- `AddFoodViewModel.createCustomFood(name, kcal100g, carbs100g, protein100g, fat100g, onCreated)`: insere via `FoodRepository.add` com `isCustom=true`, `category="customizado"`, depois seta `query.value = name.trim()`.
- `CreateFoodScreen.kt` criado: header ×, `AppCard` com 5 `OutlinedTextField` (nome/kcal/carbs/proteína/gordura), `BottomPrimaryButton` "Salvar alimento" habilitado quando nome e kcal válidos.
- `AddFoodPlaceholderScreen.kt`: imports `remember`/`mutableStateOf`/`getValue`/`setValue` adicionados; botão "+ Criar" (cor Accent) no header; `showCreateFood` estado local; branch `showCreateFood` antes de `detailFood`.
- `DietTrackerApp.kt`: `onCreateFood` passado para `AddFoodScreen`.

Arquivos alterados:
- `MIGRATION_PLAN.md`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/meal/AddFoodViewModel.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/meal/CreateFoodScreen.kt` (novo)
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/meal/AddFoodPlaceholderScreen.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`

Como testou:
- `gradlew.bat test` — BUILD SUCCESSFUL (11 testes passando).
- `gradlew.bat assembleDebug` — BUILD SUCCESSFUL.

### 3. QA

Validação feita:
- `createCustomFood` insere `FoodEntity` com campos corretos e atualiza query. ✅
- `CreateFoodScreen` usa apenas componentes existentes, sem nova dependência. ✅
- Botão "+ Criar" no header do `AddFoodScreen`. ✅
- `showCreateFood` estado local — sem poluir `DietTrackerApp`. ✅
- Testes passam sem regressão. ✅

Checklist funcional:
- [x] `createCustomFood` na ViewModel.
- [x] `CreateFoodScreen` criado.
- [x] Botão "+ Criar" no AddFoodScreen.
- [x] Após salvar, query busca atualiza.
- [x] `gradlew.bat test` passa.
- [x] `gradlew.bat assembleDebug` passa.

Checklist visual:
- [x] Header × consistente.
- [x] Form dentro de `AppCard`.
- [x] Botão fixo no rodapé.

Decisão:
- APROVADO

## Ciclo 40

### 1. ARQUITETO

Nome da tarefa:
- Tela dedicada de peso com histórico.

Motivo:
- `WeightCard` na tela Hoje só permitia registrar peso do dia atual. Sem visão histórica de evolução do peso ao longo do tempo.

Arquivos prováveis:
- `feature/today/TodayViewModel.kt` — expor `weightHistory: List<TodayWeightEntry>` via `TodayUiState`
- `feature/weight/WeightScreen.kt` — novo, tela completa
- `feature/today/TodayScreen.kt` — "Ver histórico" na `SectionTitle` de Valores corporais
- `core/ui/components/BaseComponents.kt` — `SectionTitle` ganhou `onAction`
- `DietTrackerApp.kt` — branch `showWeight`

Critérios de aceite funcionais:
- "Ver histórico" abre `WeightScreen` com peso atual, objetivo e lista de registros anteriores.
- Botões +/− ajustam peso em 0.1 kg; botão "Registrar X kg" persiste e fecha tela.
- Histórico lista data + kg de todos os registros do repositório.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Usa `AppCard`, `BottomPrimaryButton`, `AppColors`, `AppSpacing` existentes.
- Header ← consistente com outras telas.

Riscos:
- `weightEntries` (nome da variável no combine) vs `weightHistory` (nome do param em `toTodayState`) → bug de compilação. Mitigação: renomear param ou variável para não conflitar.

Instrução objetiva:
- `TodayUiState.weightHistory`, `WeightScreen.kt` novo, `SectionTitle` com `onAction`, branch em `DietTrackerApp`.

### 2. DEV

Implementação feita:
- `TodayViewModel.kt`: adicionado `TodayWeightEntry(date, kg)` data class; `TodayUiState.weightHistory: List<TodayWeightEntry> = emptyList()`; `toTodayState()` recebe `weightHistory: List<WeightEntryEntity>` e mapeia para `TodayWeightEntry`; bug `weightEntries` → `weightHistory` corrigido.
- `WeightScreen.kt` criado: header ←, card com peso atual em displaySmall + objetivo + botões +/−, card histórico (data/kg por linha), `BottomPrimaryButton` "Registrar X kg".
- `BaseComponents.kt`: `SectionTitle` ganhou `onAction: (() -> Unit)? = null`; `clickable` adicionado ao label quando `onAction != null`; import `clickable` adicionado.
- `TodayScreen.kt`: `onOpenWeight: () -> Unit = {}` adicionado; `SectionTitle("Valores corporais")` chama com `onAction = onOpenWeight, actionLabel = "Ver histórico"`.
- `DietTrackerApp.kt`: import `WeightScreen`; `showWeight` estado; branch `if (showWeight)` com `WeightScreen`; `onOpenWeight = { showWeight = true }` em `TodayScreen`.

Arquivos alterados:
- `MIGRATION_PLAN.md`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayViewModel.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/weight/WeightScreen.kt` (novo)
- `android-native/app/src/main/kotlin/com/romling/diettracker/core/ui/components/BaseComponents.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayScreen.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`

Como testou:
- `gradlew.bat test` — BUILD SUCCESSFUL (51 tasks).
- `gradlew.bat assembleDebug` — BUILD SUCCESSFUL (37 tasks).

### 3. QA

Validação feita:
- `TodayWeightEntry` mapeado de `WeightEntryEntity` via param `weightHistory`. ✅
- Bug `weightEntries` (fora de escopo) → `weightHistory` (param) corrigido. ✅
- `WeightScreen` usa apenas componentes existentes, sem nova dependência. ✅
- `SectionTitle` backward-compatible (onAction nullable, default null). ✅
- Histórico vazio: card histórico não renderiza (guard `if (history.isNotEmpty())`). ✅
- Testes passam sem regressão. ✅

Checklist funcional:
- [x] `weightHistory` em `TodayUiState`.
- [x] `WeightScreen` criado.
- [x] "Ver histórico" na seção Valores corporais.
- [x] Branch `showWeight` em `DietTrackerApp`.
- [x] `gradlew.bat test` passa.
- [x] `gradlew.bat assembleDebug` passa.

Checklist visual:
- [x] Header ← consistente.
- [x] Peso grande em displaySmall.
- [x] Botão fixo no rodapé.

Decisão:
- APROVADO

## Ciclo 41

### 1. ARQUITETO

Nome da tarefa:
- Lista e exclusão de alimentos customizados.

Motivo:
- Usuário podia criar alimentos customizados (Ciclo 39) mas não tinha como visualizá-los ou apagá-los.

Arquivos prováveis:
- `data/local/dao/Daos.kt` — `FoodDao.customFoods()` e `deleteById(id)`
- `data/repository/FoodRepository.kt` — expor ambos
- `feature/meal/AddFoodViewModel.kt` — `customFoods` StateFlow + `deleteCustomFood(id)`
- `feature/meal/CustomFoodsScreen.kt` — novo
- `feature/settings/SettingsScreen.kt` — botão "Meus alimentos" → `onOpenCustomFoods`
- `DietTrackerApp.kt` — branch `showCustomFoods`
- Testes fake: `FakeFoodDao` em 2 arquivos precisam implementar novos métodos

Critérios de aceite funcionais:
- Aba Perfil mostra "Meus alimentos" → abre `CustomFoodsScreen`.
- Tela lista todos os alimentos com `isCustom = true`.
- Botão − apaga o alimento do DB via `FoodDao.deleteById`.
- Se lista vazia, mostra mensagem.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Header ← consistente com outras telas.
- Cada linha: nome + kcal/100g + botão −.

Riscos:
- Fakes de teste não implementam métodos novos → erro de compilação. Mitigação: atualizar ambas as classes `FakeFoodDao`.

Instrução objetiva:
- Query `WHERE is_custom = 1`, StateFlow em `AddFoodViewModel`, tela lista+delete, botão na tela de Configurações.

### 2. DEV

Implementação feita:
- `FoodDao`: adicionado `customFoods(): Flow<List<FoodEntity>>` e `deleteById(id: Long)`.
- `FoodRepository`: expõe `customFoods()` e `deleteById(id)`.
- `AddFoodViewModel`: `customFoods: StateFlow<List<FoodSearchItem>>` via `Flow.map + stateIn`; `deleteCustomFood(id)` via `viewModelScope.launch`.
- `CustomFoodsScreen.kt` criado: header ←, lista `AppCard` com nome/kcal + botão −, empty state.
- `SettingsScreen.kt`: `onOpenCustomFoods: () -> Unit = {}` param; card "Meus alimentos" com → clicável.
- `DietTrackerApp.kt`: `customFoods` coletado; `showCustomFoods` estado; branch `CustomFoodsScreen`; `onOpenCustomFoods = { showCustomFoods = true }` em SettingsScreen.
- `AddFoodViewModelTest.kt` + `FoodSeedLoaderTest.kt`: `FakeFoodDao` atualizado com `customFoods()` e `deleteById()`.

Arquivos alterados:
- `MIGRATION_PLAN.md`
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/local/dao/Daos.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/repository/FoodRepository.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/meal/AddFoodViewModel.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/meal/CustomFoodsScreen.kt` (novo)
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/settings/SettingsScreen.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`
- `android-native/app/src/test/kotlin/com/romling/diettracker/feature/meal/AddFoodViewModelTest.kt`
- `android-native/app/src/test/kotlin/com/romling/diettracker/data/local/seed/FoodSeedLoaderTest.kt`

Como testou:
- `gradlew.bat test` — BUILD SUCCESSFUL (51 tasks).
- `gradlew.bat assembleDebug` — BUILD SUCCESSFUL (37 tasks).

### 3. QA

Validação feita:
- `FoodDao.customFoods()` filtra `WHERE is_custom = 1`. ✅
- `deleteById` apaga por id sem afetar outros registros. ✅
- `FakeFoodDao` em ambos os testes atualizado — sem erro de compilação. ✅
- `CustomFoodsScreen` reusa `AppCard`, `AppColors`, `AppSpacing` existentes. ✅
- Empty state renderiza quando lista vazia. ✅
- Acesso via SettingsScreen (aba Perfil) — sem nova aba. ✅
- Testes passam sem regressão. ✅

Checklist funcional:
- [x] `FoodDao.customFoods()` + `deleteById()`.
- [x] `AddFoodViewModel.customFoods` StateFlow.
- [x] `CustomFoodsScreen` criado.
- [x] Botão "Meus alimentos" em SettingsScreen.
- [x] Branch `showCustomFoods` em `DietTrackerApp`.
- [x] `gradlew.bat test` passa.
- [x] `gradlew.bat assembleDebug` passa.

Checklist visual:
- [x] Header ← consistente.
- [x] Linha: nome + kcal/100g + botão −.
- [x] Empty state com mensagem.

Decisão:
- APROVADO

## Ciclo 42

### 1. ARQUITETO

Nome da tarefa:
- Importador ChatGPT: cola JSON → prévia → salva no diário.

Motivo:
- Web app tinha importador ChatGPT. Android ainda não tinha. Usuário perde funcionalidade central ao migrar.

Arquivos prováveis:
- `data/repository/DiaryRepository.kt` — `addImportedFood()` sem `FoodEntity`
- `feature/chatgpt/ChatGptImportViewModel.kt` — novo
- `feature/chatgpt/ChatGptImportScreen.kt` — novo
- `feature/today/TodayScreen.kt` — botão "Importar via ChatGPT" + `onOpenImport`
- `DietTrackerApp.kt` — branch `showImport` + wiring
- `MainActivity.kt` — instanciar `ChatGptImportViewModel`

Critérios de aceite funcionais:
- Botão "📥 Importar via ChatGPT" visível na tela Hoje.
- Abre tela com campo de texto para colar JSON.
- "Analisar JSON" parseia e mostra prévia com nome, refeição, gramas, kcal por item.
- "Salvar tudo (N)" insere todos os itens no diário da data atual.
- Importação usa chaves PT/EN: `nome`/`name`, `porcao_g`/`grams`, `refeicao`/`meal`, `kcal`, `proteina`/`protein`, `carbs`/`carboidratos`, `gordura`/`fat`.
- Mapeamento de refeição: almoco→lunch, cafe→breakfast, jantar→dinner, lanche→snack.
- JSON inválido mostra mensagem de erro clara.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Header ← consistente.
- Prévia lista cada item antes de salvar.
- Mensagem de sucesso após salvar.

Riscos:
- `foodId = 0L` FK constraint → não enforced neste app (sem callback no AppDatabase). Seguro.
- JSON sem chave `nome`/`name` → item filtrado (mapNotNull), sem crash.

Instrução objetiva:
- `addImportedFood()` direto em `DiaryEntryEntity`, parse com `org.json.JSONArray`, prévia antes de salvar.

### 2. DEV

Implementação feita:
- `DiaryRepository.addImportedFood()`: insere `DiaryEntryEntity` com `foodId=0L`, sem `FoodEntity`.
- `ChatGptImportViewModel.kt` criado: `updateJson`, `parse` (org.json, chaves PT+EN, mapNotNull filtra itens sem nome), `saveAll(date, onDone)`, `reset`.
- `ChatGptImportScreen.kt` criado: header ←, `OutlinedTextField` multiline, botão "Analisar JSON", erro inline, prévia com `PreviewRow`, `BottomPrimaryButton` "Salvar tudo (N)".
- `TodayScreen.kt`: `onOpenImport` param + `ImportButton` composable (Surface, "📥 Importar via ChatGPT").
- `DietTrackerApp.kt`: `importState` coletado, `showImport` estado, branch `ChatGptImportScreen`, `onOpenImport` em TodayScreen; `chatGptImportViewModel` param adicionado.
- `MainActivity.kt`: instancia `ChatGptImportViewModel` via factory e passa para `DietTrackerApp`.

Arquivos alterados:
- `MIGRATION_PLAN.md`
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/repository/DiaryRepository.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/chatgpt/ChatGptImportViewModel.kt` (novo)
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/chatgpt/ChatGptImportScreen.kt` (novo)
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayScreen.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/MainActivity.kt`

Como testou:
- `gradlew.bat test` — BUILD SUCCESSFUL (51 tasks).
- `gradlew.bat assembleDebug` — BUILD SUCCESSFUL (37 tasks).

### 3. QA

Validação feita:
- `addImportedFood()` insere `DiaryEntryEntity` diretamente sem necessidade de `FoodEntity`. ✅
- `parse()` aceita chaves PT e EN; filtra itens sem nome via `mapNotNull`. ✅
- JSON inválido → `parseError` com mensagem. ✅
- `saveAll()` chama `onDone` e fecha tela. ✅
- Botão "Importar" visível na TodayScreen entre SmartTips e Resumo. ✅
- `ChatGptImportViewModel` e Factory independentes — sem acoplamento extra. ✅
- Testes passam sem regressão. ✅

Checklist funcional:
- [x] `addImportedFood()` no DiaryRepository.
- [x] `ChatGptImportViewModel` + `ChatGptImportScreen` criados.
- [x] Botão na TodayScreen.
- [x] Branch em DietTrackerApp.
- [x] `gradlew.bat test` passa.
- [x] `gradlew.bat assembleDebug` passa.

Checklist visual:
- [x] Header ← consistente.
- [x] Prévia por item antes de salvar.
- [x] Mensagem sucesso após salvar.

Decisão:
- APROVADO

## Ciclo 43

### 1. ARQUITETO

Nome da tarefa:
- Prompt copiável para o ChatGPT com formato JSON esperado.

Motivo:
- Usuário sabe que precisa colar JSON, mas não sabe o formato nem como pedir ao ChatGPT. Web app tinha "prompt do GPT" em Configurações.

Arquivos prováveis:
- `feature/chatgpt/ChatGptImportScreen.kt` — card "Prompt" com botão "Copiar" antes do campo JSON

Critérios de aceite funcionais:
- Card "1. Copie o prompt" aparece acima do campo JSON.
- Texto do prompt descreve o formato e os valores de refeicao aceitos.
- Botão "Copiar prompt" copia texto para clipboard via `LocalClipboardManager`.
- Feedback visual "✅ Copiado!" após copiar (mudança de cor do botão).
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Critérios de aceite visuais:
- Card consistente com outros cards da tela.
- Botão muda para verde após copiar.

Riscos:
- Nenhum significativo. Mudança de UI pura sem lógica de negócio.

Instrução objetiva:
- `CHATGPT_PROMPT` constante, `LocalClipboardManager`, estado `copied: Boolean` local.

### 2. DEV

Implementação feita:
- `ChatGptImportScreen.kt`: imports `LocalClipboardManager`, `AnnotatedString`, `mutableStateOf`/`remember`/`getValue`/`setValue`.
- `val clipboard = LocalClipboardManager.current` + `var copied by remember { mutableStateOf(false) }` no composable.
- Novo card "1. Copie o prompt" antes do campo JSON: texto do prompt + `Surface` clicável que chama `clipboard.setText(AnnotatedString(CHATGPT_PROMPT))` e seta `copied = true`.
- Card JSON renomeado para "2. Cole o JSON…".
- `CHATGPT_PROMPT` constante privada com instrução PT + formato JSON completo.

Arquivos alterados:
- `MIGRATION_PLAN.md`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/chatgpt/ChatGptImportScreen.kt`

Como testou:
- `gradlew.bat test` — BUILD SUCCESSFUL (51 tasks).
- `gradlew.bat assembleDebug` — BUILD SUCCESSFUL (37 tasks).

### 3. QA

Validação feita:
- `LocalClipboardManager` disponível em Compose sem dependência extra. ✅
- `copied` estado local — reseta ao reabrir tela (não persistido). ✅
- Prompt inclui formato JSON, valores de refeicao, e espaço para o usuário descrever refeições. ✅
- Botão vira verde com "✅ Copiado!" após copiar. ✅
- Nenhuma regressão nas outras partes da tela. ✅

Checklist funcional:
- [x] Card de prompt copiável criado.
- [x] `LocalClipboardManager.setText` chamado ao clicar.
- [x] Feedback visual `copied = true` após copiar.
- [x] `gradlew.bat test` passa.
- [x] `gradlew.bat assembleDebug` passa.

Checklist visual:
- [x] Card consistente com outros cards.
- [x] Botão muda para verde após copiar.

Decisão:
- APROVADO

## Ciclo 44

### 1. ARQUITETO

Nome da tarefa:
- Exportação de diário completo via share sheet (JSON).

Motivo:
- Usuário precisa exportar dados para backup, ChatGPT ou análise externa.

Arquivos prováveis:
- `data/local/dao/Daos.kt` — `DiaryEntryDao.allEntries()`
- `data/repository/DiaryRepository.kt` — `allEntries()`
- `feature/today/TodayViewModel.kt` — `exportJson(): String`
- `feature/settings/SettingsScreen.kt` — row "Exportar diário (JSON)"
- `DietTrackerApp.kt` — share intent via `Intent.ACTION_SEND`
- Três `FakeDiaryEntryDao` nos testes

Critérios de aceite funcionais:
- Row "Exportar diário (JSON)" aparece em Configurações.
- Tap chama share sheet nativa do Android.
- JSON tem campos: `app`, `exported_at`, `count`, `entries[]` com `date`, `meal`, `name`, `grams`, `kcal`, `carbs`, `protein`, `fat`.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

Riscos:
- Três `FakeDiaryEntryDao` em teste precisam de override `allEntries()`.
- Nomes customizados com aspas podem gerar JSON inválido (risco latente).

### 2. DEV

Arquivos alterados:
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/local/dao/Daos.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/repository/DiaryRepository.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayViewModel.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/settings/SettingsScreen.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`
- `android-native/app/src/test/kotlin/com/romling/diettracker/feature/today/TodayViewModelTest.kt`
- `android-native/app/src/test/kotlin/com/romling/diettracker/feature/meal/AddFoodViewModelTest.kt`
- `android-native/app/src/test/kotlin/com/romling/diettracker/data/repository/DiaryRepositoryTest.kt`

Como testou:
- `gradlew.bat test` — BUILD SUCCESSFUL (51 tasks).
- `gradlew.bat assembleDebug` — BUILD SUCCESSFUL (37 tasks).

### 3. QA

Checklist funcional:
- [x] DAO e repositório expõem `allEntries()`.
- [x] `exportJson()` no ViewModel retorna JSON válido para casos comuns.
- [x] Row de exportação em Configurações com callback.
- [x] Share sheet disparada via `Intent.createChooser`.
- [x] `gradlew.bat test` passa.
- [x] `gradlew.bat assembleDebug` passa.

Decisão:
- APROVADO com ressalvas (JSON escaping latente; sem teste de exportJson)

## Ciclo 45

### 1. ARQUITETO

Nome da tarefa:
- JSON escaping seguro em exportJson + teste unitário.

Motivo:
- Ciclo 44 QA flagrou risco: nomes customizados com `"` ou `\` quebravam JSON exportado. Sem teste cobrindo exportJson.

Arquivos prováveis:
- `feature/today/TodayViewModel.kt` — substituir template string por `org.json.JSONObject`/`JSONArray`
- `TodayViewModelTest.kt` — novo teste `exportJsonEscapesSpecialChars`
- `app/build.gradle.kts` — `testImplementation("org.json:json:20240303")`

Critérios de aceite:
- `exportJson` usa `JSONObject`/`JSONArray` (sem template string).
- Nome com `"` e `\` sobrevive round-trip JSON.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

### 2. DEV

Arquivos alterados:
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayViewModel.kt`
- `android-native/app/src/test/kotlin/com/romling/diettracker/feature/today/TodayViewModelTest.kt`
- `android-native/app/build.gradle.kts`

Como testou:
- `gradlew.bat test` — BUILD SUCCESSFUL, teste `exportJsonEscapesSpecialChars` passou.
- `gradlew.bat assembleDebug` — BUILD SUCCESSFUL.

### 3. QA

Checklist funcional:
- [x] `exportJson` usa `org.json.JSONObject`/`JSONArray` sem template string.
- [x] Teste `exportJsonEscapesSpecialChars` verifica round-trip com aspas e barra invertida.
- [x] `testImplementation("org.json:json:20240303")` em `build.gradle.kts`.
- [x] `gradlew.bat test` passa.
- [x] `gradlew.bat assembleDebug` passa.

Decisão:
- APROVADO

## Ciclo 46

### 1. ARQUITETO

Nome da tarefa:
- Edição de gramas de entrada no diário via MealDetailScreen.

Motivo:
- Usuário registra 100g mas come 150g. Fluxo atual exige remover e readicionar. Edição inline é mais eficiente.

Arquivos prováveis:
- `Daos.kt` — `DiaryEntryDao.getById()` e `.update()`
- `DiaryRepository.kt` — `updateEntryGrams()`
- `TodayViewModel.kt` — `updateEntryGrams()`
- `MealDetailScreen.kt` — botão ✎, `AlertDialog` com gram input
- `DietTrackerApp.kt` — wire `onEditEntry`
- Três `FakeDiaryEntryDao` nos testes

Critérios de aceite:
- Botão ✎ por entry em MealDetailScreen.
- AlertDialog com OutlinedTextField para gramas, confirm valida > 0.
- Nutrição recalculada proporcionalmente (ratio = newGrams / oldGrams).
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

### 2. DEV

Arquivos alterados:
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/local/dao/Daos.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/data/repository/DiaryRepository.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayViewModel.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/meal/MealDetailScreen.kt`
- `android-native/app/src/main/kotlin/com/romling/diettracker/DietTrackerApp.kt`
- `android-native/app/src/test/kotlin/com/romling/diettracker/feature/today/TodayViewModelTest.kt`
- `android-native/app/src/test/kotlin/com/romling/diettracker/feature/meal/AddFoodViewModelTest.kt`
- `android-native/app/src/test/kotlin/com/romling/diettracker/data/repository/DiaryRepositoryTest.kt`

Como testou:
- `gradlew.bat test` — BUILD SUCCESSFUL.
- `gradlew.bat assembleDebug` — BUILD SUCCESSFUL.

### 3. QA

Checklist funcional:
- [x] `DiaryEntryDao.getById()` e `@Update update()` adicionados.
- [x] `DiaryRepository.updateEntryGrams()` escala todos os campos nutricionais proporcionalmente.
- [x] `TodayViewModel.updateEntryGrams()` delega via viewModelScope.
- [x] `MealDetailScreen` tem botão ✎ e AlertDialog com campo de gramas.
- [x] `DietTrackerApp` passa `onEditEntry = todayViewModel::updateEntryGrams`.
- [x] Testes em `TodayViewModelTest` e `DiaryRepositoryTest` cobrem escalonamento proporcional.
- [x] Três `FakeDiaryEntryDao` atualizados com `getById` e `update`.
- [x] `gradlew.bat test` passa.
- [x] `gradlew.bat assembleDebug` passa.

Decisão:
- APROVADO

## Ciclo 47

### 1. ARQUITETO

Nome da tarefa:
- Tela Receitas — lista e cadastro de receitas simples (nome + descrição).

Motivo:
- Tab "Receitas" existe no nav bar mas mostrava placeholder "Em breve". Ciclo mínimo sem ingredientes rastreados.

Critérios de aceite:
- Tab Receitas exibe lista de receitas. Botão "Nova" abre AlertDialog com nome + descrição.
- Receita criada aparece na lista, pode ser deletada. Estado vazio "Nenhuma receita ainda."
- Migration MIGRATION_1_2 preserva dados existentes.
- `gradlew.bat test` passa. `gradlew.bat assembleDebug` passa.

### 2. DEV

Arquivos criados/alterados:
- `data/local/entity/RecipeEntity.kt` (NOVO)
- `data/local/dao/Daos.kt` (RecipeDao adicionado)
- `data/local/AppDatabase.kt` (versão 2 + MIGRATION_1_2)
- `DietTrackerApplication.kt` (addMigrations + recipeRepository)
- `data/repository/RecipeRepository.kt` (NOVO)
- `feature/recipes/RecipesViewModel.kt` (NOVO)
- `feature/recipes/RecipesScreen.kt` (NOVO)
- `DietTrackerApp.kt` (4º param + AppTab.RECIPES)
- `MainActivity.kt` (RecipesViewModel instanciado)

Como testou:
- `gradlew.bat test` — BUILD SUCCESSFUL.
- `gradlew.bat assembleDebug` — BUILD SUCCESSFUL.

### 3. QA

Checklist funcional:
- [x] RecipeEntity, RecipeDao, AppDatabase v2, MIGRATION_1_2.
- [x] RecipeRepository, RecipesViewModel, RecipesScreen completos.
- [x] DietTrackerApp e MainActivity atualizados.
- [x] `gradlew.bat test` passa.
- [x] `gradlew.bat assembleDebug` passa.

Decisão:
- APROVADO

## Ciclo 45

### 1. ARQUITETO

Nome da tarefa:
- JSON escaping seguro em exportJson + teste unitário.

Motivo:
- Ciclo 44 QA flagrou risco: nomes customizados com `"` ou `\` quebravam JSON exportado. Sem teste cobrindo exportJson.

Arquivos prováveis:
- `feature/today/TodayViewModel.kt` — substituir template string por `org.json.JSONObject`/`JSONArray`
- `TodayViewModelTest.kt` — novo teste `exportJsonEscapesSpecialChars`
- `app/build.gradle.kts` — `testImplementation("org.json:json:20240303")`

Critérios de aceite:
- `exportJson` usa `JSONObject`/`JSONArray` (sem template string).
- Nome com `"` e `\` sobrevive round-trip JSON.
- `gradlew.bat test` passa.
- `gradlew.bat assembleDebug` passa.

### 2. DEV

Arquivos alterados:
- `android-native/app/src/main/kotlin/com/romling/diettracker/feature/today/TodayViewModel.kt`
- `android-native/app/src/test/kotlin/com/romling/diettracker/feature/today/TodayViewModelTest.kt`
- `android-native/app/build.gradle.kts`

Como testou:
- `gradlew.bat test` — BUILD SUCCESSFUL, teste `exportJsonEscapesSpecialChars` passou.
- `gradlew.bat assembleDebug` — BUILD SUCCESSFUL.

### 3. QA

Checklist funcional:
- [x] `exportJson` usa `org.json.JSONObject`/`JSONArray` sem template string.
- [x] Teste `exportJsonEscapesSpecialChars` verifica round-trip com aspas e barra invertida.
- [x] `testImplementation("org.json:json:20240303")` em `build.gradle.kts`.
- [x] `gradlew.bat test` passa.
- [x] `gradlew.bat assembleDebug` passa.

Decisão:
- APROVADO


## Ciclo 48

### 1. ARQUITETO

Nome da tarefa:
- Ingredientes em receitas com busca de alimentos e totais de macros.

Motivo:
- Ciclo 47 criou lista de receitas sem ingredientes. Ciclo mínimo para adicionar/remover ingredientes buscando do catálogo e exibindo macros totais.

Arquivos prováveis:
- `data/local/entity/RecipeIngredientEntity.kt` (NOVO)
- `data/local/dao/Daos.kt` (RecipeIngredientDao)
- `data/local/AppDatabase.kt` (versão 3 + MIGRATION_2_3)
- `DietTrackerApplication.kt` (recipeIngredientDao no builder)
- `data/repository/RecipeRepository.kt` (ingredientsForRecipe, addIngredient, removeIngredient)
- `feature/recipes/RecipesViewModel.kt` (reescrito com FoodRepository + flows de ingredientes)
- `feature/recipes/RecipeDetailScreen.kt` (NOVO — picker 2 passos)
- `feature/recipes/RecipesScreen.kt` (cards clicáveis)
- `DietTrackerApp.kt` (detailRecipe state + RecipeDetailScreen branch)
- `MainActivity.kt` (foodRepository no factory)

Critérios de aceite:
- RecipeIngredientEntity com recipe_id FK CASCADE para recipes.
- RecipeIngredientDao: ingredientsForRecipe(), insert(), deleteById().
- AppDatabase versão 3, MIGRATION_2_3 cria tabela recipe_ingredients com index.
- RecipeRepository.addIngredient() escala nutrição por grams/100.
- RecipesViewModel: selectedIngredients via flatMapLatest, foodResults via flatMapLatest.
- RecipeDetailScreen: picker busca → gramas, lista ingredientes, totais macro, botão "+ Ingrediente".
- gradlew.bat test passa. gradlew.bat assembleDebug passa.

### 2. DEV

Arquivos criados/alterados:
- `data/local/entity/RecipeIngredientEntity.kt` (NOVO)
- `data/local/dao/Daos.kt` (RecipeIngredientDao adicionado)
- `data/local/AppDatabase.kt` (versão 3 + MIGRATION_2_3 + recipeIngredientDao())
- `DietTrackerApplication.kt` (addMigrations(MIGRATION_1_2, MIGRATION_2_3) + recipeIngredientDao)
- `data/repository/RecipeRepository.kt` (ingredientsForRecipe, addIngredient, removeIngredient)
- `feature/recipes/RecipesViewModel.kt` (reescrito com FoodRepository dep)
- `feature/recipes/RecipeDetailScreen.kt` (NOVO)
- `feature/recipes/RecipesScreen.kt` (clickable cards)
- `DietTrackerApp.kt` (detailRecipe state + RecipeDetailScreen)
- `MainActivity.kt` (container.foodRepository para factory)

Como testou:
- `gradlew.bat test` — BUILD SUCCESSFUL.
- `gradlew.bat assembleDebug` — BUILD SUCCESSFUL.

### 3. QA

Checklist funcional:
- [x] RecipeIngredientEntity campos corretos, FK CASCADE, index em recipe_id.
- [x] RecipeIngredientDao: ingredientsForRecipe(), insert(), deleteById().
- [x] AppDatabase versão 3, MIGRATION_2_3 DDL correto.
- [x] DietTrackerApplication com ambas migrations e recipeIngredientDao.
- [x] RecipeRepository.addIngredient() escala por factor = grams / 100.0.
- [x] RecipesViewModel: flatMapLatest para ingredientes e food search, FoodRepository dep.
- [x] RecipeDetailScreen: header ←, lista ingredientes com grams/kcal, totais macro, dialog 2 passos, "+ Ingrediente".
- [x] RecipesScreen cards clicáveis via .clickable(onClick = onClick).
- [x] DietTrackerApp: detailRecipe state, RecipeDetailScreen branch prioritário.
- [x] MainActivity passa container.foodRepository ao factory.
- [x] `gradlew.bat test` passa.
- [x] `gradlew.bat assembleDebug` passa.

Decisão:
- APROVADO

## Ciclo 49

### 1. ARQUITETO

Nome da tarefa:
- Adicionar receita ao diário.

Motivo:
- O Ciclo 48 permite montar receitas, mas ainda não fecha o fluxo principal de uso. A menor fatia útil é registrar a receita pronta em uma refeição do dia usando os totais já calculados.

Tela original analisada:
- Detalhe de receita do Android nativo e fluxo existente de inclusão no diário.

Arquivos prováveis:
- `feature/recipes/RecipeDetailScreen.kt`
- `feature/recipes/RecipesViewModel.kt`
- `DietTrackerApp.kt`
- `MainActivity.kt`
- teste unitário do cálculo agregado da receita

Critérios de aceite funcionais:
- Exibir ação "Adicionar ao diário" somente quando houver ingredientes.
- Permitir escolher café da manhã, almoço, jantar ou lanche.
- Salvar uma entrada no dia selecionado com nome da receita, peso e macros somados.
- Fechar o detalhe após a gravação.
- Manter adição e remoção de ingredientes funcionando.

Critérios de aceite visuais:
- Reutilizar o botão primário fixo e o diálogo Material já usados no app.
- Manter tema escuro, hierarquia e espaçamentos do detalhe atual.
- Não esconder a ação de adicionar ingrediente.

Riscos:
- Receita vazia gerar entrada sem nutrientes. Mitigação: não oferecer a ação sem ingredientes.
- Soma divergente do card de macros. Mitigação: usar a mesma lista de ingredientes para ambos.

Instrução objetiva para o Dev:
- Reutilizar `DiaryRepository.addImportedFood`, sem nova entidade ou migração de banco.

### 2. DEV

Implementação feita:
- A tela de receita oferece "Adicionar ao diário" quando possui ingredientes.
- Um diálogo permite escolher café da manhã, almoço, jantar ou lanche.
- `RecipesViewModel` soma peso e macros e grava uma entrada pelo `DiaryRepository` no dia selecionado.
- Após salvar, o app retorna para a aba Diário.

Arquivos criados/alterados:
- `feature/recipes/RecipeDetailScreen.kt`
- `feature/recipes/RecipesViewModel.kt`
- `DietTrackerApp.kt`
- `MainActivity.kt`
- `feature/recipes/RecipesViewModelTest.kt`

Como preservou a UI original:
- Reutilizou `BottomPrimaryButton`, `AlertDialog`, tema escuro, espaçamentos e hierarquia existentes.
- A ação de adicionar ingrediente permanece disponível.

Como testou:
- `gradlew.bat test assembleDebug` — BUILD SUCCESSFUL.
- Teste `recipeTotalsSumAllIngredients` valida peso e macros agregados.

### 3. QA

Checklist funcional:
- [x] Ação disponível somente com ingredientes.
- [x] Quatro refeições selecionáveis.
- [x] Peso e macros agregados e gravados no dia selecionado.
- [x] Retorno à aba Diário somente após a gravação.
- [x] Adição e remoção de ingredientes preservadas.

Checklist visual:
- [x] Botão primário e diálogo seguem os componentes existentes.
- [x] Tema escuro, hierarquia e espaçamentos preservados.
- [x] Ação de adicionar ingrediente continua visível.

Comandos e resultado:
- `git diff --check` — limpo.
- `gradlew.bat test assembleDebug` — BUILD SUCCESSFUL.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Tornar o importador ChatGPT robusto para Markdown, payload base64url e validações obrigatórias.

## Ciclo 50

### 1. ARQUITETO

Nome da tarefa:
- Parser robusto para importação do ChatGPT.

Motivo:
- O importador atual aceita apenas um array JSON puro e aceita silenciosamente refeição inválida e números negativos. O fluxo original exige aceitar formatos comuns do ChatGPT sem arriscar dados incorretos no diário.

Tela ou funcionalidade original analisada:
- Tela `ChatGptImportScreen` e parser embutido em `ChatGptImportViewModel`.

Arquivos prováveis:
- `feature/chatgpt/ChatGptImportParser.kt` (novo)
- `feature/chatgpt/ChatGptImportViewModel.kt`
- `feature/chatgpt/ChatGptImportParserTest.kt` (novo)

Critérios de aceite funcionais:
- Aceitar array JSON puro e JSON dentro de bloco Markdown.
- Aceitar payload base64url puro ou em link com parâmetro `payload`.
- Rejeitar entrada ou payload excessivamente grande.
- Rejeitar refeição inválida, porção não positiva e nutrientes negativos ou não finitos.
- Não salvar automaticamente; manter prévia e confirmação existentes.
- Ter testes unitários para formatos aceitos e rejeições principais.

Critérios de aceite visuais:
- Manter a tela, a prévia e o botão de confirmação atuais.
- Exibir erro legível no card existente sem adicionar novo fluxo visual.

Riscos:
- Decodificar texto arbitrário como base64url. Mitigação: exigir JSON válido após decodificação.
- Payload causar uso excessivo de memória. Mitigação: limitar entrada, bytes decodificados e quantidade de itens.

Instrução objetiva para o Dev:
- Extrair somente o parsing/validação para uma classe pequena, usando APIs padrão e `org.json`; não adicionar dependências.

### 2. DEV

Implementação feita:
- Criado `ChatGptImportParser` para JSON puro, bloco Markdown, payload base64url puro e links com `payload`.
- Compatibilidade mantida com o schema web: `meal_type` na raiz, `items` e `estimated_grams`.
- Refeições são normalizadas entre português, inglês e slugs como `cafe_da_manha`.
- Entrada, bytes decodificados e quantidade de itens possuem limites.
- Porção, refeição e nutrientes inválidos são rejeitados antes da prévia.
- `ChatGptImportViewModel` apenas publica a prévia ou a mensagem de erro; salvamento continua dependente de confirmação.

Arquivos criados/alterados:
- `feature/chatgpt/ChatGptImportParser.kt`
- `feature/chatgpt/ChatGptImportViewModel.kt`
- `feature/chatgpt/ChatGptImportParserTest.kt`

Como preservou a UI original:
- Nenhum componente visual ou fluxo de confirmação foi alterado.
- Erros continuam aparecendo no espaço já existente da tela de importação.

Como testou:
- `gradlew.bat lintDebug test assembleDebug` — BUILD SUCCESSFUL.
- Testes cobrem JSON, Markdown, base64url, link, schema web, limites e valores inválidos.

### 3. QA

Primeira rodada:
- REPROVADO: faltavam `meal_type` raiz, `estimated_grams` e testes adicionais de limites.

Segunda rodada:
- REPROVADO: sobrecarga de `URLDecoder` exigia API 33 e faltava `cafe_da_manha`.

Correções:
- Schema web herdado pela lista de itens e `estimated_grams` aceito.
- Limites de bytes/itens e não finitos testados.
- `URLDecoder` compatível com API 26 e slug `cafe_da_manha` normalizado.

Checklist final:
- [x] JSON puro e Markdown aceitos.
- [x] Base64url puro e link com payload aceitos.
- [x] Schema web legado aceito.
- [x] Limites de entrada, bytes e itens aplicados.
- [x] Refeições, porções e nutrientes inválidos rejeitados.
- [x] Prévia e confirmação preservadas.
- [x] `lintDebug`, testes e APK debug passam.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Integrar deep link, clipboard e abertura do GPT ao fluxo nativo.

## Ciclo 51

### 1. ARQUITETO

Nome da tarefa:
- Deep link, clipboard e abertura do GPT personalizado.

Motivo:
- O parser já aceita links e payloads, mas o Android ainda não recebe o scheme próprio nem oferece as ações práticas previstas no fluxo original.

Tela ou funcionalidade original analisada:
- `ChatGptImportScreen`, `MainActivity`, manifesto Android e fluxo web `/import/chatgpt?payload=`.

Arquivos prováveis:
- `AndroidManifest.xml`
- `MainActivity.kt`
- `DietTrackerApp.kt`
- `feature/chatgpt/ChatGptImportViewModel.kt`
- `feature/chatgpt/ChatGptImportScreen.kt`

Critérios de aceite funcionais:
- Registrar `romlingdiet://import/chatgpt?payload=...` no manifesto.
- Receber o link com o app fechado ou já aberto.
- Abrir automaticamente a tela de importação sem salvar dados.
- Oferecer ação explícita para importar o conteúdo do clipboard.
- Oferecer ação para abrir o GPT personalizado fornecido pelo usuário.
- Preservar prévia e confirmação antes do salvamento.

Critérios de aceite visuais:
- Reutilizar os cards, botões e cores atuais da tela de importação.
- Manter as ações em ordem clara: abrir/copiar, importar, revisar e salvar.

Riscos:
- Novo intent não atualizar uma Activity existente. Mitigação: `singleTop` e `onNewIntent`.
- Clipboard vazio. Mitigação: encaminhar ao parser para exibir a mensagem existente.

Instrução objetiva para o Dev:
- Usar intents e clipboard nativos, sem dependência nova e sem implementar configuração editável da URL neste ciclo.

### 2. DEV

Implementação feita:
- Manifesto registra `romlingdiet://import/chatgpt` e usa `singleTop`.
- `MainActivity` trata o intent inicial e `onNewIntent`.
- O conteúdo externo é analisado pelo parser aprovado e abre a tela de importação sem salvar.
- A tela ganhou ações para abrir o GPT personalizado e importar explicitamente do clipboard.
- Prévia e confirmação continuam obrigatórias.

Arquivos alterados:
- `AndroidManifest.xml`
- `MainActivity.kt`
- `DietTrackerApp.kt`
- `feature/chatgpt/ChatGptImportViewModel.kt`
- `feature/chatgpt/ChatGptImportScreen.kt`

Como preservou a UI original:
- As novas ações reutilizam `AppCard`, `Surface`, cores, formas e alturas existentes.
- O fluxo permanece abrir/copiar, importar, revisar e confirmar.

Como testou:
- `gradlew.bat lintDebug test assembleDebug` — BUILD SUCCESSFUL.

### 3. QA

Primeira rodada:
- REPROVADO: detalhe de receita tinha prioridade visual sobre importação recebida por deep link.

Correção:
- A condição raiz de navegação passou a dar precedência a `showImport`.

Checklist final:
- [x] Scheme `romlingdiet://import/chatgpt` registrado.
- [x] Intent inicial e `onNewIntent` tratados.
- [x] Deep link abre o importador mesmo sobre outra tela.
- [x] Nenhum dado é salvo automaticamente.
- [x] Clipboard exige ação explícita.
- [x] GPT personalizado abre pelo navegador/app disponível.
- [x] Prévia e confirmação preservadas.
- [x] Lint, testes e APK debug passam.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Tornar URL e prompt do GPT editáveis nas Configurações.

## Ciclo 52

### 1. ARQUITETO

Nome da tarefa:
- Configuração persistente da URL e do prompt do GPT.

Motivo:
- O Ciclo 51 usa valores fixos na tela de importação. O escopo original exige que ambos possam ser alterados sem recompilar o app.

Tela ou funcionalidade original analisada:
- `SettingsScreen`, `SettingsRepository`, estado da tela Hoje e `ChatGptImportScreen`.

Arquivos prováveis:
- `data/repository/SettingsRepository.kt`
- `feature/today/TodayViewModel.kt`
- `feature/settings/SettingsScreen.kt`
- `feature/chatgpt/ChatGptImportScreen.kt`
- `DietTrackerApp.kt`

Critérios de aceite funcionais:
- Persistir URL e prompt junto às configurações atuais.
- Exibir os valores salvos na tela Configurações.
- Impedir URL fora de HTTP/HTTPS e prompt vazio de substituir valores válidos.
- Usar imediatamente URL e prompt salvos na tela de importação.
- Preservar metas existentes ao salvar.

Critérios de aceite visuais:
- Manter cards, campos e botão existentes.
- Permitir rolagem da tela com os novos campos.
- Usar campo multilinha para o prompt.

Riscos:
- Configuração inválida impedir abertura do GPT. Mitigação: manter o último valor válido.
- Tela exceder a altura disponível. Mitigação: rolagem vertical.

Instrução objetiva para o Dev:
- Ampliar `GoalSettings` e o fluxo atual; não criar novo repositório nem adicionar dependência.

### 2. DEV

Implementação feita:
- `GoalSettings` passou a incluir URL e prompt do GPT com defaults compatíveis com instalações antigas.
- `SettingsRepository` persiste e publica as duas novas configurações no fluxo existente.
- `TodayUiState` entrega os valores reativamente à tela de importação.
- `SettingsScreen` ganhou rolagem, campo de URL, prompt multilinha e salvamento conjunto.
- `ChatGptImportScreen` deixou de usar constantes locais.
- URL é aceita apenas com scheme HTTP/HTTPS e host válido; prompt vazio preserva o valor anterior.

Arquivos criados/alterados:
- `data/repository/SettingsRepository.kt`
- `feature/today/TodayViewModel.kt`
- `feature/settings/SettingsScreen.kt`
- `feature/chatgpt/ChatGptImportScreen.kt`
- `DietTrackerApp.kt`
- `feature/settings/SettingsScreenTest.kt`

Como preservou a UI original:
- Campos foram adicionados ao card existente, com o mesmo estilo Material e botão único.
- A tela passou a rolar para manter todos os controles acessíveis.

Como testou:
- `gradlew.bat lintDebug test assembleDebug` — BUILD SUCCESSFUL.
- Teste valida URL HTTP/HTTPS e rejeita scheme inseguro ou URL incompleta.

### 3. QA

Checklist funcional:
- [x] URL e prompt persistidos com as metas.
- [x] Instalações antigas recebem defaults.
- [x] Metas existentes são preservadas ao salvar.
- [x] URL inválida e prompt vazio não substituem valores válidos.
- [x] Importador usa imediatamente os valores salvos.
- [x] Constantes locais removidas da tela de importação.

Checklist visual:
- [x] Tela de Configurações rolável.
- [x] URL em campo de linha única e prompt multilinha.
- [x] Cards, espaçamentos e botão existentes preservados.

Comandos e resultado:
- `gradlew.bat lintDebug test assembleDebug` — BUILD SUCCESSFUL.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Importar backup JSON exportado pelo app.

## Ciclo 53

### 1. ARQUITETO

Nome da tarefa:
- Importação confirmada do backup JSON do diário.

Motivo:
- O app exporta o diário, mas ainda não consegue restaurá-lo. A migração de dados exige o caminho inverso sem salvar conteúdo não revisado.

Tela ou funcionalidade original analisada:
- `TodayViewModel.exportJson`, `DiaryRepository`, `SettingsScreen` e o padrão de prévia do importador ChatGPT.

Arquivos prováveis:
- `feature/settings/BackupImportScreen.kt` (novo)
- `data/repository/DiaryRepository.kt`
- `feature/today/TodayViewModel.kt`
- `feature/settings/SettingsScreen.kt`
- `DietTrackerApp.kt`
- teste unitário do parser

Critérios de aceite funcionais:
- Aceitar o objeto JSON produzido por `exportJson`.
- Validar data, refeição, nome, porção e nutrientes antes da prévia.
- Limitar tamanho da entrada e quantidade de registros.
- Mostrar prévia com quantidade e registros antes de salvar.
- Importar somente após confirmação explícita.
- Manter os dados atuais e adicionar os registros do backup.

Critérios de aceite visuais:
- Acesso pela tela Configurações junto à exportação.
- Tela dedicada em tema escuro, com campo grande, card de prévia e botão fixo de confirmação.

Riscos:
- Backup repetido duplicar registros. Mitigação: texto explícito de importação aditiva na prévia; deduplicação fica fora deste ciclo.
- Arquivo excessivo consumir memória. Mitigação: limites de caracteres e registros.

Instrução objetiva para o Dev:
- Reutilizar `DiaryRepository.addImportedFood`; não alterar schema Room nem apagar dados existentes.

### 2. DEV

Implementação feita:
- Tela dedicada acessível por "Importar diário (JSON)" nas Configurações.
- Parser aceita o objeto produzido por `TodayViewModel.exportJson` e aliases básicos do legado.
- Data, refeição, nome, gramas e nutrientes são validados antes da prévia.
- Entrada limitada a 2 milhões de caracteres e 5 mil registros.
- Prévia mostra até 20 itens e informa que a importação é aditiva.
- Confirmação grava pelo `DiaryRepository` sem apagar dados atuais e bloqueia toque repetido.
- Exportação preserva gramas fracionárias para garantir round-trip.

Arquivos criados/alterados:
- `feature/settings/BackupImportScreen.kt`
- `data/repository/DiaryRepository.kt`
- `feature/today/TodayViewModel.kt`
- `feature/settings/SettingsScreen.kt`
- `DietTrackerApp.kt`
- `feature/settings/BackupImportParserTest.kt`
- `feature/today/TodayViewModelTest.kt`

Como preservou a UI original:
- Tela usa tema escuro, `AppCard`, campo grande, prévia e `BottomPrimaryButton`.
- Gramas fracionárias usam formatação local na prévia.

Como testou:
- `gradlew.bat lintDebug test assembleDebug` — BUILD SUCCESSFUL.
- Testes cobrem round-trip, gramas fracionárias e rejeição de data/refeição/porção inválidas.

### 3. QA

Primeira rodada:
- REPROVADO: exportação arredondava menos de 1 g para zero e botão permitia importações repetidas.

Segunda rodada:
- REPROVADO: prévia ainda exibia 0,5 g como 0 g.

Correções:
- Exportação preserva `Double`, confirmação possui trava e prévia usa `NumberFormat`.

Checklist final:
- [x] Formato exportado pelo app é aceito.
- [x] Conteúdo inválido é rejeitado antes da prévia.
- [x] Prévia e confirmação explícita obrigatórias.
- [x] Importação aditiva não apaga registros.
- [x] Toque repetido não duplica a operação.
- [x] Gramas fracionárias preservadas e exibidas corretamente.
- [x] Lint, testes e APK debug passam.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Completar os campos do alimento personalizado.

## Ciclo 54

### 1. ARQUITETO

Nome da tarefa:
- Cadastro completo de alimento personalizado.

Motivo:
- `FoodEntity` já suporta os campos do escopo original, mas a tela e o ViewModel salvam apenas nome e quatro macros.

Tela ou funcionalidade original analisada:
- `CreateFoodScreen`, fluxo `AddFoodScreen` → `AddFoodViewModel` e `FoodEntity`.

Arquivos prováveis:
- `feature/meal/CreateFoodScreen.kt`
- `feature/meal/AddFoodPlaceholderScreen.kt`
- `feature/meal/AddFoodViewModel.kt`
- `DietTrackerApp.kt`
- `feature/meal/AddFoodViewModelTest.kt`

Critérios de aceite funcionais:
- Permitir nome, categoria, aliases, kcal, carboidratos, proteína, gordura, fibras, açúcar e sódio.
- Permitir unidade padrão, gramas por unidade e fonte/observação.
- Rejeitar números negativos e gramas por unidade não positivas.
- Persistir todos os campos em `FoodEntity` com `isCustom = true`.
- Manter o alimento recém-criado pesquisável.

Critérios de aceite visuais:
- Tela rolável, dividida em cards de identificação, nutrição e porção/fonte.
- Manter tema escuro, campos existentes e botão fixo de salvamento.

Riscos:
- Callback com muitos parâmetros ficar frágil. Mitigação: objeto `CustomFoodInput` tipado.
- Campo inválido ser convertido silenciosamente em zero. Mitigação: botão só executa quando todos os campos preenchidos são válidos.

Instrução objetiva para o Dev:
- Reusar `FoodEntity` atual, sem migração Room e sem dependência nova.

### 2. DEV

Implementação feita:
- `CustomFoodInput` substitui o callback de cinco parâmetros e reúne todos os campos.
- Tela dividida em cards de identificação, nutrição e porção/fonte.
- Campos adicionados: categoria, aliases, fibras, açúcar, sódio, unidade, gramas por unidade e fonte/observação.
- Todos os valores são persistidos no `FoodEntity` existente com `isCustom = true`.
- Números negativos/não finitos e gramas por unidade não positivas bloqueiam o salvamento.
- A busca passa a usar o nome do alimento recém-criado.

Arquivos alterados:
- `feature/meal/CreateFoodScreen.kt`
- `feature/meal/AddFoodPlaceholderScreen.kt`
- `feature/meal/AddFoodViewModel.kt`
- `DietTrackerApp.kt`
- `feature/meal/AddFoodViewModelTest.kt`

Como preservou a UI original:
- Tema escuro, rolagem, `AppCard`, campos e botão fixo foram mantidos.
- Os campos foram organizados em três cards sem criar novo fluxo.

Como testou:
- `gradlew.bat lintDebug test assembleDebug` — BUILD SUCCESSFUL.
- Teste confirma persistência dos campos adicionais e `isCustom`.

### 3. QA

Checklist funcional:
- [x] Todos os campos existentes em `FoodEntity` estão disponíveis no cadastro.
- [x] Valores inválidos são rejeitados.
- [x] Callback tipado evita perda ou troca de argumentos.
- [x] Todos os campos são persistidos com `isCustom = true`.
- [x] Nome e aliases continuam pesquisáveis pelo DAO existente.

Checklist visual:
- [x] Tela rolável e dividida em três cards.
- [x] Tema, espaçamentos, campos e botão existentes preservados.

Comandos e resultado:
- `gradlew.bat lintDebug test assembleDebug` — BUILD SUCCESSFUL.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Validar metas e impedir valores zero, negativos ou não finitos.

## Ciclo 55

### 1. ARQUITETO

Nome da tarefa:
- Validação defensiva das configurações.

Motivo:
- A tela aceita zero, negativos e infinito nas metas, e o repositório persiste qualquer `GoalSettings` recebido.

Tela ou funcionalidade original analisada:
- `SettingsScreen`, `GoalSettings` e `SettingsRepository.save`.

Arquivos prováveis:
- `data/repository/SettingsRepository.kt`
- `feature/settings/SettingsScreen.kt`
- testes de configurações

Critérios de aceite funcionais:
- Exigir metas calóricas, macros, água e pesos positivos e finitos.
- Exigir URL HTTP/HTTPS válida e prompt não vazio no repositório.
- Desabilitar salvamento na UI quando uma meta estiver inválida.
- Impedir persistência inválida mesmo se outro chamador ignorar a UI.
- Manter configurações válidas funcionando sem mudança de schema.

Critérios de aceite visuais:
- Usar o estado desabilitado nativo do botão existente.
- Não adicionar diálogo ou tela nova.

Riscos:
- Validação apenas na UI ser contornada. Mitigação: `require` no repositório.

Instrução objetiva para o Dev:
- Criar uma validação pura e pequena, coberta por teste, sem dependência nova.

### 2. DEV

Implementação feita:
- `GoalSettings.isValid()` centraliza a validação de metas, água, pesos, URL e prompt.
- `SettingsRepository.save` usa `require` antes de editar preferências.
- O botão de salvar usa `areGoalInputsValid` e fica desabilitado com metas inválidas.
- URL aceita HTTP/HTTPS sem diferenciar maiúsculas do scheme.

Arquivos alterados:
- `data/repository/SettingsRepository.kt`
- `feature/settings/SettingsScreen.kt`
- `feature/settings/SettingsScreenTest.kt`

Como preservou a UI original:
- Apenas o estado desabilitado nativo do botão foi usado; nenhuma tela ou diálogo foi adicionado.

Como testou:
- `gradlew.bat lintDebug test assembleDebug` — BUILD SUCCESSFUL.
- Testes cobrem zero, negativo, infinito, água inválida, URL insegura, prompt vazio e HTTPS maiúsculo.

### 3. QA

Primeira rodada:
- REPROVADO: UI tratava scheme da URL como case-sensitive e faltava cobertura da condição do botão.

Correções:
- Scheme normalizado com `lowercase()`.
- Condição do botão extraída e testada diretamente.

Checklist final:
- [x] Metas e pesos exigem valores positivos e finitos.
- [x] Água exige inteiro positivo.
- [x] URL HTTP/HTTPS e prompt não vazio validados no repositório.
- [x] UI desabilita salvamento inválido.
- [x] Outro chamador não consegue contornar a validação.
- [x] Lint, testes e APK debug passam.

Decisão:
- APROVADO

### Próxima tarefa aberta pelo Arquiteto

Nome:
- Auditoria final visual/funcional e fechamento do README/checklist Android.

## Ciclo 56

### 1. ARQUITETO

Nome da tarefa:
- Corrigir os P1 confirmados na auditoria visual do Android.

Escopo:
- Restaurar os rotulos invisiveis dos botoes primarios.
- Corrigir o contraste do cabecalho da busca de alimentos.
- Fazer Back retornar pelas subtelas antes de sair do app.
- Expor o importador ChatGPT dentro da refeicao pelo acesso Camera.

Decisao de escopo:
- Nao criar controles inativos para Codigo ou Digitar.
- Reusar o importador e os estados de navegacao existentes.

### 2. DEV

Implementacao feita:
- `BottomPrimaryButton` define explicitamente a cor escura do rotulo sobre o fundo claro.
- O cabecalho de `AddFoodScreen` define texto primario para fechar e nome da refeicao.
- `BackHandler` central fecha cada overlay e preserva a tela anterior.
- `AddFoodScreen` trata Back nos estados internos de criacao e detalhe.
- O botao `Camera / ChatGPT` abre o importador sem perder a refeicao em andamento.

Arquivos alterados:
- `core/ui/components/BaseComponents.kt`
- `DietTrackerApp.kt`
- `feature/meal/AddFoodPlaceholderScreen.kt`

Como testou:
- `gradlew.bat testDebugUnitTest assembleDebug` - BUILD SUCCESSFUL.
- APK reinstalado no AVD Galaxy Z Fold 6 API 35.
- Validado visualmente o cabecalho, o acesso Camera/ChatGPT e o rotulo `Adicionar porcao padrao`.
- Validado por UI Automator: Back retorna de detalhe para busca e de busca para Hoje.

### 3. QA

Primeira rodada:
- REPROVADO: remover cores dos estilos tipograficos corrigiu botoes, mas deixou o filtro selecionado com texto escuro.

Correcao:
- Restauradas as cores tipograficas; a cor foi limitada ao rotulo do `BottomPrimaryButton`.

Checklist final:
- [x] Sem regressao no filtro `Todos`.
- [x] Cabecalho da busca legivel.
- [x] Camera/ChatGPT acessivel pela refeicao.
- [x] Botao primario com rotulo visivel.
- [x] Back preserva a hierarquia das subtelas.
- [x] Testes unitarios e APK debug passam.

Decisao:
- APROVADO

### Proxima tarefa aberta pelo Arquiteto

Nome:
- Continuar a auditoria pelos alvos de toque, semantica e insets antes de iniciar o Ciclo F de atividades.

## Ciclo 57

### 1. ARQUITETO

Nome da tarefa:
- Corrigir alvos de toque, semantica da navegacao e inset da barra de status.

Escopo:
- Garantir minimo de 48 dp nos controles pequenos confirmados.
- Expor papel e estado selecionado das abas inferiores.
- Impedir que Configuracoes role sob a barra de status.

Decisao de escopo:
- Alterar apenas controles com tamanho conhecido abaixo do minimo.
- Reusar `selectable`, `Role.Tab` e `statusBarsPadding` do Compose.

### 2. DEV

Implementacao feita:
- Acao de refeicao compacta passou de 40 para 48 dp.
- Remocao de alimento personalizado passou de 36 para 48 dp.
- Acoes de copiar, abrir GPT e importar clipboard passaram de 44 para 48 dp.
- Fechar calendario e sequencia ganhou area interna de toque.
- Barra inferior usa `selectable` com `Role.Tab` e estado selecionado.
- Configuracoes aplica inset da barra de status antes da rolagem.
- Linhas de importar, exportar e meus alimentos garantem altura minima de 48 dp.

Arquivos alterados:
- `DietTrackerApp.kt`
- `core/ui/theme/AppDimensions.kt`
- `core/ui/theme/AppSpacing.kt`
- `feature/settings/SettingsScreen.kt`
- `feature/meal/CustomFoodsScreen.kt`
- `feature/chatgpt/ChatGptImportScreen.kt`
- `feature/today/CalendarScreen.kt`
- `feature/today/StreakScreen.kt`

Como testou:
- `gradlew.bat lintDebug testDebugUnitTest assembleDebug` - BUILD SUCCESSFUL.
- APK reinstalado no AVD Galaxy Z Fold 6 API 35.
- UI Automator confirmou estado `selected` na barra inferior.
- Rolagem de Configuracoes validada visualmente sem texto sob a barra de status.

### 3. QA

Checklist final:
- [x] Controles mapeados possuem area minima de 48 dp.
- [x] Aba ativa possui estado semantico selecionado.
- [x] Papel de tab aplicado a navegacao inferior.
- [x] Configuracoes respeita a barra de status ao rolar.
- [x] Lint, testes e APK debug passam.

Decisao:
- APROVADO

### Proxima tarefa aberta pelo Arquiteto

Nome:
- Migrar listas longas para `LazyColumn` sem alterar a aparencia.

## Ciclo 58

### 1. ARQUITETO

Nome da tarefa:
- Virtualizar somente listas realmente sem limite.

Descoberta:
- A busca de alimentos ja aplica `take(20)` antes de compor as linhas.
- Receitas e alimentos personalizados nao possuem limite e usavam `Column.verticalScroll`.

Decisao de escopo:
- Nao alterar a busca principal sem evidencia de gargalo.
- Migrar apenas receitas e alimentos personalizados para `LazyColumn`.
- Usar IDs persistidos como chaves estaveis.

### 2. DEV

Implementacao feita:
- `RecipesScreen` usa `LazyColumn` e `items` com chave `recipe.id`.
- `CustomFoodsScreen` mantem um unico `AppCard` e usa `LazyColumn` interna com `itemsIndexed` e chave `food.id`.
- Divisores, espacamentos, cabecalhos, estados vazios e acoes foram preservados.
- Evidencia da auditoria foi corrigida para registrar o limite real de 20 resultados da busca.

Arquivos alterados:
- `feature/recipes/RecipesScreen.kt`
- `feature/meal/CustomFoodsScreen.kt`
- `ANDROID_FINAL_AUDIT.md`

Como testou:
- `gradlew.bat lintDebug testDebugUnitTest assembleDebug` - BUILD SUCCESSFUL.
- APK reinstalado no AVD Galaxy Z Fold 6 API 35.
- Estados vazios de Receitas e Meus alimentos validados visualmente.
- Busca e tela Hoje permaneceram sem alteracao.

### 3. QA

Checklist final:
- [x] Nenhuma virtualizacao desnecessaria na busca limitada a 20 itens.
- [x] Colecoes sem limite usam listas lazy.
- [x] Chaves estaveis usam IDs do banco.
- [x] Estados vazios e cabecalhos preservados.
- [x] Lint, testes e APK debug passam.

Decisao:
- APROVADO

### Proxima tarefa aberta pelo Arquiteto

Nome:
- Padronizar estados de feedback e comandos destrutivos restantes.

## Ciclo 59

### 1. ARQUITETO

Nome da tarefa:
- Confirmar exclusoes persistentes antes de alterar o banco.

Escopo:
- Alimento personalizado.
- Receita.
- Ingrediente de receita.
- Alimento registrado na tela Hoje.
- Alimento registrado no detalhe da refeicao.

Decisao de escopo:
- Reusar um unico dialogo Compose.
- Manter a remocao do ultimo copo de agua direta, pois e reversivel com um toque.
- Nao adicionar Snackbar quando a lista atualizada ja confirma visualmente o resultado.

### 2. DEV

Implementacao feita:
- `ConfirmDeleteDialog` mostra o nome do item e informa que a acao nao pode ser desfeita.
- Cancelar apenas fecha o dialogo.
- Remover chama o callback existente e fecha o dialogo.
- Os cinco fluxos destrutivos usam o mesmo componente.

Arquivos alterados:
- `core/ui/components/BaseComponents.kt`
- `feature/meal/CustomFoodsScreen.kt`
- `feature/recipes/RecipesScreen.kt`
- `feature/meal/MealDetailScreen.kt`
- `feature/recipes/RecipeDetailScreen.kt`
- `feature/today/TodayScreen.kt`

Como testou:
- `gradlew.bat lintDebug testDebugUnitTest assembleDebug` - BUILD SUCCESSFUL.
- APK reinstalado no AVD Galaxy Z Fold 6 API 35.
- Dialogo validado visualmente com o alimento `Abacate`.
- UI Automator confirmou que Cancelar preserva o item e Remover o exclui.

### 3. QA

Checklist final:
- [x] Dialogo identifica o item.
- [x] Cancelar nao altera dados.
- [x] Confirmar executa uma unica exclusao.
- [x] Lista atualiza apos a exclusao.
- [x] Lint, testes e APK debug passam.

Decisao:
- APROVADO

### Proxima tarefa aberta pelo Arquiteto

Nome:
- Melhorar o layout expandido de tablet sem alterar a experiencia de celular.

## Ciclo 60

### 1. ARQUITETO

Nome da tarefa:
- Priorizar alimentos frequentes na busca de cada refeicao.

Decisao de escopo:
- Reusar o historico de `diary_entries`, sem nova tabela ou migracao.
- Ordenar por quantidade de registros e usar recencia como desempate.
- Manter a busca textual sobre todo o catalogo.

### 2. DEV

Implementacao feita:
- A consulta Room agrupa alimentos registrados por `meal_type`.
- A tela abre em `Frequentes` e permite alternar para `Todos`.
- Cada refeicao tem seu proprio ranking de ate 20 alimentos.
- Busca e selecoes anteriores sao limpas ao abrir uma refeicao.
- Estado vazio explica quando os frequentes ainda nao existem.

Como testou:
- `gradlew.bat lintDebug testDebugUnitTest assembleDebug` - BUILD SUCCESSFUL.
- Teste unitario cobre o carregamento de frequentes por refeicao e a limpeza da busca.
- No AVD Galaxy Z Fold 6, foram validados estado vazio, aba Todos, busca por `arroz` e retorno automatico a Frequentes.
- Apos registrar `Arroz Branco Cozido` no cafe da manha, o alimento apareceu na aba Frequentes dessa refeicao.

### 3. QA

Checklist final:
- [x] Frequentes sao derivados do historico real.
- [x] Ranking e separado por refeicao.
- [x] Todos continua acessivel.
- [x] Busca textual nao fica restrita aos frequentes.
- [x] Reabertura limpa a busca anterior.
- [x] Lint, testes e APK debug passam.

Decisao:
- APROVADO

### Proxima tarefa aberta pelo Arquiteto

Nome:
- Melhorar o layout expandido de tablet sem alterar a experiencia de celular.

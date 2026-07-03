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

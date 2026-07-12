# Auditoria Visual e Funcional Android

Data: 2026-07-11

Status: plano para revisao. Nenhuma correcao desta auditoria foi aplicada.

## Escopo e evidencias

- Inventario estatico de todas as telas Compose, rotas, componentes, tokens e estados.
- Comparacao com o `UI_PARITY_PLAN` e com as referencias visuais do projeto.
- Validacao automatizada com `lintDebug`, testes unitarios e `assembleDebug`.
- Validacao interativa no AVD Galaxy Z Fold 6, API 35, resolucao reportada de 1856 x 2160 px.
- Telas observadas: Hoje, adicionar alimento, detalhe do alimento, detalhe da refeicao, receitas, configuracoes, importador ChatGPT, Jejum e Pro.
- Resultado automatizado: `BUILD SUCCESSFUL` (80 tarefas, 2026-07-11).
- Cobertura existente: 10 suites unitarias; nenhuma suite `androidTest`/instrumentada.

A matriz completa de celulares, fonte ampliada e landscape ainda precisa ser executada; os achados marcados como observados foram reproduzidos no AVD acima.

## Resumo executivo

Nao foi encontrado bloqueador P0 no codigo ou no build. A base funcional esta ampla e coerente com a migracao: diario, refeicoes, alimentos, agua, peso, calendario, sequencia, receitas, configuracoes, backup e importacao ChatGPT estao conectados.

O teste visual confirmou defeitos de entrega: botoes primarios inferiores aparecem brancos e sem rotulo, o cabecalho da busca fica preto sobre fundo escuro, o Back do sistema sai do app a partir de subtelas e o fluxo de adicionar alimento nao apresenta o acesso por Camera/ChatGPT solicitado.

## Status das correcoes

Validado no AVD Galaxy Z Fold 6, API 35, em 2026-07-11:

- [x] Botoes primarios inferiores voltaram a exibir rotulos com contraste.
- [x] Cabecalho da busca exibe fechar e nome da refeicao em cor legivel.
- [x] Back percorre detalhe do alimento -> busca -> Hoje e importador -> busca.
- [x] Acesso `Camera / ChatGPT` foi adicionado dentro da refeicao e abre o importador.
- [x] Barra inferior informa semanticamente a aba selecionada e usa papel de tab.
- [x] Acoes conhecidas de 36/44 dp foram elevadas ao minimo de 48 dp.
- [x] Configuracoes permanece abaixo da barra de status durante a rolagem.
- [x] Receitas e alimentos personalizados usam listas lazy com chaves estaveis.
- [x] Busca de alimentos permanece limitada a 20 resultados; nenhuma virtualizacao desnecessaria foi aplicada.
- [x] Exclusoes persistentes pedem confirmacao com o nome do item antes de remover.
- [x] Busca de alimentos oferece `Frequentes` por refeicao, `Todos` e busca no catalogo.
- [x] Em tablets largos, a area funcional fica centralizada e limitada a 840 dp sem alterar celulares.
- [x] Abas sem funcao `Jejum` e `Pro` foram removidas da navegacao.
- [x] Fluxo Hoje passa em 322/360/412 dp, fonte 200%, portrait e landscape.
- [x] Fundacao Room e calculo MET de Atividades passam nos exemplos de aceite.
- [ ] Demais achados P1/P2/P3 e o Ciclo F de atividades continuam abertos.

## Pontuacao

| Dimensao | Nota | Leitura |
| --- | ---: | --- |
| Acessibilidade | 1/4 | Contraste aparente bom, mas semantica, rotulos e alvos de toque precisam de revisao. |
| Performance | 3/4 | Arquitetura local simples; listas longas ainda usam `Column` com `verticalScroll`. |
| Theming e consistencia | 2/4 | Tokens existem, mas foram observados contrastes e rotulos de botao quebrados. |
| Responsividade | 1/4 | O tablet usa o layout de telefone esticado e apresenta grandes vazios; faltam os demais testes. |
| Anti-patterns de UX | 1/4 | Back incorreto, placeholders e acesso ausente a Camera/ChatGPT quebram jornadas esperadas. |
| **Total** | **8/20** | Base funcional boa, mas ha defeitos visuais e de navegacao antes da entrega. |

## Achados priorizados

### P0 - Bloqueadores

- Nenhum achado P0 confirmado.

### P1 - Corrigir antes da entrega

1. **Acessibilidade de controles customizados**
   - Evidencia: varios `Modifier.clickable` em texto, `Column`, `Row` e `Box` sem `role`, descricao ou estado selecionado; exemplos na barra inferior, calendario, agua, peso e lista de alimentos.
   - Impacto: TalkBack pode anunciar controles de forma incompleta e testes semanticos ficam frageis.
   - Acao: usar `IconButton`/`Button` onde couber e adicionar `semantics`, `Role`, `selected`, `stateDescription` e descricoes localizadas aos demais.
   - Aceite: percurso principal completo por TalkBack, foco previsivel e todos os controles com nome e funcao.

2. **Alvos de toque abaixo de 48 dp**
   - Evidencia: acoes de 36, 40 e 44 dp em alimentos, refeicoes e importador.
   - Impacto: remocao, adicao e selecao ficam mais dificeis, especialmente em telas compactas.
   - Acao: preservar o tamanho visual quando necessario, mas garantir area interativa minima de 48 x 48 dp.
   - Aceite: nenhum controle acionavel abaixo de 48 x 48 dp no Layout Inspector/teste semantico.

3. **Abas `Jejum` e `Pro` levam apenas a `Em breve` - CORRIGIDO**
   - Evidencia: destinos `FASTING` e `PRO` continuam expostos na navegacao principal sem fluxo funcional.
   - Impacto: navegacao promete funcionalidades indisponiveis e passa sensacao de app incompleto.
   - Acao: implementar o escopo definido ou ocultar/desabilitar essas abas ate haver funcionalidade real.
   - Aceite: toda aba visivel possui uma jornada util e estado vazio acionavel.

4. **Auditoria final em dispositivo parcialmente reproduzida**
   - Evidencia: 322/360/412 dp, fonte 100%/200% e portrait/landscape foram iniciados; fonte 200% revelou cortes e ainda nao existe `androidTest`.
   - Impacto: teclado, recortes, deep link e persistencia ainda nao foram revalidados em toda a matriz.
   - Acao: completar a matriz minima e automatizar os percursos criticos.
   - Aceite: evidencias em 322/360/412 dp, fonte 100% e 200%, portrait e ao menos um landscape.

5. **Botoes primarios inferiores aparecem sem texto**
   - Evidencia observada: detalhe do alimento e detalhe da refeicao exibem uma barra branca clicavel, mas o rotulo fica invisivel.
   - Impacto: a acao principal da tela nao pode ser identificada visualmente.
   - Acao: definir explicitamente a cor de conteudo do `BottomPrimaryButton` e validar todos os seus usos.
   - Aceite: rotulos como `Adicionar` e `Adicionar mais` possuem contraste legivel em todas as telas.

6. **Cabecalho da busca possui contraste quebrado**
   - Evidencia observada: `x Cafe da manha` aparece preto sobre o fundo verde muito escuro na tela de adicionar alimento.
   - Impacto: titulo e acao de fechar ficam quase invisiveis.
   - Acao: aplicar os tokens de texto primario ao cabecalho e ao comando de fechar.
   - Aceite: titulo e fechar atingem contraste legivel no tema escuro.

7. **Back do sistema sai do app a partir de subtelas**
   - Evidencia observada: ao pressionar Back no importador ChatGPT, a Activity foi encerrada e a tela inicial do Android apareceu.
   - Impacto: comportamento diverge da seta de voltar e pode causar perda de contexto ou de texto digitado.
   - Acao: centralizar a pilha de navegacao ou tratar Back com o mesmo estado usado pelos controles de fechar.
   - Aceite: Back retorna uma tela por vez em detalhe, busca, importacao, calendario, sequencia, peso e backup.

8. **Acesso por Camera/ChatGPT ausente na tela da refeicao**
   - Evidencia observada: adicionar alimento mostra apenas fechar, criar, busca e lista completa; nao apresenta Pesquisar/Camera/Codigo/Digitar nem filtro de frequentes.
   - Impacto: o caminho solicitado pelo usuario para abrir o importador pela Camera nao existe nessa jornada; o importador aparece apenas como card global em Hoje.
   - Acao: restaurar a faixa de ferramentas e ligar Camera ao importador ChatGPT mantendo a refeicao selecionada.
   - Aceite: Camera abre o importador a partir de cada refeicao e a confirmacao salva nela; frequentes variam por refeicao.

### P2 - Alto valor de acabamento

5. **Listas longas sem virtualizacao**
   - Evidencia corrigida: a busca principal limita a UI a 20 resultados; receitas e alimentos personalizados eram colecoes sem limite usando `Column.verticalScroll`.
   - Impacto: composicao mais cara, pior tempo de abertura e maior uso de memoria conforme os dados crescem.
   - Acao: migrar somente colecoes realmente sem limite para `LazyColumn` com `key` estavel, preservando cabecalhos e botoes fixos.
   - Aceite: rolagem fluida e apenas itens visiveis compostos em listas de 200+ registros.

6. **Iconografia baseada em emoji e simbolos de texto**
   - Evidencia: barra inferior e varias acoes usam emojis ou caracteres como `+`, `x` e setas.
   - Impacto: renderizacao varia entre versoes Android, alinhamento e leitura por acessibilidade ficam inconsistentes.
   - Acao: substituir comandos por Material Icons ou assets vetoriais; manter emoji apenas como conteudo ilustrativo.
   - Aceite: comandos possuem icone consistente, descricao e alinhamento em todas as densidades.

7. **Strings de interface espalhadas no Kotlin**
   - Evidencia: `strings.xml` contem apenas o nome do app; os textos das telas estao hardcoded nos composables.
   - Impacto: dificulta revisao ortografica, pluralizacao, acessibilidade e futura localizacao.
   - Acao: centralizar texto de interface em resources, incluindo descricoes de acessibilidade e plurais.
   - Aceite: nenhum texto voltado ao usuario permanece hardcoded fora de previews/testes.

8. **Responsividade parcial**
   - Evidencia: `AppDimensions` adapta alguns elementos em tres faixas, mas muitas alturas e larguras continuam fixas.
   - Impacto: fonte grande, landscape, teclado e telas largas podem cortar conteudo ou criar vazios excessivos.
   - Acao: testar e ajustar constraints, `WindowInsets`, IME, quebra de linhas e layouts de largura ampliada.
   - Aceite: sem corte, sobreposicao ou comando inacessivel na matriz de dispositivos e fontes.

9. **Estados de feedback precisam de padrao unico**
   - Evidencia: telas misturam texto inline, botoes customizados e dialogs para erro, vazio, confirmacao e sucesso.
   - Impacto: o usuario pode nao perceber salvamento, falha ou acao destrutiva em todos os fluxos.
   - Acao: catalogar e padronizar loading, vazio, erro recuperavel, sucesso e confirmacao destrutiva.
   - Aceite: cada jornada do roteiro possui estado definido para carregando, vazio, erro e sucesso quando aplicavel.

10. **Persistencia de configuracoes diverge do plano arquitetural**
    - Evidencia: configuracoes usam `SharedPreferences`; o plano de migracao menciona DataStore.
    - Impacto: nao e defeito visual, mas deixa documentacao e implementacao em desacordo.
    - Acao: decidir entre manter e documentar `SharedPreferences` ou migrar para DataStore em ciclo separado.
    - Aceite: uma unica decisao registrada no plano e no README.

11. **Layout de tablet apenas estica o conteudo de telefone**
    - Evidencia observada: cards usam quase toda a largura de 1856 px, metricas ficam muito separadas e detalhes/estados vazios deixam grandes areas sem estrutura.
    - Acao: limitar a largura do conteudo ou adotar composicao em duas colunas para telas expandidas.
    - Aceite: largura de leitura controlada e melhor aproveitamento do espaco no AVD testado.

12. **Conteudo rola sob a barra de status**
    - Evidencia observada: ao rolar Configuracoes, o card `Importar diario` ficou parcialmente encoberto pelos icones e horario do sistema.
    - Acao: aplicar insets de sistema ao container rolavel ou ao topo fixo.
    - Aceite: nenhum texto ou controle fica sob a barra de status durante a rolagem.

### P3 - Documentacao e manutencao

13. **README ainda descreve principalmente o app FastAPI/Termux**
    - Falta: abrir `android-native`, requisitos, build, APK, instalacao, ADB, deep link, backup/restauracao e solucao de problemas.

14. **Checklist superior do plano de migracao esta desatualizado**
    - Varios itens ja concluidos continuam desmarcados, embora os ciclos posteriores registrem conclusao.

15. **Gradle reporta recursos obsoletos para a futura versao 10**
    - O build atual passa; executar com `--warning-mode all` e registrar a origem antes de atualizar o wrapper/plugins.

16. **Faltam documentos curtos de produto e design**
    - O repositorio tem um plano detalhado, mas nao uma fonte concisa para publico, jornadas criticas, principios visuais e decisoes de UX.

## Pontos positivos

- Build, lint e testes unitarios passam juntos.
- Tema escuro, cores, tipografia, espacamento, formas e dimensoes possuem base tokenizada.
- Ha adaptacao explicita para larguras compactas, medias e ampliadas.
- Fluxos destrutivos importantes possuem confirmacao em partes relevantes do app.
- Regras de negocio criticas possuem testes unitarios: diario, sequencia, dia verde, seed, importacao, configuracoes e receitas.
- O app preserva a proposta local-first e nao depende de API externa para a importacao ChatGPT.

## Roteiro funcional de aceite

1. Abrir o app em instalacao limpa e confirmar seed, tela Hoje e navegacao inferior.
2. Trocar data, abrir calendario e voltar usando botao da UI e gesto/botao Back.
3. Adicionar alimento em cada refeicao por busca, frequentes, detalhe e alimento personalizado.
4. Remover um alimento pela tela da refeicao e pela visao de registrados.
5. Validar totais, macros, calorias e texto da refeicao depois de adicionar/remover.
6. Adicionar e remover agua; conferir meta e persistencia apos reiniciar.
7. Registrar peso, editar/remover quando permitido e conferir historico/persistencia.
8. Criar receita, adicionar ingredientes, incluir no diario e excluir receita.
9. Importar ChatGPT por JSON, Markdown, link/base64url, clipboard e deep link; testar payload invalido e cancelamento.
10. Exportar backup, importar com previa, cancelar e confirmar; conferir importacao aditiva.
11. Alterar metas/configuracoes, testar valores invalidos e reiniciar o app.
12. Repetir com TalkBack, fonte 200%, modo landscape e internet desligada.

## Plano de acao proposto

### Ciclo A - Gate de dispositivo e baseline

- AVD reproduzivel e APK atual instalados; baseline inicial capturado.
- Completar as telas restantes e executar o roteiro funcional em celular compacto.
- Registrar falhas novas com screenshot, dispositivo, passos e severidade.
- Saida: baseline aprovado pelo usuario antes de editar a UI.

### Ciclo B - Acessibilidade e navegacao

- Corrigir semantica, rotulos, foco, estados selecionados e alvos de toque.
- Substituir simbolos de comando por icones consistentes.
- Decidir o destino de `Jejum` e `Pro`.
- Adicionar testes Compose para barra inferior e jornadas essenciais.

### Ciclo C - Responsividade e performance

- Migrar listas dinamicas para componentes lazy.
- Validar fonte grande, IME, insets, landscape, compactos e tablets.
- Corrigir cortes, sobreposicoes e desalinhamentos encontrados no baseline.
- Medir abertura e rolagem das listas com 200+ alimentos.

### Ciclo D - Consistencia e estados

- Centralizar strings e descricoes em resources.
- Padronizar loading, vazio, erro, sucesso e confirmacoes.
- Revisar hierarquia tipografica, espacamento e contraste em todas as telas.
- Repetir a matriz visual e funcional completa.

### Ciclo E - Fechamento da migracao

- Atualizar README Android e checklist do plano.
- Resolver ou registrar a decisao sobre DataStore/SharedPreferences.
- Investigar avisos de depreciacao do Gradle.
- Rodar `lintDebug test assembleDebug`, gerar APK e anexar evidencias finais.

### Ciclo F - Exercicios, atividades e calorias gastas

Objetivo: permitir registrar exercicios do dia e preencher automaticamente o valor `Gastas` do resumo.

Telas e jornadas:

- Adicionar a secao `Atividades` na tela Hoje, abaixo dos registros corporais e antes das anotacoes.
- Mostrar atividades registradas no dia, calorias de cada uma, total diario e acao `Adicionar`.
- Criar a tela `Adicionar atividade` com busca, atividades frequentes, catalogo alfabetico e atividade personalizada.
- Incluir inicialmente musculacao, caminhada, ciclismo, corrida, eliptico, trilha, yoga, natacao, aerobica, agachamentos, artes marciais, alongamento e outras atividades comuns.
- Criar a tela de detalhe/registro com nome, icone, calorias estimadas, duracao, distancia quando aplicavel, nota opcional e botao Salvar.
- Permitir editar e remover atividades ja registradas.
- Recalcular imediatamente o card Resumo e a lista de atividades depois de salvar, editar ou remover.

Calculo proposto:

- Usar o peso mais recente do usuario e o MET da atividade.
- Formula: `kcal = MET * 3.5 * peso_kg / 200 * duracao_min`.
- Arredondar apenas o valor exibido; manter precisao no banco.
- Atividades com distancia podem registrar km para historico, mas a estimativa inicial continua baseada em MET e duracao.
- Permitir intensidade leve, moderada ou vigorosa quando a atividade possuir METs diferentes.
- Atividade personalizada deve aceitar nome, duracao e MET ou calorias informadas manualmente.
- Se nao houver peso registrado, usar o peso inicial das configuracoes e informar discretamente qual peso foi usado.
- `Gastas` deve ser a soma das calorias de todas as atividades do dia selecionado.
- `Restantes` deve considerar a regra definida para o app: `meta - consumidas + gastas`.

Dados minimos:

- `ActivityType`: nome, aliases, categoria, icone, MET leve/moderado/vigoroso, exige distancia e ativo.
- `ActivityEntry`: data, tipo, duracao, distancia opcional, intensidade, peso usado, calorias calculadas, nota e timestamps.
- Consultas para atividades do dia, frequentes por contagem/recencia e soma diaria de calorias.

Passos:

- Primeira entrega: entrada manual de passos e estimativa explicita, sem prometer monitoramento em segundo plano.
- Segunda entrega opcional: integrar Health Connect para passos automaticos, somente apos permissao do usuario.
- Evitar dupla contagem quando passos importados e caminhada manual cobrirem o mesmo periodo.

Validacao e aceite:

- Para 108 kg, musculacao moderada de 60 minutos com MET 4,5 deve estimar aproximadamente 510 kcal.
- Para 100 kg, caminhada moderada de 30 minutos com MET 3,5 deve estimar aproximadamente 184 kcal.
- Alterar duracao, intensidade ou peso atualiza a estimativa antes de salvar.
- Salvar uma atividade aumenta `Gastas`; editar recalcula; remover reduz o total.
- Trocar a data mostra apenas as atividades daquele dia.
- Frequentes variam conforme o historico real do usuario.
- Calculo, persistencia, soma diaria e protecao contra valores invalidos possuem testes unitarios.
- Fluxo adicionar/editar/remover e atualizacao do Resumo possuem teste Compose instrumentado.

## Decisoes pendentes do usuario

- `Jejum`: implementar agora, ocultar ou manter como indisponivel?
- `Pro`: implementar agora, ocultar ou manter como indisponivel?
- O alvo de entrega inclui tablet/landscape ou apenas celulares em portrait?
- A localizacao futura sera somente pt-BR ou devemos preparar outro idioma?
- Ha algum dispositivo fisico prioritario para a matriz final?
- Para passos automaticos, Health Connect deve entrar na primeira entrega ou depois do registro manual?
- As calorias gastas devem aumentar `Restantes` integralmente ou usar algum percentual configuravel?

# Design

## Principios

- Priorizar leitura rapida e acoes repetidas no diario.
- Usar tema escuro, contraste alto e verde apenas para progresso e acao positiva.
- Manter comandos com alvo minimo de 48 dp e descricao acessivel.
- Usar Material Icons para comandos; emoji apenas como ilustracao de alimento ou atividade.
- Pedir confirmacao antes de exclusoes persistentes.

## Responsividade

- Celular: conteudo em coluna e barra inferior fixa.
- Fonte ampliada: controles crescem; metricas podem empilhar.
- Tablet: area funcional centralizada com largura maxima de 840 dp.
- Matriz minima: 322, 360 e 412 dp; fonte 100% e 200%; portrait e landscape.

## Estados

- Vazio: explicar o que ainda nao foi registrado e manter a acao principal visivel.
- Erro recuperavel: mostrar a causa junto da acao de tentar novamente ou corrigir.
- Sucesso: preferir atualizacao imediata da lista e dos totais.
- Exclusao: dialogo com nome do item, Cancelar e Remover.

## Decisoes

- Room para dados relacionais; SharedPreferences para metas simples.
- Importacao e backup sao aditivos e exigem previa/confirmacao.
- Health Connect adiado para evitar permissoes e dupla contagem sem requisito explicito.

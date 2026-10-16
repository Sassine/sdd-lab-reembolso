# Spec NNN · <nome da mudança>

> **O quê e por quê.** Sem tabela, classe, framework ou endpoint aqui.
> Teste de cada frase: *se eu trocar de tecnologia, isto continua verdade?*
> Se não continua, a frase pertence ao plan.md.

- **Origem:** <ticket, documento de negócio>
- **Status:** rascunho | em esclarecimento | aprovada
- **Aprovada por:** <nome> em <data>

## Problema

<Duas a quatro frases. Quem sofre, com o quê, por que agora.>

## Fora de escopo

- <O que esta mudança NÃO faz. Tão importante quanto o escopo.>

## Requisitos

Use um ID por requisito: `**R1**`, `**R2**`… As tarefas vão apontar para eles.
Formato sugerido (EARS):

- Evento: `QUANDO <gatilho>, O SISTEMA DEVE <resposta>.`
- Estado: `ENQUANTO <estado>, O SISTEMA DEVE <resposta>.`
- Indesejado: `SE <condição indesejada>, ENTÃO O SISTEMA DEVE <resposta>.`

- **R1** — QUANDO ..., O SISTEMA DEVE ...
- **R2** — SE ..., ENTÃO O SISTEMA DEVE ...

## Critérios de aceite

Concretos e verificáveis. "Retorna 200" não é critério; diga o que volta e o que muda.

| ID | Requisito | Dado | Quando | Então |
|---|---|---|---|---|
| CA1 | R1 | ... | ... | ... |

## Comportamento que NÃO pode mudar

- <Regras existentes que esta mudança precisa preservar.>

## Casos de borda e erros

<Tudo que não for o caminho feliz. O que não estiver aqui tem morte silenciosa.>

## Dúvidas

<Marque cada uma com `[PRECISA ESCLARECER: pergunta]`. Enquanto existir uma,
a spec não vai para tasks. Quando responder, mova para "Decisões".>

## Decisões

| Data | Pergunta | Decisão | Quem decidiu |
|---|---|---|---|

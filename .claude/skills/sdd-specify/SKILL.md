---
name: sdd-specify
description: Transforma um ticket ou pedido de negócio em specs/NNN-nome/spec.md (o quê e o porquê, requisitos com ID, critérios de aceite). Use quando alguém pedir para especificar, escrever a spec ou começar uma feature nova a partir de um ticket. Não escreve código nem plano técnico.
---

# Especificar

Você vai produzir a spec de uma mudança. A spec é o contexto mais valioso do fluxo:
é ela que tira decisões da mão do modelo e devolve para quem responde por elas.

## Entrada

O caminho de um ticket (ex.: `tickets/TICKET-002-politica-v3.md`) ou a descrição do pedido.
Se não houver nenhum dos dois, pergunte antes de começar.

## Passos

1. Leia o ticket e os documentos de negócio que ele cita (`docs/`). Leia `sdd/constitution.md`.
2. Leia **só o necessário** do código para entender o comportamento atual. Se precisar
   varrer muitos arquivos, delegue a leitura ao agente `pesquisador` e use só o resumo.
3. Escolha o próximo número livre em `specs/` e crie `specs/NNN-nome-curto/spec.md`
   a partir de `sdd/templates/spec.md`.
4. Escreva cada requisito com ID (`**R1**`, `**R2**`...) em formato EARS:
   QUANDO / ENQUANTO / SE ... ENTÃO ... O SISTEMA DEVE ...
5. Para cada requisito, pelo menos um critério de aceite concreto (dado, quando, então),
   com valores reais. "Retorna 200" não serve: diga o que volta e o que muda no estado.
6. Preencha "Comportamento que NÃO pode mudar" com as regras vizinhas que existem hoje.
7. Tudo que o documento de negócio não responde vira `[PRECISA ESCLARECER: pergunta]`
   na seção Dúvidas. **Não invente a resposta.**

## Regras

- Nada de classe, tabela, endpoint, framework ou biblioteca na spec. Teste de cada
  frase: *se trocarmos de tecnologia, isto continua verdade?* Se não, é plano.
- Prefira 5 requisitos claros a 20 vagos. Spec proporcional ao tamanho da mudança.
- Adjetivo não é requisito ("rápido", "seguro", "amigável"). Transforme em número ou corte.

## Saída

Mostre ao humano: caminho criado, lista de requisitos (uma linha cada) e as dúvidas abertas.
Termine dizendo que o próximo passo é `/sdd-clarify` (ou responder as dúvidas).

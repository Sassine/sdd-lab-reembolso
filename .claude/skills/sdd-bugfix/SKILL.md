---
name: sdd-bugfix
description: Conduz a correção de um defeito em código existente com uma bugfix spec de três blocos (comportamento atual, esperado e o que não pode mudar) e teste de caracterização antes do conserto. Use quando o pedido for um bug, uma regressão ou um comportamento divergente da regra de negócio.
---

# Corrigir um defeito com spec mínima

Spec completa para um bug pequeno é usar marreta para quebrar noz. Mas corrigir sem
dizer o que **não pode mudar** é o caminho mais curto para a regressão. Este fluxo é o
meio-termo.

## Entrada

O ticket do defeito (ex.: `tickets/TICKET-003-prazo-60-dias.md`).

## Passos

1. Crie `specs/NNN-nome/bugfix.md` a partir de `sdd/templates/bugfix.md`.
2. **Comportamento atual:** reproduza com um exemplo concreto (entrada → saída).
3. **Comportamento esperado:** cite a fonte (documento de negócio, seção). Requisito `**R1**`.
4. **Não pode mudar:** liste as regras vizinhas que continuam valendo, cada uma como `**R2**`, `**R3**`...
5. Investigue a causa e registre `Classe#metodo` e o porquê. Mostre ao humano antes de corrigir.
6. Escreva primeiro o teste que **falha hoje** pelo motivo certo. Rode e mostre a falha.
7. Corrija com a menor mudança possível.
8. Rode o teste novo, os testes existentes da área e o portão completo.
9. Registre no `progress.md` da pasta e faça commit `fix: ...` com `Spec: specs/NNN-nome`.

## Regras

- Não aproveite para refatorar a vizinhança. Uma correção, um motivo.
- Se o documento de negócio for ambíguo sobre o comportamento esperado, pare e pergunte.

## Saída

Causa em uma linha, diff resumido, testes adicionados e resultado do portão.

---
name: sdd-retomar
description: Retoma o trabalho numa sessão nova, depois de um /clear, de uma queda ou no dia seguinte, reconstruindo o estado só pelos artefatos em disco e pelo git. Use no começo de uma sessão quando alguém disser "continua", "onde paramos" ou "retoma a spec".
---

# Retomar do disco

A sessão nova não lembra de nada. Isso é bom: ela nasce sem o lixo da anterior.
Tudo que importa está em disco. Siga o ritual na ordem, sem pular.

## Ritual

1. `pwd` e `git status`. Se houver mudança não commitada, mostre e pergunte se é
   trabalho em andamento ou resto a descartar. Não descarte sozinho.
2. `git log --oneline -10`. O último commit diz qual tarefa terminou de verdade.
3. Descubra a spec ativa: a pasta citada no último commit (`Spec:`), ou a mais recente em `specs/`.
4. Leia **a última entrada** do `progress.md` dessa spec (não o arquivo inteiro, se for longo).
5. `java scripts/SddCheck.java --proxima specs/NNN-nome`.
6. Rode o gate. Ele precisa estar verde antes de qualquer trabalho novo.

## Conferência de consistência

Compare as três fontes da verdade:

| Fonte | Diz |
|---|---|
| git | o que o código realmente tem |
| tasks.md | o que está marcado como feito |
| progress.md | a narrativa e as decisões |

Se discordarem (ex.: tarefa marcada `[x]` sem commit, ou commit sem `[x]`), **não
conserte por conta própria**: mostre a divergência ao humano e proponha a correção.

## Saída

Em até 8 linhas: spec ativa, tarefas feitas/total, última decisão relevante,
estado do gate e a próxima tarefa. Depois pergunte se segue com `/sdd-implement`.

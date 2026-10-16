# Lab 09 · Bônus: loop autônomo

**Objetivo:** entender o padrão de loop (conhecido como "Ralph") e os seus limites.

A ideia: em vez de uma sessão longa que enche a janela, rodar o mesmo prompt várias
vezes, cada vez numa sessão nova, até não sobrar tarefa. Cada volta lê o estado do disco,
faz **uma** tarefa, roda o portão e faz commit.

## O script

`scripts/loop.sh` (Claude Code, modo não interativo):

```bash
./scripts/loop.sh specs/002-<nome> 5     # no máximo 5 voltas
```

Ele para quando: não há tarefa pendente, o portão fica vermelho, ou o limite de voltas
acaba. Rode **sempre numa branch**, nunca na main.

## Antes de usar

- **Limite de voltas é trava de custo, não de segurança.** Ele limita o gasto, não o que
  o agente faz em cada volta. Permissões restritas continuam necessárias.
- **Spec ambígua + loop = estrago em escala.** O loop executa com convicção o que estiver
  escrito. Só use com spec, plano e tarefas revisados.
- **Acompanhe o loop.** Leia o `git log` e o `progress.md` entre voltas, principalmente
  nas primeiras vezes.
- Tarefa que falha duas vezes é sinal de spec ou plano errado, não de "mais uma volta".

## Para pensar

O orquestrador do Lab 05 e o loop resolvem o mesmo problema (janela que enche) de
jeitos diferentes. Quando você preferiria cada um?

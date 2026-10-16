# Lab 06 · Cair e voltar

**Objetivo:** provar que o estado vive em disco, não na conversa.

## 1. Salvar e limpar

No meio da implementação (entre duas tarefas):

```
/sdd-snapshot
```

Leia a entrada que ele gravou no `progress.md`. Tem as decisões e o porquê? Tem um
próximo passo que dá para executar sem contexto? Se não tem, corrija à mão: é isso
que a próxima sessão vai ler.

Agora limpe a sessão (`/clear` no Claude Code; no Copilot CLI, saia e abra de novo).

## 2. Retomar

```
/sdd-retomar
```

Ele precisa chegar à mesma próxima tarefa só olhando git, tasks e progress.

## 3. Sabotagem

1. Implemente a próxima tarefa normalmente, até o commit.
2. Agora estrague: apague uma linha importante da classe criada e faça commit
   "T00N: ajuste".
3. Limpe a sessão e rode `/sdd-retomar`. O portão deve ficar vermelho.
4. Recupere:

```bash
git log --oneline -3
git revert --no-edit HEAD
./scripts/verify.sh          # verde de novo
```

5. Registre no `progress.md` o que aconteceu. Se a tarefa precisar ser refeita,
   volte o checkbox para `[ ]` e diga o porquê.

**Git é a verdade do código. tasks.md é a verdade do status. progress.md é a verdade
da narrativa.** Quando discordam, quem resolve é você.

## 4. Portão automático (Claude Code)

```bash
cp .claude/settings.exemplo.json .claude/settings.json
```

Com isso, toda vez que o agente for encerrar uma resposta, o SddCheck roda. Se falhar,
ele é obrigado a continuar e corrigir. Repita a sabotagem 2 do Lab 04 e veja o agente
ser bloqueado. Se o agente não conseguir corrigir e ficar insistindo, apague o `.claude/settings.json`.
(O Copilot CLI também suporta hooks; veja a documentação da sua versão.)

# Lab 05 · Implementar como orquestrador

**Objetivo:** a sessão principal coordena; cada tarefa roda num sub-agente com a janela dele.

## 1. Uma tarefa

```
/sdd-implement specs/002-<nome>
```

Acompanhe o que o orquestrador faz:

1. confere `git status` e roda o portão **antes** de começar;
2. pega a próxima tarefa com `SddCheck --proxima`;
3. monta um briefing autocontido e despacha o `implementador`;
4. roda a verificação ele mesmo (não confia no "terminei");
5. despacha o `revisor` só com o diff e os requisitos;
6. marca `[x]`, escreve no `progress.md`, faz commit.

## 2. Medir

Depois de uma tarefa, rode `/context` na sessão principal. Compare com o que você
gastaria se a sessão principal tivesse lido e escrito todos os arquivos.

## 3. O revisor sem contexto

Leia a resposta do revisor. Ele não viu a conversa do implementador e por isso lê o
resultado como alguém de fora. Se ele aprovar sem ressalva nenhuma em todas as tarefas,
desconfie: peça para ele revisar especificamente os limites (R$ 500,00 exatos, R$ 120,00
exatos).

## 4. Continuar

Repita `/sdd-implement` para as próximas tarefas. A cada uma:

```bash
git log --oneline -5
java scripts/SddCheck.java specs/002-<nome>
```

O commit, o checkbox e o progress.md têm que contar a mesma história.

## Quando parar e assumir

- A mesma tarefa falhou duas vezes.
- O implementador pediu para tocar um arquivo fora da lista.
- O revisor apontou algo que a spec não decide.

Nenhum desses é falha do agente. Em geral é a spec ou o plano pedindo ajuste: volte,
arrume o artefato e rode de novo.

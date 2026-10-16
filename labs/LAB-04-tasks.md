# Lab 04 · Quebrar em tarefas

**Objetivo:** tarefas que cabem numa sessão, rastreáveis e verificáveis.

## 1. Gerar

```
/sdd-tasks specs/002-<nome>
java scripts/SddCheck.java specs/002-<nome>
```

## 2. Quebrar o portão de propósito

Edite `tasks.md` e faça cada uma destas coisas, uma por vez, rodando o SddCheck depois:

1. Apague a linha `Verificação:` de uma tarefa.
2. Troque `[R2]` por `[R99]`.
3. Marque duas tarefas `[P]` da mesma fase com um arquivo em comum.
4. Marque uma tarefa como `[x]` sem registrar no `progress.md`.
5. Volte um `[PRECISA ESCLARECER: ...]` para a spec.

Desfaça com `git checkout -- specs/`.

O SddCheck não usa IA. É um portão determinístico: sempre dá a mesma resposta para o
mesmo arquivo, e não se convence com argumento.

## 3. Tamanho

Para cada tarefa, pergunte:

- Cabe numa sessão sem passar de ~40% da janela?
- A descrição tem mais de um "e"? (provavelmente são duas)
- Ela termina com um commit que faz sentido sozinho?
- Um revisor consegue avaliar o diff em 5 minutos?

## 4. Paralelismo

Quais tarefas são `[P]`? Para rodar em paralelo de verdade, três condições: mesma fase,
arquivos diferentes e nenhuma dependência. E, na hora de executar, workspace isolado
(git worktree). Faltou uma, é sequencial.

---
name: sdd-implement
description: Orquestra a implementação de uma spec tarefa por tarefa a partir de tasks.md, delegando cada tarefa ao agente implementador, validando com o agente revisor e com o gate de testes, marcando o checkbox, registrando o progress.md e fazendo commit. Use quando tasks.md estiver aprovado e alguém pedir para implementar, executar as tarefas ou continuar a implementação.
---

# Implementar como orquestrador

Nesta sessão você **não escreve o código**. Você é o orquestrador: lê o estado,
despacha, verifica e registra. Isso mantém a sua janela limpa enquanto cada
sub-agente gasta a dele numa tarefa só.

## Entrada

A pasta da spec. Opcional: quantas tarefas fazer nesta sessão (padrão: 1).

## Ritual de início

1. `git status` deve estar limpo. Se não estiver, pare e pergunte.
2. Rode o gate (`./scripts/verify.sh` ou `.\scripts\verify.ps1`). Se já estiver
   vermelho antes de começar, pare: não construa em cima de base quebrada.
3. `java scripts/SddCheck.java --proxima specs/NNN-nome` para saber a próxima tarefa.

## Para cada tarefa

1. **Briefing.** Monte um briefing autocontido para o `implementador` com: ID e título,
   o trecho dos requisitos citados (copie da spec), o trecho relevante do plano,
   a lista de arquivos que a tarefa pode tocar, a verificação e a regra
   "não toque em outros arquivos; não apague testes".
2. **Despache** para o agente `implementador`. Se a ferramenta não tiver sub-agentes,
   execute você mesmo, limitado ao briefing.
3. **Verifique** você mesmo: rode a linha `Verificação:` da tarefa e depois o gate completo.
4. **Revise:** despache o agente `revisor` passando só o `git diff` e os requisitos da
   tarefa. Ele não vê a conversa do implementador, e é isso que o torna útil.
5. Se o revisor apontar problema bloqueante, devolva ao implementador **uma vez** com
   o apontamento. Se falhar de novo, pare e registre como bloqueada.
6. **Registre:** marque `[x]` na tarefa (só o checkbox), acrescente uma entrada no
   `progress.md` (modelo em `sdd/templates/progress.md`) e faça commit
   `T00N: descrição` com `Spec: specs/NNN-nome`.

## Paralelismo

Tarefas `[P]` da mesma fase podem ir para implementadores simultâneos **somente** se
a ferramenta isolar o workspace (ex.: git worktree). Sem isolamento, faça em sequência.
Leitura e revisão podem ser paralelas; escrita, em regra, não.

## Quando parar e chamar o humano

- Requisito ambíguo ou em conflito com o código existente.
- Mesma tarefa falhou duas vezes.
- Qualquer ação destrutiva (apagar dados, reescrever histórico, mudar dependências).
- A janela da sessão passou de ~40%: rode `/sdd-snapshot` e peça um `/clear`.

## Saída

Tarefas concluídas nesta sessão, hash dos commits, estado do gate e a próxima tarefa.

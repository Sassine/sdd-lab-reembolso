---
name: sdd-tasks
description: Quebra spec e plano aprovados em specs/NNN/tasks.md, com tarefas pequenas, rastreáveis aos requisitos, com arquivos, verificação e marcação [P] de paralelismo. Use depois do plano aprovado, quando alguém pedir para quebrar em tarefas ou montar o backlog da implementação.
---

# Quebrar em tarefas

Uma boa tarefa de agente cabe numa janela de contexto, toca um conjunto nomeado de
arquivos, diz como provar que terminou, aponta para um requisito e termina num commit.

## Entrada

A pasta da spec com `spec.md` e `plan.md` aprovados.

## Passos

1. Leia spec e plano. Não releia o código inteiro: o plano já resume o que importa.
2. Crie `specs/NNN/tasks.md` a partir de `sdd/templates/tasks.md`.
3. Organize em fases: fundação (o que bloqueia o resto) → trabalho independente →
   integração → acabamento.
4. Cada tarefa no formato exato:

   ```
   - [ ] T001 [P] [R1,R3] Descrição no imperativo
         Arquivos: `caminho/A.java`, `caminho/ATest.java`
         Verificação: `./mvnw -q test -Dtest=ATest`
   ```

5. Marque `[P]` só quando as três condições valem: mesma fase, nenhum arquivo em
   comum com outra `[P]` da fase, nenhuma dependência entre elas. Na dúvida, sequencial.
6. Todo requisito da spec precisa aparecer em pelo menos uma tarefa. Todo critério de
   aceite precisa de um teste em alguma tarefa.
7. Rode `java scripts/SddCheck.java specs/NNN-nome` e corrija até passar.

## Tamanho

Se a descrição pede "e" mais de uma vez, provavelmente são duas tarefas.
Se a verificação precisa de mais de um comando diferente, idem.

## Saída

Quantas tarefas, quantas fases, quais podem rodar em paralelo e a cobertura
requisito → tarefa. Próximo passo: o humano revisa; depois `/sdd-implement`.

---
name: sdd-snapshot
description: Salva o estado da sessão no progress.md da spec ativa antes de limpar o contexto, para que uma sessão nova continue sem reler o projeto. Use quando a janela estiver enchendo, antes de /clear, no fim do dia ou quando alguém disser "salva aí que eu vou dar clear".
---

# Salva aí que eu vou dar clear

Compactação automática resume sem saber o que era sinal e o que era ruído, e
carrega para frente os erros da sessão. Este passo faz a compactação de forma
intencional, num arquivo que o humano pode ler e corrigir.

## Passos

1. Identifique a spec ativa e a tarefa em andamento.
2. Acrescente ao final de `specs/NNN-nome/progress.md` (nunca reescreva o que já existe)
   uma entrada no modelo de `sdd/templates/progress.md`, com:
   - objetivo da sessão e status (concluída, parcial, bloqueada);
   - **decisões tomadas e o porquê** (é o que mais se perde);
   - arquivos tocados e arquivos lidos que foram úteis, com o motivo, para a próxima
     sessão não precisar procurar de novo;
   - o que foi tentado e não funcionou;
   - próximo passo concreto, no imperativo, que dê para executar sem contexto extra;
   - bloqueios e dúvidas abertas.
3. Se algo descoberto vale para o projeto inteiro (ex.: um comando que sempre precisa
   de uma flag), proponha ao humano acrescentar uma linha no `AGENTS.md`.
   Não acrescente sozinho.
4. Não inclua saídas de build, stack traces inteiros nem conteúdo de arquivos.
   Caminho e uma linha de resumo bastam.

## Saída

Mostre a entrada gravada e diga: "Pode dar /clear. Para continuar: /sdd-retomar."

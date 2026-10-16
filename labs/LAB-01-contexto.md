# Lab 01 · Contexto

**Objetivo:** ver a janela de contexto encher e decidir o que merece estar no `AGENTS.md`.

## 1. Linha de base

Sessão nova. Rode `/context` e anote o total usado e quanto é instrução de sistema,
ferramentas e arquivos de memória.

## 2. Encher de propósito

> Leia todos os arquivos .java do projeto e me explique a arquitetura.

Rode `/context` de novo. Anote. Agora pergunte algo que estava num arquivo lido
no começo e veja se a resposta continua precisa.

**O ponto:** cada token lido continua na janela e é reenviado em cada chamada seguinte.
Ler tudo "por garantia" custa dinheiro e tira atenção do que importa.

## 3. Fazer de novo, com sub-agente

Sessão nova:

> Use o agente pesquisador para descobrir onde a regra de prazo é aplicada e quais
> testes cobrem o limite de 60 dias. Traga só o resumo.

Rode `/context`. O sub-agente leu os arquivos na janela **dele**; para a sua voltou
só o resumo.

## 4. O diff do AGENTS.md

Para cada linha abaixo, decida: **entra** no `AGENTS.md`, **não entra**, ou **vai para
outro lugar** (constitution, spec, skill)?

1. "Use `./mvnw -q test` para rodar os testes."
2. "Spring Boot usa injeção de dependência por construtor."
3. "Dinheiro é sempre BigDecimal com 2 casas."
4. "O teto de refeição é R$ 120 por dia."
5. "Em `tasks.md`, mude só o checkbox."
6. "Java 21 tem records."
7. "Na máquina do time, o build precisa de `-Dhttps.protocols=TLSv1.2`; o agente descobre isso a cada sessão e gasta 5 minutos."

<details><summary>Uma resposta possível</summary>

1 entra (operação, não óbvio) · 2 não (o modelo já sabe) · 3 constitution (regra de
engenharia permanente) · 4 spec ou docs (regra de negócio, muda com a política) ·
5 entra (regra de trabalho) · 6 não (o modelo já sabe) · 7 entra: é o caso clássico
de algo que o agente redescobre e paga de novo toda vez.

</details>

**A pergunta que decide:** *o modelo já sabe isso?* Se sabe, fica fora.
Se ele descobre toda vez e paga de novo, entra.

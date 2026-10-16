---
name: sdd-clarify
description: Revisa uma spec procurando lacunas, ambiguidades e cenários sem tratamento (morte silenciosa) e conduz as perguntas ao humano, registrando as decisões na própria spec. Use depois de /sdd-specify e antes de /sdd-plan, ou quando alguém pedir para revisar ou esclarecer uma spec.
---

# Esclarecer

O modelo segue exatamente a direção que você deu. Ele não inventa o cenário que
ninguém pediu. Tudo que a spec não cobre morre em silêncio na implementação.
Este passo existe para encontrar esses buracos antes do código.

## Entrada

A pasta da spec (ex.: `specs/002-politica-v3`). Se não vier, use a spec mais recente
em status "rascunho" ou "em esclarecimento".

## Passos

1. Leia a spec e o documento de negócio de origem.
2. Procure, nesta ordem:
   - **Caminhos não tratados:** para cada requisito, o que acontece no erro, no vazio,
     no limite exato (ex.: exatamente R$ 500,00), no valor negativo, no repetido?
   - **Concorrência:** duas pessoas fazendo a mesma ação ao mesmo tempo.
   - **Termos ambíguos:** "por dia" de quem? "acima de" inclui o limite?
   - **Conflitos** entre requisitos, ou entre a spec e o comportamento atual.
   - **Critérios fracos:** aceite que só olha status HTTP.
   - **Spec smells:** detalhe técnico na spec, adjetivo sem número, requisito sem critério.
3. Liste no máximo 5 perguntas, da mais cara de errar para a mais barata.
   Para cada uma, ofereça 2 ou 3 opções e diga qual você recomendaria e por quê.
4. Faça as perguntas ao humano **uma de cada vez** e espere a resposta.
5. Para cada resposta: atualize o requisito ou critério afetado, remova o
   `[PRECISA ESCLARECER]` correspondente e registre a linha em "Decisões"
   (data, pergunta, decisão, quem decidiu).
6. Quando não sobrar dúvida, mude o status para "aprovada" **somente se o humano confirmar**.

## Saída

Resumo do que mudou na spec e quantas dúvidas restam. Se restar zero e a spec
estiver aprovada, o próximo passo é `/sdd-plan`.

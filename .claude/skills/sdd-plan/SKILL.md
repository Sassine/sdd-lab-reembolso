---
name: sdd-plan
description: Cria research.md e plan.md para uma spec aprovada, com o desenho técnico, contrato da API, modelo de dados, decisões com porquê e checagem contra a constitution. Use quando a spec estiver aprovada e alguém pedir o plano técnico, o desenho ou "como vamos implementar".
---

# Planejar

A spec diz o quê. O plano diz como, e registra por que este como e não outro.
Uma linha errada no plano vira centenas de linhas erradas de código: este é o
ponto de maior alavancagem para revisão humana.

## Entrada

A pasta da spec. Pare se a spec tiver `[PRECISA ESCLARECER` ou status diferente de
"aprovada" e diga o que falta.

## Passos

1. **Research.** Delegue ao agente `pesquisador` (ou faça você, se a ferramenta não
   tiver sub-agentes) a pergunta: "o que existe hoje que esta spec toca?". Ele deve
   devolver achados com `Classe#metodo`. Grave em `specs/NNN/research.md` usando
   `sdd/templates/research.md`. Na sessão principal, fique só com o resumo.
2. Crie `specs/NNN/plan.md` a partir de `sdd/templates/plan.md`.
3. Preencha a checagem da constitution artigo por artigo. Se algum artigo não puder
   ser cumprido, pare e pergunte ao humano.
4. Inclua um diagrama mermaid do desenho. Ele serve para o humano revisar a forma
   antes de ler o detalhe.
5. Para cada decisão relevante, registre as alternativas e o porquê.
6. Na estratégia de teste, ligue cada critério de aceite (CA1, CA2...) ao teste que vai prová-lo.

## Regras

- Dependência nova só com justificativa (constitution, art. 6).
- Não escreva código de produção aqui. Trechos curtos de assinatura ou JSON de exemplo são bem-vindos.
- Se descobrir que a spec está errada ou incompleta, **pare** e proponha a correção da
  spec. Não compense no plano.

## Saída

Caminhos criados, as 3 decisões mais importantes em uma linha cada e os riscos.
Próximo passo: o humano revisa o plano; depois `/sdd-tasks`.

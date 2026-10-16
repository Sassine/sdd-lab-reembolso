# Lab 08 · Medir em vez de achar

**Objetivo:** produzir o seu próprio dado sobre quando SDD compensa.

Num estudo controlado (METR, 2025), desenvolvedores experientes ficaram 19% mais lentos
com IA e, ao final, estimaram que tinham ficado 20% mais rápidos. A percepção errou o
sinal. Por isso este lab mede.

## A mudança

> Nova categoria `CURSO`, com teto de R$ 2.000,00 por pedido, e que sempre exige
> aprovação do Financeiro, independentemente do valor.

## Rodada A · só plan mode

Sessão nova, `Shift+Tab` para modo plano, cole a mudança acima, aprove o plano e
deixe implementar. Sem spec, sem tasks.

## Rodada B · SDD

`git stash` ou outra branch. Sessão nova: `/sdd-specify` com o texto acima →
`/sdd-clarify` → `/sdd-plan` → `/sdd-tasks` → `/sdd-implement`.

## Anote

| Métrica | A · plan mode | B · SDD |
|---|---|---|
| Tempo total (relógio) | | |
| Tempo seu, ativo (lendo, respondendo, revisando) | | |
| Tokens / custo (`/usage` ou `/cost`) | | |
| Testes novos | | |
| Defeitos achados depois (rode os casos do quadro abaixo) | | |
| Decisões tomadas pelo modelo sem perguntar | | |

Casos para testar nas duas: curso de R$ 2.000,00 exatos; R$ 2.000,01; curso de
R$ 100,00 aprovado só pelo gestor; gestor tentando aprovar como Financeiro.

## Discussão

- Em qual rodada você gastou mais tempo **seu**? E tempo de relógio?
- Para esta mudança, SDD completo valeu? Para qual tamanho de mudança passaria a valer?
- Quantas decisões de negócio o modelo tomou sozinho na rodada A?

SDD não é para tudo. Uma mudança pequena e clara cabe num plan mode. O que este lab
procura é o seu ponto de virada, medido, não estimado.

# Bugfix 003 · Pedido no 60º dia recusado

Origem: `tickets/TICKET-003-prazo-60-dias.md`

## Comportamento atual

Hoje = 20/10/2026. Despesa de 21/08/2026 (60 dias corridos atrás) → 422
"Prazo de submissão expirado".

## Comportamento esperado

Política de Despesas, seção 2: "até 60 (sessenta) dias corridos, contados da data da
despesa. O dia da despesa conta como dia zero." Logo, o 60º dia ainda está no prazo.

- **R1** — QUANDO a despesa tiver exatamente 60 dias corridos, O SISTEMA DEVE aceitar o pedido.

## Comportamento que NÃO pode mudar

- **R2** — SE a despesa tiver 61 dias ou mais, ENTÃO O SISTEMA DEVE continuar recusando por prazo.
- **R3** — SE a data da despesa estiver no futuro, ENTÃO O SISTEMA DEVE continuar recusando.
- **R4** — QUANDO a despesa for de hoje, O SISTEMA DEVE continuar aceitando.

## Causa provável

`servico/PoliticaDePrazo.java#validar` recusa com `dias >= 60`. A política diz "até 60",
que inclui o 60. Os testes da spec 001 cobriam 59 e 61 dias, nunca o limite exato,
e a spec 001 não tinha critério de aceite para o dia 60.

## Teste de caracterização

- Novo: `PoliticaDePrazoTest#aceitaDespesaDe60DiasAtras` (falha antes da correção).
- Precisam continuar verdes: `#rejeitaDespesaDe61DiasAtras`, `#rejeitaDespesaNoFuturo`, `#aceitaDespesaDeHoje`.

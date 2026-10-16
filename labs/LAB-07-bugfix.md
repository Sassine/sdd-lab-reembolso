# Lab 07 · Bugfix com spec mínima

**Objetivo:** corrigir um defeito real sem cerimônia demais e sem regressão.

## 1. O ticket

`tickets/TICKET-003-prazo-60-dias.md`: despesa de 21/08 submetida em 20/10 foi recusada.

Antes do agente: a política diz "até 60 dias corridos, o dia da despesa é o dia zero".
20/10 − 21/08 = 60 dias. Quem está certo, o colaborador ou o sistema?

## 2. Rodar

```
/sdd-bugfix tickets/TICKET-003-prazo-60-dias.md
```

Confira o `bugfix.md`:

- [ ] **Atual:** exemplo reproduzível (21/08 em 20/10 → 422)
- [ ] **Esperado:** cita a seção 2 da política
- [ ] **Não pode mudar:** 61 dias continua recusado; data futura continua recusada;
      hoje continua aceito
- [ ] **Causa:** `PoliticaDePrazo#validar` usa `>=` onde a política diz "até"
- [ ] O teste do dia 60 foi escrito **antes** da correção e falhou pelo motivo certo

## 3. A pergunta incômoda

A spec 001 já dizia "mais de 60 dias" (`**R3**`). Os testes da época cobriam 59 e 61,
mas não o 60. O código errou num ponto que **ninguém escreveu como critério de aceite**.

Abra `specs/001-reembolso-basico/spec.md` e veja: qual critério de aceite teria
pegado isso? Acrescente-o. A spec anda junto com o código.

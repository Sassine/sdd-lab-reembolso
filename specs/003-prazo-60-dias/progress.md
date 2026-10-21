# Progresso 003 · Pedido no 60º dia recusado

## 2026-10-21 16:20 · bugfix · concluída

- **O que foi feito:** teste do dia 60 escrito primeiro (falhou com "Prazo de submissão expirado", o motivo certo); correção de `>=` para `>` em `PoliticaDePrazo#validar`.
- **Decisões e porquê:** menor mudança possível; nenhuma refatoração da vizinhança. Acrescentado o critério CA3b na spec 001, que nunca cobriu o limite exato.
- **Verificação:** `./scripts/verify.sh` → verde; 59, 61, futuro e hoje continuam com o mesmo resultado.
- **Próximo passo concreto:** responder o TICKET-003 ao suporte.

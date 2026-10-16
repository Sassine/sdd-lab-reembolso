# Progresso 001 · Reembolso básico

## 2026-03-03 09:40 · T001 · concluída

- **O que foi feito:** entidade, enums e repositório.
- **Decisões e porquê:** `precision=12, scale=2` no valor (constitution, art. 2).
- **Verificação:** `./mvnw -q test-compile` → ok
- **Próximo passo concreto:** T002.

## 2026-03-03 10:15 · T002 · concluída

- **O que foi feito:** `Clock` como bean; `RelogioDeTeste` fixa hoje em 20/10/2026 10h (São Paulo).
- **Decisões e porquê:** relógio fixo em vez de mock, para que testes de API e de unidade usem a mesma data.
- **Próximo passo concreto:** T003 e T004 (independentes).

## 2026-03-03 11:30 · T003 · concluída

- **O que foi feito:** `PoliticaDePrazo` com testes de hoje, 59 dias, 61 dias e futuro.
- **Bloqueios / dúvidas:** nenhum.

## 2026-03-03 11:50 · T004 · concluída

- **O que foi feito:** `aprovar` e `rejeitar` exigem status SUBMETIDA.

## 2026-03-03 15:20 · T005 · concluída

- **O que foi feito:** serviço, controller, `TratadorDeErros` com Problem Details e 9 testes de API (CA1–CA7 + 404 + CNPJ inválido).
- **Verificação:** `./scripts/verify.sh` → verde
- **Próximo passo concreto:** nenhum. Spec 001 encerrada.

# Tarefas NNN · <nome da mudança>

> Regras: uma tarefa cabe numa sessão e termina num commit.
> `[P]` = pode rodar em paralelo com outras `[P]` da mesma fase (arquivos diferentes,
> sem dependência). `[R1,R2]` = requisitos da spec. `[INFRA]` = sem requisito direto.
> Toda tarefa tem `Arquivos:` e `Verificação:`. O agente só muda o checkbox.

## Fase 1 · Fundação

- [ ] T001 [R1] <descrição curta, no imperativo>
      Arquivos: `src/main/java/...`, `src/test/java/...`
      Verificação: `./mvnw -q test -Dtest=...`

## Fase 2 · <nome>

- [ ] T002 [P] [R2] <...>
      Arquivos: `...`
      Verificação: `...`
- [ ] T003 [P] [R3] <...>
      Arquivos: `...`
      Verificação: `...`

## Fase 3 · Integração

- [ ] T004 [R1,R2,R3] <...>
      Arquivos: `...`
      Verificação: `./scripts/verify.sh`

# AGENTS.md

Instruções operacionais para qualquer agente de código neste repositório.
Curto de propósito: só o que você não descobriria sozinho lendo o código.

## Projeto

Serviço de reembolso de despesas (Java 21, Spring Boot 3.5, H2 em memória).
Regras de negócio: `docs/politica-de-despesas.md`. Regras de engenharia: `sdd/constitution.md`.

## Comandos

| Para | Linux/macOS | Windows |
|---|---|---|
| Testes | `./mvnw -q test` | `mvnw.cmd -q test` |
| Um teste | `./mvnw -q test -Dtest=NomeDoTeste` | `mvnw.cmd -q test -Dtest=NomeDoTeste` |
| Gate completo | `./scripts/verify.sh` | `.\scripts\verify.ps1` |
| Checar artefatos SDD | `java scripts/SddCheck.java` | `java scripts\SddCheck.java` |
| Próxima tarefa | `java scripts/SddCheck.java --proxima specs/<pasta>` | idem |

O gate completo roda os testes **e** o SddCheck. Uma tarefa só termina com ele verde.

## Onde fica cada coisa

| Arquivo | Papel | Quem edita |
|---|---|---|
| `sdd/constitution.md` | regras permanentes | humano, com registro |
| `specs/NNN-nome/spec.md` | o quê e por quê, requisitos `**R1**` | humano aprova |
| `specs/NNN-nome/plan.md` | como, decisões técnicas e porquês | humano aprova |
| `specs/NNN-nome/tasks.md` | lista de tarefas, uma por sessão | agente marca `[x]` |
| `specs/NNN-nome/progress.md` | log do que aconteceu, só acrescenta | agente |

## Regras de trabalho

1. **Uma tarefa por sessão.** Termine, rode o gate, faça commit, registre no progress.md.
2. Em `tasks.md`, mude só o checkbox `[ ]` → `[x]`. Não reescreva nem reordene tarefas.
3. `progress.md` é append-only: nunca apague entradas anteriores.
4. Não apague nem enfraqueça testes para passar. Se algo parecer errado, pare e registre.
5. Se a spec não responder uma dúvida, **não decida sozinho**: registre `[PRECISA ESCLARECER: ...]` e pare.
6. Commits: `T00N: descrição curta` + linha `Spec: specs/NNN-nome`.
7. Notas de progresso não vão neste arquivo. Este arquivo é só operação.

## Retomando uma sessão

Leia nesta ordem: `git log --oneline -10` → `progress.md` da spec ativa →
`java scripts/SddCheck.java --proxima specs/<pasta>` → rode o gate antes de mexer.

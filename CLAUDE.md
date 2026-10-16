@AGENTS.md

## Específico do Claude Code

- Skills do fluxo SDD: `.claude/skills/` (`/sdd-specify`, `/sdd-clarify`, `/sdd-plan`,
  `/sdd-tasks`, `/sdd-implement`, `/sdd-retomar`, `/sdd-snapshot`, `/sdd-bugfix`).
- Sub-agentes: `.claude/agents/` (`pesquisador`, `implementador`, `revisor`).
- Use `/context` para ver quanto da janela está ocupado. Passou de ~40%, rode
  `/sdd-snapshot`, dê `/clear` e retome com `/sdd-retomar`.

## Compact Instructions

Se a compactação acontecer, preserve: a spec ativa (pasta), a tarefa em andamento,
decisões tomadas nesta sessão e o porquê, e os arquivos alterados. Descarte saídas de
build e logs de teste que já passaram.

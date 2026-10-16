#!/usr/bin/env bash
# Loop autônomo (lab 09). Cada volta = sessão nova = uma tarefa = um commit.
# Uso: ./scripts/loop.sh specs/002-nome [max_voltas]
# Rode numa branch, nunca na main. Requer Claude Code (modo não interativo).
set -euo pipefail
cd "$(dirname "$0")/.."

SPEC="${1:?informe a pasta da spec, ex.: specs/002-politica-v3}"
MAX="${2:-5}"

ramo=$(git rev-parse --abbrev-ref HEAD)
if [ "$ramo" = "main" ] || [ "$ramo" = "master" ]; then
  echo "Recusado: crie uma branch antes (git switch -c loop/$(basename "$SPEC"))."; exit 1
fi

PROMPT="Use a skill sdd-implement para executar exatamente UMA tarefa de $SPEC e encerrar.
Não faça perguntas. Se precisar de decisão humana, registre a tarefa como bloqueada no
progress.md, não faça commit de código e encerre."

for volta in $(seq 1 "$MAX"); do
  proxima=$(java scripts/SddCheck.java --proxima "$SPEC" | head -1)
  if [ "$proxima" = "NENHUMA" ]; then echo "Nada pendente. Fim."; exit 0; fi
  echo "== volta $volta/$MAX: $proxima"

  claude -p "$PROMPT" \
    --permission-mode acceptEdits \
    --allowedTools "Read,Edit,Write,Glob,Grep,Task,Bash(git:*),Bash(./mvnw:*),Bash(java scripts/SddCheck.java:*),Bash(./scripts/verify.sh)"

  if ! ./scripts/verify.sh > /dev/null 2>&1; then
    echo "Portão vermelho depois da volta $volta. Parando para revisão humana."; exit 2
  fi
  if grep -q "bloqueada" "$SPEC/progress.md" 2>/dev/null && [ "$(java scripts/SddCheck.java --proxima "$SPEC" | head -1)" = "$proxima" ]; then
    echo "Tarefa $proxima ficou bloqueada. Parando para revisão humana."; exit 3
  fi
done
echo "Limite de $MAX voltas atingido."

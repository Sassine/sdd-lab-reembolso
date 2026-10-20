#!/usr/bin/env sh
# Gate completo: testes + artefatos SDD. Uma tarefa só termina com isto verde.
set -e
cd "$(dirname "$0")/.."
echo "== testes"
./mvnw -q test
echo "== artefatos SDD"
java scripts/SddCheck.java
echo "GATE VERDE"

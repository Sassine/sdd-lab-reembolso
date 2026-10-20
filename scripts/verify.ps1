# Gate completo: testes + artefatos SDD. Uma tarefa só termina com isto verde.
$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")
Write-Host "== testes"
& .\mvnw.cmd -q test
if ($LASTEXITCODE -ne 0) { Write-Host "GATE VERMELHO: testes"; exit 1 }
Write-Host "== artefatos SDD"
& java scripts\SddCheck.java
if ($LASTEXITCODE -ne 0) { Write-Host "GATE VERMELHO: artefatos"; exit 1 }
Write-Host "GATE VERDE"

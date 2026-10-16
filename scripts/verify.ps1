# Portão completo: testes + artefatos SDD. Uma tarefa só termina com isto verde.
$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")
Write-Host "== testes"
& .\mvnw.cmd -q test
if ($LASTEXITCODE -ne 0) { Write-Host "PORTÃO VERMELHO: testes"; exit 1 }
Write-Host "== artefatos SDD"
& java scripts\SddCheck.java
if ($LASTEXITCODE -ne 0) { Write-Host "PORTÃO VERMELHO: artefatos"; exit 1 }
Write-Host "PORTÃO VERDE"

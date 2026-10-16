# Lab 00 · Preparação

**Objetivo:** ter o projeto rodando e saber o que o agente carrega sozinho quando abre.

## 1. Rodar o projeto

```bash
./mvnw -q test                # Windows: mvnw.cmd -q test
java scripts/SddCheck.java
```

Esperado: 13 testes verdes e `SDD-CHECK OK` com a spec 001 em `5/5 tarefas feitas`.

## 2. Abrir o agente na raiz do repositório

```bash
claude      # ou
copilot
```

## 3. Perguntar o que ele já sabe, sem deixar ele ler nada

> Sem abrir nenhum arquivo: quais comandos você usaria para rodar os testes deste
> projeto, e onde ficam as regras de negócio?

Se ele responder `./mvnw -q test` e `docs/politica-de-despesas.md`, ele leu o
`AGENTS.md` ao abrir. É a única coisa que entra na janela **sempre**, em toda sessão
e em todo sub-agente. Por isso ele é curto.

## 4. Ver o que fica disponível sob demanda

- Skills: digite `/` (Claude Code) ou `/skills list` (Copilot CLI). Aparecem as
  `sdd-*`. Na abertura, só o **nome e a descrição** de cada skill entram na janela; o corpo
  só é lido quando a skill é usada.
- Sub-agentes: `/agents` (Claude Code) ou `/agent` (Copilot CLI): `pesquisador`,
  `implementador`, `revisor`.

## Para levar

| Camada | Entra quando | Exemplo |
|---|---|---|
| Sempre | toda sessão, todo sub-agente | `AGENTS.md` |
| Sob demanda | quando a tarefa pede | skills `sdd-*` |
| Por mudança | quando você aponta | `specs/002-.../spec.md` |

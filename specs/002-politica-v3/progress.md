# Progresso 002 · Política de Despesas v3

> Append-only. Uma tarefa por sessão.

## 2026-10-21 17:03 · T001 · concluída

- **Objetivo da sessão:** definir os componentes de vigência pelo instante da submissão e preservar o prazo atual.
- **O que foi feito:** criados VersaoPolitica e PoliticaDeVigencia, com corte inclusivo em 01/11/2026 às 00:00 de Brasília; acrescentada sobrecarga de PoliticaDePrazo com data de referência explícita, preservando a assinatura anterior por delegação. Implementação delegada ao implementador e revisão independente aprovada, sem bloqueantes.
- **Decisões e porquê:** aceitar Instant capturado e LocalDateTime de Brasília permite reutilizar uma referência temporal e classificar o momento já persistido sem reler o relógio. Mantida a recusa em dias >= 60, conforme R9 e decisão de escopo. A captura no serviço e a integração com a API permanecem para T009; esta tarefa entrega somente a fundação.
- **Arquivos tocados:** `src\main\java\com\orbe\reembolso\dominio\VersaoPolitica.java`, `src\main\java\com\orbe\reembolso\servico\PoliticaDeVigencia.java`, `src\main\java\com\orbe\reembolso\servico\PoliticaDePrazo.java`, `src\test\java\com\orbe\reembolso\servico\PoliticaDeVigenciaTest.java`, `src\test\java\com\orbe\reembolso\servico\PoliticaDePrazoTest.java`, checkbox de `tasks.md` e este log.
- **Verificação:** implementador registrou teste primeiro com falha comportamental de fuso e falha de compilação das novas assinaturas; caracterização de 60 dias verde antes da mudança. Testes focados de vigência/prazo: 17 verdes. `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\verify.ps1` executado pelo implementador e novamente pelo orquestrador: testes e SddCheck verdes. `git diff --check` sem problemas.
- **Commit:** esta entrada integra o commit `T001: definir vigencia da politica pelo instante da submissao`; localizar o hash por `git log --oneline --grep="^T001:"`. Alterações preexistentes foram commitadas separadamente, por autorização do usuário, em `29c642e`.
- **Bloqueios / dúvidas:** nenhum.
- **Próximo passo concreto:** T002 — modelar as transições por etapa com histórico de decisões aceitas no agregado.

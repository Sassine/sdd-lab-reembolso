# Research 002 · Política de Despesas v3

## Pergunta

O que existe hoje que a política v3 toca: aprovação, status, consulta por dia e concorrência?

## Achados

| # | Achado | Evidência | Impacto no plano |
|---|---|---|---|
| 1 | Aprovação aceita qualquer responsável, inclusive o solicitante | `servico/ReembolsoService.java#aprovar` | R5 precisa de validação nova antes da transição |
| 2 | Transições ficam na entidade; `aprovar` e `rejeitar` exigem SUBMETIDA | `dominio/Reembolso.java#aprovar`, `#exigirStatus` | estender a entidade, não o serviço (constitution, art. 1) |
| 3 | Não há controle de versão na entidade | `dominio/Reembolso.java` (sem `@Version`) | explica o caso do ticket: última escrita vence. R7 precisa de `@Version` |
| 4 | Status atual: SUBMETIDA, APROVADA, REJEITADA, PAGA | `dominio/StatusReembolso.java` | falta o estado intermediário de R2 |
| 5 | O repositório não tem consulta por colaborador + data + categoria | `servico/ReembolsoRepository.java` | R6 precisa de uma soma nova |
| 6 | Prazo é uma política isolada e testada | `servico/PoliticaDePrazo.java` | modelo a seguir para as políticas novas (art. 3) |
| 7 | O corpo de aprovação é `{responsavel, motivo}`; aprovação e rejeição usam o mesmo DTO | `api/DecisaoRequest.java`, `api/ReembolsoController.java#aprovar` | aprovação ganha DTO próprio com papel; rejeição mantém o atual |
| 8 | Testes de API aprovam sem papel | `api/ReembolsoApiTest.java#aprovaReembolsoSubmetido`, `#naoAprovaDuasVezes` | precisam ser atualizados por causa da mudança de contrato (decisão registrada na spec) |
| 9 | `TratadorDeErros` não trata exceção de concorrência | `api/TratadorDeErros.java` | sem handler, conflito de versão viraria 500 |

## O que não foi encontrado

- Nenhum registro de quem aprovou em cada nível (só `decididoPor`).
- Nenhum teste de concorrência.

## Riscos e armadilhas

- Se o teto for verificado antes da duplicidade, um pedido duplicado de refeição recebe
  a mensagem errada (o original entra na soma).
- Exceção de versão só aparece no flush. Sem `saveAndFlush`, ela estoura no commit,
  fora do serviço, e a tradução para 409 fica mais frágil.

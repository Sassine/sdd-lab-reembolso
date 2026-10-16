# Spec 002 · Política de Despesas v3

- **Origem:** `tickets/TICKET-002-politica-v3.md`, `docs/politica-de-despesas.md` (seções 4.1 e 5)
- **Status:** aprovada
- **Aprovada por:** Marina (PO Finanças) em 21/10/2026

## Problema

A política v3 entra em vigor em 01/11. Hoje qualquer pessoa aprova qualquer pedido,
inclusive o próprio (apontamento de auditoria), não há teto de refeição nem alçada por
valor, e pedidos acima de R$ 5.000,00 entram como reembolso quando deveriam ser
adiantamento. Houve também um caso de dois gestores aprovando o mesmo pedido quase ao
mesmo tempo, com o histórico registrando o aprovador errado.

## Fora de escopo

- Autenticação: quem aprova é informado na requisição, como hoje.
- Motivo obrigatório na rejeição (seção 6 da política já pede, mas não é novo na v3;
  registrado para a próxima versão).
- Aprovação parcial (pagar só até o teto).
- Processo de adiantamento para valores acima de R$ 5.000,00.

## Requisitos

- **R1** — QUANDO o gestor aprovar um pedido de até R$ 500,00, O SISTEMA DEVE marcá-lo como aprovado.
- **R2** — QUANDO o gestor aprovar um pedido acima de R$ 500,00 e até R$ 5.000,00, O SISTEMA DEVE deixá-lo aguardando o Financeiro; QUANDO o Financeiro aprovar esse pedido, O SISTEMA DEVE marcá-lo como aprovado.
- **R3** — SE um pedido novo tiver valor acima de R$ 5.000,00, ENTÃO O SISTEMA DEVE recusá-lo orientando o processo de adiantamento.
- **R4** — SE o Financeiro tentar aprovar um pedido que o gestor ainda não aprovou, ou o gestor tentar aprovar um pedido que já aguarda o Financeiro, ENTÃO O SISTEMA DEVE recusar a operação sem alterar o pedido.
- **R5** — SE quem aprova for quem solicitou o pedido, ou se a aprovação do Financeiro vier da mesma pessoa que aprovou como gestor, ENTÃO O SISTEMA DEVE recusar a aprovação.
- **R6** — SE a soma das refeições do colaborador na mesma data, incluindo o pedido novo, passar de R$ 120,00, ENTÃO O SISTEMA DEVE recusar o pedido informando quanto já existe e quanto seria o total.
- **R7** — QUANDO duas aprovações do mesmo pedido chegarem ao mesmo tempo, O SISTEMA DEVE efetivar apenas uma e recusar a outra como conflito.
- **R8** — QUANDO gestor ou Financeiro rejeitar um pedido ainda não aprovado (submetido ou aguardando o Financeiro), O SISTEMA DEVE registrar a rejeição.

## Critérios de aceite

Hoje = 20/10/2026. Ana é a solicitante; Bruno é gestor; Carla é do Financeiro.

| ID | Req. | Dado | Quando | Então |
|---|---|---|---|---|
| CA1 | R1 | pedido de Ana, R$ 500,00 | Bruno aprova como gestor | status APROVADA, aprovador gestor = Bruno, decidido por Bruno |
| CA2 | R2 | pedido de Ana, R$ 500,01 | Bruno aprova como gestor | status AGUARDANDO_FINANCEIRO, sem data de decisão |
| CA3 | R2 | CA2 concluído | Carla aprova como Financeiro | status APROVADA, aprovador financeiro = Carla, decidido por Carla |
| CA4 | R3 | — | Ana submete R$ 5.000,01 | recusado (regra de negócio), mensagem cita "adiantamento", nada gravado |
| CA5 | R3 | — | Ana submete R$ 5.000,00 | criado, status SUBMETIDA |
| CA6 | R4 | pedido de R$ 800,00 SUBMETIDO | Carla aprova como Financeiro | recusado (conflito de estado), status continua SUBMETIDA |
| CA7 | R4 | pedido de R$ 800,00 AGUARDANDO_FINANCEIRO | Bruno aprova de novo como gestor | recusado (conflito de estado) |
| CA8 | R5 | pedido de Ana | " ana " aprova como gestor | recusado (regra de negócio); comparação ignora maiúsculas e espaços |
| CA9 | R5 | pedido de R$ 800,00 aprovado por Bruno como gestor | Bruno aprova como Financeiro | recusado (regra de negócio), status continua AGUARDANDO_FINANCEIRO |
| CA10 | R6 | Ana tem refeição de R$ 100,00 em 15/10 | submete refeição de R$ 20,00 em 15/10 | criado (total R$ 120,00, no limite) |
| CA11 | R6 | Ana tem refeição de R$ 100,00 em 15/10 | submete refeição de R$ 20,01 em 15/10 | recusado; mensagem cita R$ 100,00 já existentes e total de R$ 120,01 |
| CA12 | R6 | Ana tem refeição de R$ 100,00 **rejeitada** em 15/10 | submete refeição de R$ 100,00 em 15/10 com outro CNPJ | criado (rejeitadas não contam) |
| CA13 | R6 | Bia tem refeição de R$ 100,00 em 15/10 | Ana submete refeição de R$ 100,00 em 15/10 | criado (o teto é por colaborador) |
| CA14 | R7 | pedido de R$ 300,00 SUBMETIDO | Bruno e Davi aprovam como gestor ao mesmo tempo | exatamente uma aprovação vale; a outra é recusada como conflito; o pedido registra um único aprovador |
| CA15 | R8 | pedido AGUARDANDO_FINANCEIRO | Carla rejeita com motivo | status REJEITADA, motivo registrado |

## Comportamento que NÃO pode mudar

- Prazo de submissão (spec 001, R2 e R3).
- Duplicidade: mesmo colaborador, CNPJ, valor e data, em qualquer status (spec 001, R4).
- Listagem por colaborador (spec 001, R7).
- Formato de erro (Problem Details) e códigos: 400 entrada, 404, 409 conflito de estado, 422 regra.

## Casos de borda e erros

- Exatamente R$ 500,00: só gestor (CA1). Exatamente R$ 5.000,00: aceito (CA5). Exatamente R$ 120,00 no dia: aceito (CA10).
- Aprovação sem informar o papel: entrada inválida (400).
- Pedido de refeição duplicado: a duplicidade é verificada antes do teto, para a
  mensagem de erro ser a correta.
- Pedidos submetidos antes de 01/11 acima de R$ 500,00 seguem a regra nova na aprovação.

## Dúvidas

Nenhuma aberta.

## Decisões

| Data | Pergunta | Decisão | Quem decidiu |
|---|---|---|---|
| 21/10/2026 | O teto de R$ 120 é por colaborador ou por pessoa na mesa? | Por colaborador e por data da despesa | Marina |
| 21/10/2026 | Pedidos rejeitados entram na soma do dia? | Não. Todos os outros status entram. | Marina |
| 21/10/2026 | Acima do teto: recusa ou aprova até o teto? | Recusa o pedido inteiro; aprovação parcial fica fora de escopo | Marina |
| 21/10/2026 | R$ 500,00 exatos precisam do Financeiro? | Não. "Até R$ 500,00" inclui o limite | Marina |
| 21/10/2026 | A API passa a exigir o papel de quem aprova. Quem consome hoje? | Só o front interno, ajustado junto. Papel obrigatório | Marina + tech lead |
| 21/10/2026 | O Financeiro pode rejeitar? | Sim, em qualquer status ainda não aprovado (política, seção 6) | Marina |

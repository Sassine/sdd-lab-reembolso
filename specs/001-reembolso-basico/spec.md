# Spec 001 · Reembolso básico

> Exemplo de spec **ancorada**: a feature já existe, e esta spec continua sendo a
> referência do comportamento. Quando a 002 mudar a aprovação, esta spec precisa ser
> atualizada junto (a spec anda com o código).

- **Origem:** Política de Despesas v2
- **Status:** aprovada · implementada · **R5 e R6 substituídos pela spec 002** (aprovação em dois níveis)
- **Aprovada por:** Marina (PO Finanças) em 02/03/2026

## Problema

Colaboradores pediam reembolso por e-mail e planilha. Pedidos se perdiam, havia
reembolso em dobro da mesma nota e nenhum registro de quem aprovou.

## Fora de escopo

- Integração com a folha de pagamento (status PAGA é marcado por outro processo).
- Anexo da nota fiscal.
- Alçada por valor (virá numa versão futura da política).

## Requisitos

- **R1** — QUANDO um colaborador submeter uma despesa com dados completos, O SISTEMA DEVE registrar o pedido com status SUBMETIDA.
- **R2** — SE a data da despesa estiver no futuro, ENTÃO O SISTEMA DEVE recusar o pedido informando o motivo.
- **R3** — SE a despesa tiver mais de 60 dias corridos, ENTÃO O SISTEMA DEVE recusar o pedido informando o prazo.
- **R4** — SE já existir pedido do mesmo colaborador com mesmo fornecedor, valor e data, ENTÃO O SISTEMA DEVE recusar o pedido como duplicado.
- **R5** — QUANDO um responsável aprovar ou rejeitar um pedido SUBMETIDO, O SISTEMA DEVE registrar a decisão, quem decidiu, quando e o motivo (se houver). *(Desde a spec 002: aprovação segue a alçada e exige o papel; ver `specs/002-politica-v3`.)*
- **R6** — SE alguém tentar decidir um pedido que não está SUBMETIDO, ENTÃO O SISTEMA DEVE recusar a operação sem alterar o pedido. *(Desde a spec 002: rejeição também vale em AGUARDANDO_FINANCEIRO.)*
- **R7** — QUANDO alguém consultar os pedidos de um colaborador, O SISTEMA DEVE listá-los do mais recente para o mais antigo pela data da despesa.

## Critérios de aceite

| ID | Req. | Dado | Quando | Então |
|---|---|---|---|---|
| CA1 | R1 | hoje 20/10/2026 | Ana submete refeição de R$ 85,50 de 17/10 | pedido criado, status SUBMETIDA, valor 85.50 |
| CA2 | R2 | hoje 20/10/2026 | despesa de 21/10 | recusado, motivo cita "futuro" |
| CA3 | R3 | hoje 20/10/2026 | despesa de 22/07 (90 dias) | recusado, motivo cita "Prazo" |
| CA4 | R4 | Ana já tem transporte R$ 42,00 de 19/10 no CNPJ X | mesma submissão de novo | recusado como duplicado, nada novo gravado |
| CA5 | R5 | pedido SUBMETIDO | Bruno aprova | status APROVADA, decididoPor = Bruno |
| CA6 | R6 | pedido já APROVADO | Carla tenta aprovar | recusado, pedido continua aprovado por Bruno |
| CA7 | R7 | Ana tem pedidos de 15/10 e 19/10; Bia tem um | consulta de Ana | 2 pedidos, o de 19/10 primeiro |

## Comportamento que NÃO pode mudar

Não se aplica (primeira versão).

## Casos de borda e erros

- CNPJ com qualquer coisa diferente de 14 dígitos: recusado como entrada inválida.
- Valor zero ou negativo: recusado como entrada inválida.
- Consulta de pedido inexistente: "não encontrado".

## Dúvidas

Nenhuma aberta.

## Decisões

| Data | Pergunta | Decisão | Quem decidiu |
|---|---|---|---|
| 28/02/2026 | Duplicidade considera pedidos rejeitados? | Sim, qualquer status. Para reenviar, o colaborador pede reabertura ao Financeiro. | Marina |

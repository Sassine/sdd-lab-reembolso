# Spec 001 · Reembolso básico

> Exemplo de spec **ancorada**: a feature já existe, e esta spec continua sendo a
> referência do contrato legado v2. A evolução para a Política de Despesas v3 é
> tratada na [spec 002](../002-politica-v3/spec.md), conforme a vigência abaixo
> (a spec anda com o código).

- **Origem:** Política de Despesas v2
- **Status:** aprovada · implementada
- **Aprovada por:** Marina (PO Finanças) em 02/03/2026

## Contexto de vigência e preservação do legado

- Esta spec documenta o contrato v2 dos pedidos submetidos antes de 01/11/2026
  às 00:00, no horário de Brasília (America/Sao_Paulo). Conforme R9 da spec 002,
  somente submissões a partir desse instante, inclusive, seguem a v3,
  independentemente da data da despesa ou da decisão.
- Pedidos anteriores, inclusive os ainda pendentes após o corte e os de valor
  acima de R$ 5.000,00, mantêm as regras v2: decisão única, sem alçada por valor
  e com motivo opcional, inclusive na rejeição. Refeições v2 não consomem o teto
  da v3. Decisões e históricos anteriores são preservados, sem criar etapas ou
  eventos retroativos.
- R5/R6 e CA5/CA6 abaixo descrevem a decisão única v2. Na v3, as alçadas e
  decisões por etapa seguem a spec 002; seu R6 exige histórico consultável
  preservando as etapas anteriores. A proteção contra decisões concorrentes
  prevista em R8 da spec 002 vale para ambas as versões, aceitando no máximo
  uma decisão na disputa e preservando a autoria e o resultado aceitos.
- Esta contextualização preserva as decisões históricas desta spec, sem
  reescrever tasks concluídas nem alterar entradas anteriores de progress.md,
  que permanece append-only.
- O prazo não foi corrigido nesta evolução: o 60º dia continua recusado em
  ambas as versões. A divergência com “até 60 dias” da política publicada
  permanece explícita na spec 002; não há garantia de aderência integral ao prazo.

## Problema

Colaboradores pediam reembolso por e-mail e planilha. Pedidos se perdiam, havia
reembolso em dobro da mesma nota e nenhum registro de quem aprovou.

## Fora de escopo

- Integração com a folha de pagamento (status PAGA é marcado por outro processo).
- Anexo da nota fiscal.
- Alçada por valor na entrega 001 (contrato v2); a alçada da política v3 é
  tratada na spec 002, não está fora do escopo global do serviço.

## Requisitos

- **R1** — QUANDO um colaborador submeter uma despesa com dados completos, O SISTEMA DEVE registrar o pedido com status SUBMETIDA.
- **R2** — SE a data da despesa estiver no futuro, ENTÃO O SISTEMA DEVE recusar o pedido informando o motivo.
- **R3** — SE a despesa tiver mais de 60 dias corridos, ENTÃO O SISTEMA DEVE recusar o pedido informando o prazo.
- **R4** — SE já existir pedido do mesmo colaborador com mesmo fornecedor, valor e data, ENTÃO O SISTEMA DEVE recusar o pedido como duplicado.
- **R5** — No contrato v2, QUANDO um responsável aprovar ou rejeitar um pedido SUBMETIDO, O SISTEMA DEVE registrar a decisão única, quem decidiu, quando e o motivo (se houver).
- **R6** — No contrato v2, SE alguém tentar decidir um pedido que não está SUBMETIDO, ENTÃO O SISTEMA DEVE recusar a operação sem alterar o pedido.
- **R7** — QUANDO alguém consultar os pedidos de um colaborador, O SISTEMA DEVE listá-los do mais recente para o mais antigo pela data da despesa.

## Critérios de aceite

| ID | Req. | Dado | Quando | Então |
|---|---|---|---|---|
| CA1 | R1 | hoje 20/10/2026 | Ana submete refeição de R$ 85,50 de 17/10 | pedido criado, status SUBMETIDA, valor 85.50 |
| CA2 | R2 | hoje 20/10/2026 | despesa de 21/10 | recusado, motivo cita "futuro" |
| CA3 | R3 | hoje 20/10/2026 | despesa de 22/07 (90 dias) | recusado, motivo cita "Prazo" |
| CA4 | R4 | Ana já tem transporte R$ 42,00 de 19/10 no CNPJ X | mesma submissão de novo | recusado como duplicado, nada novo gravado |
| CA5 | R5 | pedido v2 SUBMETIDO | Bruno aprova | status APROVADA, decididoPor = Bruno |
| CA6 | R6 | pedido v2 já APROVADO | Carla tenta aprovar | recusado, pedido continua aprovado por Bruno |
| CA7 | R7 | Ana tem pedidos de 15/10 e 19/10; Bia tem um | consulta de Ana | 2 pedidos, o de 19/10 primeiro |

## Comportamento que NÃO pode mudar

Não se aplica à entrega original (primeira versão). A preservação desse contrato
legado na evolução para a v3 segue R9 da spec 002 e o contexto de vigência acima.

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

# Spec 002 · Política de Despesas v3

- **Origem:** `tickets/TICKET-002-politica-v3.md`; `docs/politica-de-despesas.md`, versão 3, vigência e seções 4.1 a 6.
- **Status:** aprovada
- **Aprovada por:** usuário em 21/10/2026, mediante confirmação explícita

## Problema

O Financeiro precisa aplicar a Política de Despesas v3, vigente a partir de 01/11/2026. Hoje não há teto de refeições nem alçadas por valor, e qualquer pessoa pode aprovar um pedido, inclusive o próprio. A decisão atual guarda apenas um responsável, sem distinguir etapas, e o ticket relata aprovações simultâneas com atribuição incorreta no histórico.

## Fora de escopo

- Integração com pagamentos ou alteração do processo de folha.
- Implementação do processo de adiantamento de Viagens.
- Anexo de nota fiscal.
- Correção do prazo de submissão no 60º dia, a tratar separadamente: esta entrega mantém a recusa a partir do 60º dia, divergindo do texto “até 60 dias” da política.
- Verificação real da identidade, integração com diretório de colaboradores e comprovação externa dos vínculos de gestor e Financeiro. A autorização desta spec é uma simulação restrita ao laboratório, não uma garantia de autorização real.

## Requisitos

R1 a R7 aplicam-se aos pedidos sujeitos à v3 conforme R9. R8 protege também os pedidos anteriores, sem alterar suas regras de alçada.

- **R1** — QUANDO um colaborador submeter uma refeição sujeita à política v3, O SISTEMA DEVE somar seu valor às refeições pendentes e aprovadas desse colaborador na mesma data da despesa, no horário de Brasília, e aceitar a submissão somente se o total não ultrapassar R$ 120,00, reservando o valor aceito. SE o total ultrapassar esse teto, ENTÃO O SISTEMA DEVE recusar integralmente a submissão, explicar o limite e não criar o pedido nem reduzir seu valor. QUANDO uma refeição for rejeitada, O SISTEMA DEVE liberar sua reserva; submissões concorrentes e aprovações também devem respeitar o teto.
- **R2** — QUANDO o gestor imediato aprovar um pedido válido de até R$ 500,00, inclusive, O SISTEMA DEVE concluir sua aprovação sem exigir aprovação do Financeiro.
- **R3** — QUANDO um pedido válido tiver valor acima de R$ 500,00 e até R$ 5.000,00, inclusive, O SISTEMA DEVE exigir aprovação do gestor imediato e, depois, do Financeiro, mantendo o pedido sem aprovação final até a conclusão das duas etapas.
- **R4** — SE uma submissão tiver valor acima de R$ 5.000,00, ENTÃO O SISTEMA DEVE recusá-la como reembolso e informar que o colaborador deve solicitar adiantamento pelo processo de Viagens, sem criar o pedido.
- **R5** — QUANDO alguém tentar aprovar uma etapa, O SISTEMA DEVE avaliar a identidade, o papel e o vínculo declarados nessa decisão como dados verdadeiros apenas para a simulação do laboratório. SE o solicitante tentar aprovar o próprio pedido, faltar identidade, papel ou vínculo necessário, a pessoa declarada não tiver a atribuição exigida, ou o aprovador do gestor tentar também aprovar como Financeiro, ENTÃO O SISTEMA DEVE recusar a aprovação com explicação, sem alterar o pedido nem suas decisões anteriores e sem selecionar substituto automaticamente.
- **R6** — QUANDO uma etapa de aprovação for concluída, O SISTEMA DEVE registrar e permitir consultar a etapa, quem a aprovou e quando, preservando os registros das etapas anteriores.
- **R7** — QUANDO o responsável declarado pela etapa atual rejeitar um pedido ainda sem aprovação final, informando motivo não vazio, O SISTEMA DEVE registrar a rejeição, a etapa, o responsável, o momento e o motivo, preservar as aprovações anteriores e impedir a aprovação posterior do pedido rejeitado. ENQUANTO aguardar gestor, somente o gestor imediato declarado pode rejeitar; ENQUANTO aguardar Financeiro, somente pessoa declarada do Financeiro pode rejeitar. SE faltarem os dados necessários, a pessoa for o solicitante, não tiver a atribuição da etapa atual ou for a mesma que aprovou como gestor ao tentar rejeitar na etapa financeira, ENTÃO O SISTEMA DEVE recusar a rejeição com explicação e sem alterar o pedido ou seu histórico.
- **R8** — SE decisões concorrentes disputarem a mesma etapa pendente de um pedido, ENTÃO O SISTEMA DEVE aceitar no máximo uma delas, informar conflito às demais e preservar no pedido e no histórico a autoria e o resultado da decisão aceita.
- **R9** — QUANDO um pedido for submetido a partir de 01/11/2026 às 00:00, no horário de Brasília, O SISTEMA DEVE aplicar a v3, independentemente da data da despesa. ENQUANTO um pedido submetido antes desse instante estiver pendente, O SISTEMA DEVE manter suas regras anteriores, inclusive para valores acima de R$ 5.000,00, sem consumir o teto de refeições da v3. O SISTEMA DEVE preservar decisões e históricos anteriores sem criar etapas retroativas, mantendo a proteção de R8 contra decisões concorrentes.

O histórico dos pedidos da v3 contém apenas aprovações e rejeições aceitas, identificadas por etapa, responsável, momento e motivo quando houver. Submissão e tentativas recusadas não geram eventos nesse histórico; os dados do pedido continuam disponíveis para consulta.

## Critérios de aceite

Os exemplos usam identificadores fictícios. Identidade, papel e vínculo de gestor ou Financeiro são declarados a cada decisão, sem comprovação real; menções a gestor e Financeiro nos exemplos representam essas declarações. Salvo indicação contrária, os pedidos são válidos pelas regras preservadas e foram submetidos a partir de 01/11/2026, sujeitos à v3.

| ID | Requisito | Dado | Quando | Então |
|---|---|---|---|---|
| CA1 | R1 | Colaborador A sem outra refeição em 02/11/2026 | A submete refeição de R$ 120,00 nessa data | O pedido é criado como submetido, com valor integral de R$ 120,00 reservado; a aprovação do gestor continua necessária |
| CA2 | R1 | Colaborador A sem outra refeição em 02/11/2026 | A submete refeição de R$ 120,01 nessa data | A submissão é recusada com explicação do teto de R$ 120,00; nenhum pedido ou reserva é criado e não há redução automática do valor |
| CA3 | R2, R6 | Pedido de transporte de R$ 500,00 do colaborador A, com gestor imediato B | B aprova em 03/11/2026 às 10:00, horário de Brasília | O pedido fica aprovado, sem etapa financeira pendente; a consulta identifica a aprovação do gestor por B nesse momento |
| CA4 | R3, R6 | Pedido de hospedagem de R$ 500,01 do colaborador A | O gestor B aprova em 03/11/2026 às 10:00 e, depois, o Financeiro C aprova às 11:00 | Após B, falta aprovação financeira; após C, o pedido fica aprovado e a consulta mantém as duas etapas, B às 10:00 e C às 11:00 |
| CA5 | R3 | Pedido de hospedagem de R$ 5.000,00 ainda sem aprovação do gestor | O Financeiro C tenta aprovar antes do gestor | A operação é recusada por etapa fora de ordem; o pedido permanece sem aprovação e nenhuma aprovação financeira é registrada |
| CA6 | R3 | Pedido de hospedagem de R$ 5.000,00 | B aprova como gestor e depois C, pessoa distinta, aprova como Financeiro | O pedido fica aprovado somente após C; o valor de R$ 5.000,00 não é recusado pelo limite de reembolso |
| CA7 | R4 | Colaborador A apresenta despesa de hospedagem de R$ 5.000,01 | A submete o pedido | A submissão é recusada, nenhum pedido é criado e a explicação orienta solicitar adiantamento pelo processo de Viagens |
| CA8 | R5 | A solicita transporte de R$ 100,00 e também possui atribuição de gestor | A tenta aprovar o próprio pedido | A operação é recusada por autoaprovação; o pedido continua pendente e sem aprovação registrada |
| CA9 | R5 | Hospedagem de R$ 800,00 solicitada por A e aprovada pelo gestor B; B também possui atribuição financeira | B tenta aprovar a etapa financeira | A operação é recusada por falta de separação entre aprovadores; a aprovação do gestor B permanece e a financeira continua pendente |
| CA10 | R5 | Transporte de R$ 100,00 de A, cujo gestor imediato é B; D não é esse gestor | D tenta aprovar como gestor | A operação é recusada por ausência da atribuição exigida, sem alterar o pedido nem registrar aprovação |
| CA11 | R7 | Hospedagem de R$ 800,00 já aprovada pelo gestor B e aguardando Financeiro | C, do Financeiro, rejeita em 03/11/2026 às 11:00 por “Despesa sem justificativa de serviço” | O pedido fica rejeitado; a consulta preserva a aprovação de B e identifica C, o momento e o motivo da rejeição; nova aprovação é recusada |
| CA12 | R7 | Transporte de R$ 100,00 aguardando gestor | B rejeita por “Despesa particular” | O pedido fica rejeitado, com B, o momento e o motivo registrados, sem aprovação registrada |
| CA13 | R7 | Transporte de R$ 100,00 aguardando gestor | B tenta rejeitar sem motivo | A rejeição é recusada com explicação sobre o motivo obrigatório; pedido e decisões anteriores não mudam |
| CA14 | R8 | Transporte de R$ 100,00 de A aguardando gestor B | Duas aprovações de B disputam simultaneamente a mesma etapa | Apenas uma aprovação é aceita; a outra informa conflito; o pedido fica aprovado por B e há um único registro da etapa, correspondente à operação aceita |
| CA15 | R8 | Transporte de R$ 100,00 de A aguardando gestor B | Uma aprovação e uma rejeição com motivo, ambas de B, disputam simultaneamente a etapa | Apenas uma decisão é aceita; a outra informa conflito; o estado, a autoria, o momento e o motivo, quando aplicável, correspondem exclusivamente à decisão aceita |
| CA16 | R1 | A tem refeição pendente de R$ 50,00 e aprovada de R$ 30,00 em 02/11/2026 | A submete outra refeição de R$ 40,00, com nota distinta, nessa data | O novo pedido é criado e o total reservado passa a R$ 120,00; sua aprovação posterior mantém o total em R$ 120,00, sem contar o mesmo pedido duas vezes |
| CA17 | R1 | A tem refeição pendente de R$ 80,00 em 02/11/2026 | A tenta submeter mais R$ 40,01 nessa data | A nova submissão é recusada integralmente; o pedido anterior permanece pendente e o total reservado continua em R$ 80,00 |
| CA18 | R1, R7 | A tem refeição de R$ 80,00 em 02/11/2026 | O gestor rejeita com motivo e A submete outra refeição de R$ 120,00 nessa data, com nota distinta | A rejeição libera R$ 80,00; o novo pedido é criado com reserva de R$ 120,00 e o rejeitado permanece registrado sem consumir o teto |
| CA19 | R1 | A já tem R$ 120,00 reservados em refeições de 02/11/2026 | B submete R$ 120,00 em refeições de 02/11/2026 e A submete R$ 120,00 em refeições de 03/11/2026, em datas de submissão válidas | Ambos os pedidos são criados, pois colaborador diferente ou data da despesa diferente têm totais independentes |
| CA20 | R1 | A tem R$ 80,00 reservados em refeições de 02/11/2026 | Duas submissões de R$ 30,00 de A, com notas distintas para essa data, disputam simultaneamente o saldo | Apenas uma é aceita; a outra é recusada por exceder o teto; somente um novo pedido é criado e o total reservado fica em R$ 110,00 |
| CA21 | R5 | Transporte de R$ 100,00 de A aguardando gestor | B tenta aprovar sem declarar o vínculo de gestor imediato de A | A decisão é recusada com explicação do vínculo ausente; o pedido continua pendente e nenhuma aprovação ou substituição automática é registrada |
| CA22 | R5, R6 | Hospedagem de R$ 800,00 de A aprovada por B, declarado gestor imediato naquele momento | C aprova a etapa financeira declarando identidade e atribuição financeira; o gestor imediato agora declarado para A é D | O pedido fica aprovado por C na etapa financeira, e a aprovação anterior permanece atribuída a B, sem reescrever o histórico para D |
| CA23 | R9, R3 | Hospedagem de R$ 800,00 com despesa de 31/10/2026 | A submete em 01/11/2026 às 00:00, horário de Brasília | O pedido é criado sob a v3 e exige gestor e depois Financeiro, apesar de a despesa ser anterior à vigência |
| CA24 | R9 | Hospedagem de R$ 5.000,01 submetida em 31/10/2026 às 23:59, horário de Brasília, ainda pendente | O responsável aprova em 02/11/2026 conforme as regras anteriores | O pedido fica aprovado em decisão única pelas regras anteriores, sem recusa retroativa pelo valor e sem criar etapa financeira |
| CA25 | R9, R1 | A tem refeição de R$ 100,00 de 31/10/2026 submetida antes da vigência | A submete outra refeição de R$ 120,00 para a mesma data, com nota distinta, em 01/11/2026 | O novo pedido é criado sob a v3 com reserva de R$ 120,00; o pedido antigo permanece sob a v2 e não consome esse teto |
| CA26 | R9 | Pedido de R$ 800,00 já aprovado antes de 01/11/2026 com um único responsável registrado | O pedido é consultado após a vigência | O resultado mantém o estado e a autoria anteriores, sem inventar aprovação financeira ou histórico por etapas |
| CA27 | R9, R8 | Pedido de R$ 5.000,01 submetido antes da vigência, ainda pendente | Dois responsáveis tentam aprová-lo simultaneamente em 02/11/2026 | Apenas uma aprovação é aceita pelas regras anteriores e a outra informa conflito; estado e histórico identificam exclusivamente a decisão aceita |
| CA28 | R7 | Transporte de R$ 100,00 de A aguardando gestor B | C, declarado Financeiro, tenta rejeitar com motivo “Despesa particular” | A rejeição é recusada por não ser a etapa financeira; o pedido permanece pendente e nenhum evento é acrescentado ao histórico |
| CA29 | R7 | Transporte de R$ 100,00 de A aguardando gestor; A declara ser o gestor imediato | A tenta rejeitar o próprio pedido com motivo “Cancelar despesa” | A rejeição é recusada por decisão sobre o próprio pedido; estado e histórico permanecem inalterados |
| CA30 | R7 | Hospedagem de R$ 800,00 de A já aprovada pelo gestor B e aguardando Financeiro; B também declara atribuição financeira | B tenta rejeitar com motivo “Despesa particular” | A rejeição é recusada por falta de separação de pessoas; a aprovação do gestor é preservada e o pedido continua aguardando Financeiro |
| CA31 | R6, R7 | Transporte de R$ 100,00 de A recém-submetido, aguardando gestor B | C, sem vínculo de gestor, tenta aprovar e é recusado; depois B rejeita por “Despesa particular” | O histórico consultável contém somente a rejeição aceita, com etapa do gestor, B, momento e motivo; submissão e tentativa recusada de C não aparecem como eventos |
| CA32 | R7 | Transporte de R$ 100,00 aguardando gestor B | B tenta rejeitar com motivo contendo apenas espaços | A rejeição é recusada por motivo vazio; o pedido permanece pendente e o histórico não muda |

## Comportamento que NÃO pode mudar

- Despesas válidas continuam sendo registradas como pedidos submetidos antes da decisão.
- Categorias continuam sendo refeição, transporte, hospedagem e outros.
- Despesas futuras continuam recusadas; datas e momentos são considerados no horário de Brasília.
- Despesas de até 59 dias continuam aceitas quanto ao prazo; as de 60 dias ou mais continuam recusadas, inclusive na v3. Exemplo: em 01/11/2026, despesa de 03/09/2026 (59 dias) atende ao prazo; despesas de 02/09/2026 (60 dias) e 01/09/2026 (61 dias) são recusadas. A divergência com a política publicada será corrigida separadamente.
- A duplicidade continua considerando colaborador, CNPJ do fornecedor, valor e data da despesa, independentemente da categoria e do estado do pedido, inclusive rejeitado.
- CNPJ diferente de 14 dígitos, valores zero ou negativos e dados obrigatórios ausentes continuam sendo recusados como entrada inválida.
- A consulta continua restrita aos pedidos do colaborador informado, ordenados pela data da despesa da mais recente para a mais antiga; em empate, o pedido registrado mais recentemente vem primeiro.
- Pedido inexistente continua sendo informado como não encontrado.
- Pedidos definitivamente aprovados ou rejeitados não podem receber nova decisão; uma tentativa recusada não altera a decisão existente.
- A regra atual de decisão única é substituída apenas onde necessário para as duas etapas previstas na v3, sem permitir repetir uma etapa já concluída. Pedidos submetidos antes da vigência mantêm suas regras anteriores, com a proteção de concorrência de R8.

## Casos de borda e erros

- Limites inclusivos: R$ 120,00 atende ao teto de refeições; R$ 500,00 exige só gestor; R$ 500,01 exige gestor e Financeiro; R$ 5.000,00 permanece reembolso; R$ 5.000,01 é recusado como reembolso.
- Aprovação financeira antecipada, repetição de etapa, decisão em pedido encerrado e decisão concorrente perdedora devem informar o conflito sem modificar o pedido ou seu histórico.
- Autoaprovação, aprovador sem atribuição necessária e repetição da mesma pessoa nas duas alçadas devem explicar a violação, sem registrar aprovação.
- A simulação usa identidade, papel e vínculo declarados no momento da decisão, sem verificar a identidade real. Dados necessários ausentes ou impedimento por separação de pessoas bloqueiam a decisão; não há substituição automática. Alterações posteriores nas declarações de vínculo não reescrevem a autoria histórica.
- Rejeição sem motivo, ou com motivo contendo apenas espaços, não pode encerrar o pedido.
- Rejeição obedece à etapa atual e às mesmas restrições de identidade declarada, vínculo, decisão sobre o próprio pedido e separação de pessoas. O Financeiro não pode antecipar a rejeição nem intervir em pedido que aguarda gestor, inclusive até R$ 500,00.
- O histórico contém somente decisões aceitas; submissões e tentativas recusadas não acrescentam eventos. Dados necessários ausentes não alteram o pedido nem o histórico.
- Refeições são acumuladas por colaborador e data da despesa, independentemente do fornecedor; pendentes e aprovadas consomem o teto, rejeitadas não consomem. Aprovar um pedido já reservado não consome seu valor novamente.
- Submissões concorrentes de refeições não podem ultrapassar o teto acumulado; a submissão excedente é recusada integralmente, sem criar pedido ou reserva.
- A data da submissão, e não a da despesa ou decisão, define a versão da política. O corte é 01/11/2026 às 00:00 no horário de Brasília; pedidos antigos não são reenquadrados e não consomem o teto da v3.

## Dúvidas

Nenhuma aberta.

## Decisões

| Data | Pergunta | Decisão | Quem decidiu |
|---|---|---|---|
| 21/10/2026 | D1 e D2 — Abrangência, consumo e excedente do teto de refeições | Opção A: somar por colaborador e data da despesa, no horário de Brasília; reservar na submissão considerando pendentes e aprovadas; rejeitadas liberam o valor. Recusar integralmente a submissão excedente, sem criar pedido ou reduzir valor. Submissões simultâneas e aprovações devem respeitar R$ 120,00. | Usuário |
| 21/10/2026 | D3 e D4 — Identidade, atribuição e impedimento dos aprovadores | Opção B: simulação restrita ao laboratório, com identidade, papel e vínculo declarados em cada decisão e tratados como verdadeiros, sem verificação real. Aplicar autoaprovação e separação de pessoas sobre esses dados; ausência de vínculo ou impedimento bloqueia a decisão, sem substituição automática. Declarações posteriores não alteram decisões anteriores. Isso não comprova autorização real nem impede personificação. | Usuário |
| 21/10/2026 | D5 — Vigência e pedidos anteriores | Opção A: aplicar a v3 às submissões a partir de 01/11/2026 às 00:00, no horário de Brasília, independentemente da data da despesa. Pedidos anteriores mantêm as regras antigas, inclusive pendentes e valores acima de R$ 5.000,00; decisões e históricos não são reescritos nem recebem etapas inventadas. Refeições da v2 não consomem o teto da v3. A proteção contra decisões concorrentes vale para ambas as versões. | Usuário |
| 21/10/2026 | D6 e D8 — Rejeição e abrangência do histórico | Opção A: rejeitar somente pela etapa atual, com motivo não vazio; aplicar proibição de decisão sobre o próprio pedido e exigir pessoa distinta do gestor também na rejeição financeira. O histórico consultável contém apenas decisões aceitas com etapa, responsável, momento e motivo quando houver; não inclui submissão ou tentativas recusadas. Históricos anteriores ficam intactos. | Usuário |
| 21/10/2026 | D7 — Divergência no 60º dia | Opção B: manter nesta entrega a recusa de despesas a partir do 60º dia, inclusive na v3, e tratar a correção separadamente. A divergência com o prazo publicado permanece explícita e fora de escopo; esta entrega não garante conformidade integral com esse trecho da política. | Usuário |
| 21/10/2026 | Aprovação da spec esclarecida | Aprovada explicitamente após apresentação das decisões e das limitações de autorização simulada e prazo de submissão. | Usuário |

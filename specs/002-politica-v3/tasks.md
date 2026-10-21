# Tarefas 002 · Política de Despesas v3

- **Spec e plano:** aprovados; referências em `spec.md` e `plan.md` desta pasta.
- **Status das tarefas:** proposta para revisão humana; implementação não iniciada.

Uma tarefa por sessão, com teste antes da implementação, revisão, gate completo e commit próprio. `[P]` indica independência técnica na mesma fase; não autoriza ignorar a regra de uma tarefa por sessão. Não publicar a feature parcialmente integrada como pronta.

Os caminhos em `Arquivos:` delimitam os arquivos de implementação, teste e documentação da tarefa. A atualização do checkbox e o acréscimo ao `specs\002-politica-v3\progress.md` são operações comuns do orquestrador, sequenciais e fora do trabalho paralelo. Não criar outro log nem reescrever tarefas durante a execução.

As verificações usam somente o gate completo por tarefa: ele já executa todos os testes e o SddCheck. Os testes específicos e seus critérios estão mapeados ao final. Não alterar o relógio global dos testes v2, enfraquecer testes existentes ou adicionar dependências.

## Fase 1 · Fundação

- [x] T001 [R9] Defina a vigência pelo instante capturado da submissão, preservando o prazo atual.
      Arquivos: `src\main\java\com\orbe\reembolso\dominio\VersaoPolitica.java`, `src\main\java\com\orbe\reembolso\servico\PoliticaDeVigencia.java`, `src\main\java\com\orbe\reembolso\servico\PoliticaDePrazo.java`, `src\test\java\com\orbe\reembolso\servico\PoliticaDeVigenciaTest.java`, `src\test\java\com\orbe\reembolso\servico\PoliticaDePrazoTest.java`
      Verificação: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\verify.ps1`

- [x] T002 [R2,R3,R6,R7] Modele as transições por etapa com histórico de decisões aceitas no agregado.
      Arquivos: `src\main\java\com\orbe\reembolso\dominio\Reembolso.java`, `src\main\java\com\orbe\reembolso\dominio\StatusReembolso.java`, `src\main\java\com\orbe\reembolso\dominio\EtapaAprovacao.java`, `src\main\java\com\orbe\reembolso\dominio\PapelDeclarado.java`, `src\main\java\com\orbe\reembolso\dominio\DecisaoReembolso.java`, `src\main\java\com\orbe\reembolso\servico\DecidirReembolso.java`, `src\test\java\com\orbe\reembolso\dominio\ReembolsoTest.java`
      Verificação: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\verify.ps1`

- [x] T003 [R6,R8,R9] Persista o histórico ordenado sob controle otimista de versão do pedido.
      Arquivos: `src\main\java\com\orbe\reembolso\dominio\Reembolso.java`, `src\main\java\com\orbe\reembolso\dominio\DecisaoReembolso.java`, `src\main\java\com\orbe\reembolso\servico\ReembolsoRepository.java`, `src\test\java\com\orbe\reembolso\servico\ReembolsoPersistenciaTest.java`
      Verificação: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\verify.ps1`

- [x] T004 [R5,R7] Centralize a autorização declarada por etapa sem autenticação real ou substituição automática.
      Arquivos: `src\main\java\com\orbe\reembolso\servico\PoliticaDeAutorizacao.java`, `src\test\java\com\orbe\reembolso\servico\PoliticaDeAutorizacaoTest.java`
      Verificação: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\verify.ps1`

## Fase 2 · Trabalho independente

- [ ] T005 [P] [R2,R3,R4,R5] Implemente a política de aprovação por alçada reutilizando a autorização declarada.
      Arquivos: `src\main\java\com\orbe\reembolso\servico\PoliticaDeAprovacao.java`, `src\test\java\com\orbe\reembolso\servico\PoliticaDeAprovacaoTest.java`
      Verificação: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\verify.ps1`

- [ ] T006 [P] [R7] Implemente a rejeição pela etapa atual com motivo não branco.
      Arquivos: `src\main\java\com\orbe\reembolso\servico\PoliticaDeRejeicao.java`, `src\test\java\com\orbe\reembolso\servico\PoliticaDeRejeicaoTest.java`
      Verificação: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\verify.ps1`

- [ ] T007 [P] [R1] Implemente a validação monetária do teto diário sem contar novamente a reserva aprovada.
      Arquivos: `src\main\java\com\orbe\reembolso\servico\PoliticaDeRefeicao.java`, `src\test\java\com\orbe\reembolso\servico\PoliticaDeRefeicaoTest.java`
      Verificação: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\verify.ps1`

- [ ] T008 [P] [R1] Disponibilize a âncora diária com criação transacional independente e bloqueio persistente.
      Arquivos: `src\main\java\com\orbe\reembolso\dominio\DiaRefeicao.java`, `src\main\java\com\orbe\reembolso\dominio\ChaveDiaRefeicao.java`, `src\main\java\com\orbe\reembolso\servico\DiaRefeicaoRepository.java`, `src\main\java\com\orbe\reembolso\servico\ControleDeTetoDiario.java`, `src\test\java\com\orbe\reembolso\servico\ControleDeTetoDiarioTest.java`
      Verificação: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\verify.ps1`

## Fase 3 · Integração

- [ ] T009 [R2,R3,R4,R6,R9] Integre a aprovação v3 ao contrato aditivo da API sem reenquadrar decisões v2.
      Arquivos: `src\main\java\com\orbe\reembolso\api\DecisaoRequest.java`, `src\main\java\com\orbe\reembolso\api\ReembolsoController.java`, `src\main\java\com\orbe\reembolso\api\ReembolsoResponse.java`, `src\main\java\com\orbe\reembolso\api\DecisaoReembolsoResponse.java`, `src\main\java\com\orbe\reembolso\servico\ReembolsoService.java`, `src\test\java\com\orbe\reembolso\suporte\RelogioPoliticaV3DeTeste.java`, `src\test\java\com\orbe\reembolso\api\ReembolsoPoliticaV3ApiTest.java`
      Verificação: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\verify.ps1`

- [ ] T010 [R5,R6] Comprove as recusas de autorização v3 com preservação da autoria histórica.
      Arquivos: `src\main\java\com\orbe\reembolso\api\DecisaoRequest.java`, `src\main\java\com\orbe\reembolso\servico\ReembolsoService.java`, `src\main\java\com\orbe\reembolso\servico\PoliticaDeAutorizacao.java`, `src\main\java\com\orbe\reembolso\servico\PoliticaDeAprovacao.java`, `src\test\java\com\orbe\reembolso\api\ReembolsoPoliticaV3ApiTest.java`
      Verificação: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\verify.ps1`

- [ ] T011 [R6,R7] Integre a rejeição v3 com registro exclusivo de decisões aceitas.
      Arquivos: `src\main\java\com\orbe\reembolso\api\ReembolsoController.java`, `src\main\java\com\orbe\reembolso\servico\ReembolsoService.java`, `src\main\java\com\orbe\reembolso\servico\PoliticaDeRejeicao.java`, `src\test\java\com\orbe\reembolso\api\ReembolsoPoliticaV3ApiTest.java`
      Verificação: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\verify.ps1`

- [ ] T012 [R1,R7] Integre a reserva de refeições por colaborador/data sob bloqueio da âncora diária.
      Arquivos: `src\main\java\com\orbe\reembolso\servico\ReembolsoService.java`, `src\main\java\com\orbe\reembolso\servico\ReembolsoRepository.java`, `src\main\java\com\orbe\reembolso\dominio\Reembolso.java`, `src\test\java\com\orbe\reembolso\api\ReembolsoPoliticaV3ApiTest.java`
      Verificação: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\verify.ps1`

- [ ] T013 [R1,R3,R9] Comprove o corte exato de vigência com preservação dos pedidos legados.
      Arquivos: `src\main\java\com\orbe\reembolso\servico\ReembolsoService.java`, `src\main\java\com\orbe\reembolso\servico\PoliticaDeVigencia.java`, `src\main\java\com\orbe\reembolso\api\ReembolsoResponse.java`, `src\test\java\com\orbe\reembolso\api\ReembolsoVigenciaApiTest.java`
      Verificação: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\verify.ps1`

## Fase 4 · Acabamento

- [ ] T014 [R3,R6,R7,R8,R9] Comprove a exclusividade de decisões concorrentes nas etapas v3 e na decisão única v2.
      Arquivos: `src\main\java\com\orbe\reembolso\dominio\Reembolso.java`, `src\main\java\com\orbe\reembolso\servico\ReembolsoService.java`, `src\main\java\com\orbe\reembolso\api\TratadorDeErros.java`, `src\test\java\com\orbe\reembolso\servico\ReembolsoConcorrenciaTest.java`
      Verificação: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\verify.ps1`

- [ ] T015 [R1] Comprove a reserva concorrente de refeições inclusive no primeiro pedido do dia.
      Arquivos: `src\main\java\com\orbe\reembolso\servico\ControleDeTetoDiario.java`, `src\main\java\com\orbe\reembolso\servico\DiaRefeicaoRepository.java`, `src\main\java\com\orbe\reembolso\servico\ReembolsoRepository.java`, `src\main\java\com\orbe\reembolso\servico\ReembolsoService.java`, `src\main\java\com\orbe\reembolso\api\TratadorDeErros.java`, `src\test\java\com\orbe\reembolso\servico\ControleDeTetoDiarioTest.java`, `src\test\java\com\orbe\reembolso\servico\ReembolsoConcorrenciaTest.java`
      Verificação: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\verify.ps1`

- [ ] T016 [R5,R7,R8,R9] Uniformize os erros novos em Problem Details sem restringir o contrato v2.
      Arquivos: `src\main\java\com\orbe\reembolso\api\TratadorDeErros.java`, `src\main\java\com\orbe\reembolso\api\DecisaoRequest.java`, `src\test\java\com\orbe\reembolso\api\ReembolsoPoliticaV3ApiTest.java`, `src\test\java\com\orbe\reembolso\api\ReembolsoVigenciaApiTest.java`, `src\test\java\com\orbe\reembolso\servico\ReembolsoConcorrenciaTest.java`
      Verificação: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\verify.ps1`

- [ ] T017 [R6,R9] Contextualize a spec 001 como contrato legado v2 com referência à política v3.
      Arquivos: `specs\001-reembolso-basico\spec.md`
      Verificação: `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\verify.ps1`

## Dependências e limites de execução

Dependências abaixo são explícitas; referências transitivas não precisam ser repetidas. Uma tarefa marcada `[P]` só compartilha leitura de fundação com as outras `[P]`, nunca escrita dos arquivos listados.

| Tarefa | Depende de | Limite e resultado verificável |
|---|---|---|
| T001 | Nenhuma | Provar imediatamente antes/no corte, conversão para Brasília e uma única referência temporal; caracterizar recusa no 60º dia sem mudar 59/61/futuro |
| T002 | T001 | Definir comando e enums; preservar métodos v2; testar transições, rejeição após gestor e ordem imutável dos eventos em memória. Não implementar autorização no domínio |
| T003 | T002 | Mapear eventos, índice de consulta e @Version; carregar histórico em busca/listagem antes de sair da transação; provar rollback conjunto e ausência de eventos legados. Nenhuma migração durável |
| T004 | T002 | Validar identidade, papel, vínculo, gestor declarado, decisão sobre o próprio pedido e separação de pessoas para aprovação/rejeição. Política não consulta cadastro nem DTO |
| T005 | T004 | Validar limite de submissão acima de R$ 5.000,00 com orientação a Viagens; selecionar alçada nos limites R$ 500,00/R$ 500,01/R$ 5.000,00; não duplicar autorização |
| T006 | T004 | Reutilizar autorização; exigir etapa atual e motivo com conteúdo; não alterar regras v2 ou persistir eventos na política |
| T007 | Nenhuma | Testar R$ 120,00/R$ 120,01, total acumulado e aprovação sem dupla contagem; sem banco ou HTTP |
| T008 | Nenhuma | Implementar chave única, criação REQUIRES_NEW com flush e confirmação de colisão específica fora da transação revertida; lock PESSIMISTIC_WRITE. Testar que outras falhas são propagadas |
| T009 | T001, T003, T005 | Capturar instante único no comando; encaminhar declaração v3; exigir etapa; persistir evento/estado atomicamente com flush; retornar política, etapa e histórico. Criar contexto de teste v3 sem alterar RelogioDeTeste |
| T010 | T009 | Acrescentar testes de autorização de ponta a ponta antes de ajustar a ligação das políticas. Declaração contraditória não é regularizada automaticamente; testar B preservado após declarar gestor D |
| T011 | T006, T010 | Exigir responsável da etapa, motivo e separação; nenhuma tentativa recusada gera evento. Testar rejeição financeira preservando aprovação anterior |
| T012 | T007, T008, T011 | Somar somente refeições v3 em estados consumidores; READ_COMMITTED; garantir âncora antes de escrita/lock externo, bloquear, reler e validar. Rejeição libera reserva; aprovação não duplica consumo; duplicidade continua incluindo rejeitados |
| T013 | T012 | Fixtures v2 com timestamp anterior, Clock próprio para corte e decisões posteriores; não alterar relógio global, reenquadrar pedidos ou inventar eventos |
| T014 | T013 | Transações independentes, barreiras com timeout, um vencedor, um conflito e consulta nova após rollback. Provar disputa no gestor, no Financeiro, entre aprovação/rejeição e em v2; nunca reinterpretar etapa |
| T015 | T014 | Reutilizar arquivo de concorrência criado em T014; provar saldo parcial, dia vazio, corrida de criação da âncora e liberação de saldo. Não confiar em mutex local ou sleeps |
| T016 | T015 | Tratar JSON/enum inválidos, exceções concretas de versão/lock e ausência contextual v3; manter ausência de campos novos e motivo válidos na v2. Não converter falha SQL genérica em 409/duplicidade |
| T017 | T016 | Delimitar R5/R6, CA5/CA6 e alçada fora de escopo da 001 como regras v2; preservar decisões históricas, tasks concluídas e progress append-only |

As quatro tarefas `[P]` da fase 2 são T005, T006, T007 e T008: não há dependência entre elas nem arquivo de escrita em comum. T005/T006 dependem apenas de fundação já concluída. As demais tarefas são sequenciais; em especial, os testes de API e concorrência são ampliados em arquivos compartilhados.

## Cobertura requisito → tarefa

| Requisito | Tarefas |
|---|---|
| R1 | T007, T008, T012, T013, T015 |
| R2 | T002, T005, T009 |
| R3 | T002, T005, T009, T013, T014 |
| R4 | T005, T009 |
| R5 | T004, T005, T010, T016 |
| R6 | T002, T003, T009, T010, T011, T014, T017 |
| R7 | T002, T004, T006, T011, T012, T014, T016 |
| R8 | T003, T014, T016 |
| R9 | T001, T003, T009, T013, T014, T016, T017 |

## Cobertura critério → tarefa → teste

Métodos mantêm os nomes previstos no plano. `ReembolsoPoliticaV3ApiTest` e `ReembolsoVigenciaApiTest` ficam em `src\test\java\com\orbe\reembolso\api`; `ReembolsoConcorrenciaTest` fica em `src\test\java\com\orbe\reembolso\servico`. A presença nesta tabela não significa teste implementado ou aprovado.

| Critério | Tarefa responsável | Teste |
|---|---|---|
| CA1 | T012 | `ReembolsoPoliticaV3ApiTest#aceitaRefeicaoNoTeto` |
| CA2 | T012 | `ReembolsoPoliticaV3ApiTest#recusaRefeicaoAcimaDoTeto` |
| CA3 | T009 | `ReembolsoPoliticaV3ApiTest#aprova500SomenteComGestor` |
| CA4 | T009 | `ReembolsoPoliticaV3ApiTest#aprova500e01EmDuasEtapas` |
| CA5 | T009 | `ReembolsoPoliticaV3ApiTest#recusaFinanceiroAntesDoGestor` |
| CA6 | T009 | `ReembolsoPoliticaV3ApiTest#aprova5000EmDuasEtapas` |
| CA7 | T009 | `ReembolsoPoliticaV3ApiTest#recusa5000e01OrientandoViagens` |
| CA8 | T010 | `ReembolsoPoliticaV3ApiTest#recusaAutoaprovacao` |
| CA9 | T010 | `ReembolsoPoliticaV3ApiTest#recusaMesmoAprovadorNoFinanceiro` |
| CA10 | T010 | `ReembolsoPoliticaV3ApiTest#recusaQuemNaoEOGestorDeclarado` |
| CA11 | T011 | `ReembolsoPoliticaV3ApiTest#rejeitaNoFinanceiroPreservandoGestor` |
| CA12 | T011 | `ReembolsoPoliticaV3ApiTest#rejeitaNoGestorComMotivo` |
| CA13 | T011 | `ReembolsoPoliticaV3ApiTest#recusaRejeicaoSemMotivo` |
| CA14 | T014 | `ReembolsoConcorrenciaTest#aceitaUmaDasAprovacoesDaMesmaEtapa` |
| CA15 | T014 | `ReembolsoConcorrenciaTest#resolveDisputaEntreAprovacaoERejeicao` |
| CA16 | T012 | `ReembolsoPoliticaV3ApiTest#somaPendentesEAprovadasSemContarDuasVezes` |
| CA17 | T012 | `ReembolsoPoliticaV3ApiTest#recusaExcedenteSomadoSemAlterarAnterior` |
| CA18 | T012 | `ReembolsoPoliticaV3ApiTest#rejeicaoLiberaTetoSemLiberarDuplicidade` |
| CA19 | T012 | `ReembolsoPoliticaV3ApiTest#separaTetoPorColaboradorEData` |
| CA20 | T015 | `ReembolsoConcorrenciaTest#serializaRefeicoesComSaldoParcial` |
| CA21 | T010 | `ReembolsoPoliticaV3ApiTest#recusaVinculoAusente` |
| CA22 | T010 | `ReembolsoPoliticaV3ApiTest#mudancaDeclaradaDeGestorNaoReescreveHistorico` |
| CA23 | T013 | `ReembolsoVigenciaApiTest#aplicaV3NaSubmissaoExatamenteNoCorte` |
| CA24 | T013 | `ReembolsoVigenciaApiTest#aprovaLegadoAcimaDe5000DepoisDoCorte` |
| CA25 | T013 | `ReembolsoVigenciaApiTest#refeicaoLegadaNaoConsomeTetoV3` |
| CA26 | T013 | `ReembolsoVigenciaApiTest#consultaLegadoSemInventarEtapas` |
| CA27 | T014 | `ReembolsoConcorrenciaTest#protegeDecisaoUnicaLegada` |
| CA28 | T011 | `ReembolsoPoliticaV3ApiTest#financeiroNaoRejeitaAntesDoGestor` |
| CA29 | T011 | `ReembolsoPoliticaV3ApiTest#recusaAutorrejeicao` |
| CA30 | T011 | `ReembolsoPoliticaV3ApiTest#gestorNaoRejeitaTambemNoFinanceiro` |
| CA31 | T011 | `ReembolsoPoliticaV3ApiTest#historicoContemSomenteDecisoesAceitas` |
| CA32 | T011 | `ReembolsoPoliticaV3ApiTest#recusaMotivoSoComEspacos` |

## Provas complementares do plano

| Tarefa | Teste ou cenário obrigatório |
|---|---|
| T001 | `PoliticaDeVigenciaTest`: antes/no corte e Brasília; `PoliticaDePrazoTest`: exatamente 60 dias continua recusado |
| T002 | `ReembolsoTest`: estado parcial/final, rejeição preservando aprovação anterior, fechamento de etapas e projeção singular da última decisão |
| T003 | `ReembolsoPersistenciaTest`: histórico carregado por id/listagem com open-in-view desligado, ordem preservada e rollback de evento junto ao pedido |
| T004 | `PoliticaDeAutorizacaoTest`: papel incompatível com etapa, identidade/vínculo ausentes, autoaprovação/autorrejeição e pessoa repetida no Financeiro |
| T008 | `ControleDeTetoDiarioTest`: âncora inicial/reutilizada, unicidade, confirmação após corrida e falha não relacionada propagada |
| T013 | `ReembolsoVigenciaApiTest`: antes da meia-noite continua v2; rejeição v2 sem motivo após vigência; consulta/listagem mantêm dados legados e desempate por id |
| T014 | `ReembolsoConcorrenciaTest#naoReinterpretaEtapaConcorrente`; `ReembolsoConcorrenciaTest#protegeEtapaFinanceira`; ausência de evento órfão após conflito |
| T015 | `ReembolsoConcorrenciaTest#serializaPrimeirasRefeicoesDoDia`: duas notas de R$ 80,00, apenas uma aceita, soma R$ 80,00 e uma âncora; rejeição concorrente com submissão respeita total confirmado |
| T016 | JSON ilegível/enum desconhecido retornam Problem Details 400; versão/lock retornam 409; regra contextual v3 retorna 422; pedido ausente mantém 404; v2 aceita corpo antigo |

O próximo passo é a revisão humana destas tarefas; após aprovação, `/sdd-implement specs\002-politica-v3`, começando por T001.

# Research 002 · Política de Despesas v3

## Pergunta

O que existe hoje que a spec aprovada `specs\002-politica-v3\spec.md` toca? Pesquisa de leitura delegada ao agente `pesquisador` em 21/10/2026, sem execução de testes nem alteração de código.

Nos caminhos abaixo, `main` representa `src\main\java\com\orbe\reembolso` e `test` representa `src\test\java\com\orbe\reembolso`.

## Achados

| # | Achado | Evidência | Impacto no plano |
|---|---|---|---|
| 1 | A submissão retorna 201, Location e representação completa; o pedido começa SUBMETIDA | `ReembolsoController#submeter`, `main\api\ReembolsoController.java:29-34`; `Reembolso#Reembolso`, `main\dominio\Reembolso.java:62-74` | Preservar contrato e estado inicial; acrescentar políticas sem alterar o formato de submissão |
| 2 | A entrada exige colaborador, categoria, valor positivo, data e CNPJ de 14 dígitos; dinheiro aceita até duas casas; descrição é opcional | `NovoReembolsoRequest`, `main\api\NovoReembolsoRequest.java:16-25` | Reutilizar validações; não adicionar validação de dígitos verificadores ou nova categoria |
| 3 | Colaborador na submissão e responsável na decisão recebem trim; consulta não normaliza o parâmetro; não há normalização de caixa | `NovoReembolsoRequest#paraComando`; `ReembolsoController#listar/aprovar/rejeitar`, `main\api\ReembolsoController.java:42-54` | Preservar equivalência atual de identidades; não introduzir cadastro nem comparação sem distinção de caixa |
| 4 | Submissão executa prazo, consulta de duplicidade e save; duplicidade independe de categoria e status | `ReembolsoService#submeter`, `main\servico\ReembolsoService.java:28-44`; `ReembolsoRepository#existsByColaboradorAndCnpjFornecedorAndValorAndDataDespesa`, `main\servico\ReembolsoRepository.java:13-14` | Manter rejeitados na duplicidade; soma de refeições precisa de consulta distinta, filtrada por regime e estados |
| 5 | Aprovar/rejeitar recebem somente responsável e motivo opcional; não há papel, vínculo nem etapa na entrada | `DecisaoRequest`, `main\api\DecisaoRequest.java:6-9`; `ReembolsoController#aprovar/rejeitar`, `main\api\ReembolsoController.java:47-54` | Estender a declaração na v3 sem exigir novos dados da v2 |
| 6 | Decisões exigem SUBMETIDA e encerram em APROVADA/REJEITADA; motivo ausente é aceito atualmente na rejeição | `Reembolso#aprovar/rejeitar/exigirStatus`, `main\dominio\Reembolso.java:74-90` | Introduzir estado intermediário; obrigatoriedade de motivo e autorização por etapa somente na v3 |
| 7 | Há um único trio decididoPor/decididoEm/motivoDecisao, sobrescrito ao decidir | `Reembolso#registrarDecisao`, `main\dominio\Reembolso.java:92-96`; `ReembolsoResponse#de`, `main\api\ReembolsoResponse.java:11-29` | Criar eventos de decisões aceitas e definir explicitamente a projeção singular como última decisão |
| 8 | PAGA existe no enum, mas não há operação de pagamento no serviço atual | `StatusReembolso`, `main\dominio\StatusReembolso.java:3-7`; `ReembolsoService`, `main\servico\ReembolsoService.java:28-72` | Não implementar pagamento; não fazer um pedido aprovado deixar de consumir teto por futura marcação de pagamento |
| 9 | A listagem filtra colaborador por igualdade e ordena data decrescente, depois id decrescente; busca por id não tem controle de acesso | `ReembolsoRepository#findByColaboradorOrderByDataDespesaDescIdDesc`, `main\servico\ReembolsoRepository.java:16`; `ReembolsoController#buscar/listar`, `main\api\ReembolsoController.java:37-44` | Preservar ordenação; não confundir filtro com autenticação ou autorização de consulta |
| 10 | Serviço é transacional; não há isolamento especial, lock ou retry | `ReembolsoService#aprovar/rejeitar/buscar`, `main\servico\ReembolsoService.java:14-15,47-67` | Salvar pedido e evento na mesma transação; erros de concorrência devem provocar rollback integral |
| 11 | A entidade não tem @Version; o repositório não tem locks, agregação ou unicidade da nota | `Reembolso`, `main\dominio\Reembolso.java:16-56`; `ReembolsoRepository`, `main\servico\ReembolsoRepository.java:11-17` | Versão otimista para decisões; proteção distinta para o total diário, inclusive quando ainda não existe pedido |
| 12 | Clock usa America/Sao_Paulo; momentos são LocalDateTime; prazo e momento da submissão são lidos separadamente | `ClockConfig#clock`, `main\config\ClockConfig.java:16-20`; `ReembolsoService#agora`, `main\servico\ReembolsoService.java:70-72`; `PoliticaDePrazo#validar`, `main\servico\PoliticaDePrazo.java:25-34` | Capturar um único momento por comando para corte de vigência e prazo coerentes |
| 13 | O prazo recusa dias >= 60; testes existentes cobrem 59 e 61, não exatamente 60 | `PoliticaDePrazo#validar`; `PoliticaDePrazoTest#aceitaDespesaDe59DiasAtras/rejeitaDespesaDe61DiasAtras`, `test\servico\PoliticaDePrazoTest.java:22-30`; decisão D7 da spec 002 | Não corrigir o 60º dia nesta mudança; acrescentar caracterização focada |
| 14 | Tratador cobre Bean Validation 400, não encontrado 404, duplicidade/transição 409 e regra de negócio 422 | `TratadorDeErros#entradaInvalida/naoEncontrado/duplicado/transicao/regraDeNegocio`, `main\api\TratadorDeErros.java:23-58` | Reutilizar Problem Details e acrescentar tratamento específico de concorrência e entradas novas |
| 15 | Não há handlers explícitos para locks, versão otimista ou JSON ilegível | `TratadorDeErros`, `main\api\TratadorDeErros.java:23-58` | Assegurar que erros introduzidos pelo contrato não escapem como resposta fora do padrão |
| 16 | Java 21, Spring Boot 3.5.10, web/JPA/validation, H2 e starter-test; sem Security ou ferramenta de migração | `pom.xml:7-55` | Não adicionar dependências; autorização permanece simulação explícita |
| 17 | H2 é em memória, Hibernate create-drop, open-in-view desativado e console habilitado | `src\main\resources\application.yml:1-14` | DDL gerado basta para o lab; carregar histórico antes de sair da transação; não prometer migração persistente |
| 18 | Relógio de teste fixa 20/10/2026 às 10:00 Brasília | `RelogioDeTeste#fixo/relogioFixo`, `test\suporte\RelogioDeTeste.java:13-27` | Manter testes existentes como caracterização v2; usar contexto separado para v3 e corte |
| 19 | Testes existentes aprovam apenas com responsável, cobrem decisão repetida sequencial e limpam pedidos com deleteAll | `ReembolsoApiTest#aprovaReembolsoSubmetido/naoAprovaDuasVezes/limpar`, `test\api\ReembolsoApiTest.java:27-40,75-99,136-142` | Preservar métodos existentes; adaptar limpeza para novas relações sem apagar testes; criar testes realmente concorrentes |
| 20 | Gate Windows executa Maven test e depois SddCheck, interrompendo no erro | `scripts\verify.ps1:1-10` | Toda tarefa futura termina com o gate completo verde |
| 21 | SddCheck valida requisitos e tarefas, mas não lê plan/research nem comprova CA → teste | `SddCheck#verificar/lerTarefas`, `scripts\SddCheck.java:71-215` | Registrar mapeamento completo dos 32 CAs no plano; não apresentar checker como prova de cobertura |
| 22 | Spec 001 exige acompanhar a mudança de aprovação; tasks estão concluídas e progress é histórico | `specs\001-reembolso-basico\spec.md:3-5,20,28-29,40-41`; `specs\001-reembolso-basico\tasks.md:5-26`; `specs\001-reembolso-basico\progress.md:24-28` | Contextualizar a spec 001 como v2 durante a implementação, sem reescrever tarefas concluídas ou log |

## O que não foi encontrado

- Cadastro confiável de colaboradores, identidade autenticada, gestor ou Financeiro: o contrato atual recebe apenas strings (`DecisaoRequest`; `ReembolsoController#aprovar/rejeitar`).
- Histórico por etapas, versão otimista, saldo diário, âncora para bloqueio ou política de refeições (`Reembolso`; `ReembolsoRepository`; `ReembolsoService#submeter`).
- Migrações ou armazenamento durável configurado (`pom.xml:7-55`; `src\main\resources\application.yml:1-14`).
- Testes concorrentes, de teto, alçada ou corte de vigência (`ReembolsoApiTest`; `PoliticaDePrazoTest`; `RelogioDeTeste#fixo`).

## Riscos e armadilhas

- A corrida de decisões é uma inferência fundamentada em leitura seguida de gravação sem versão, não uma reprodução executada (`ReembolsoService#aprovar/rejeitar`; `Reembolso#exigirStatus`).
- Bloquear apenas pedidos existentes não protege um dia ainda vazio; CA20 cobre saldo prévio, mas a implementação também precisa provar a corrida da primeira submissão (`ReembolsoRepository`; spec 002, R1).
- Após esperar um bloqueio, uma requisição não deve ser reinterpretada como decisão de outra etapa. Declarar a etapa no novo contrato e comparar com o estado é necessário para concretizar R8 (`ReembolsoController#aprovar`; spec 002, R3/R8).
- Os formatos exatos de papel/vínculo e dos campos novos de resposta são decisões técnicas do plano; não autorizam acrescentar uma fonte real de identidade, substituição automática ou novas regras de acesso (spec 002, R5/R7 e decisões D3/D4).
- Atualizar globalmente o relógio de teste para novembro transformaria indevidamente os testes v2 em v3 (`RelogioDeTeste#fixo`; `ReembolsoApiTest#aprovaReembolsoSubmetido`).
- Não converter qualquer violação SQL em duplicidade ou conflito conhecido; não reconstruir etapas legadas a partir dos campos singulares (`TratadorDeErros`; `Reembolso#registrarDecisao`).
- A aprovação por nome declarado continua sujeita a personificação, por decisão de escopo, e o 60º dia continua divergente da política (spec 002, decisões D3/D4 e D7).

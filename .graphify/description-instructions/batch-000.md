# Node Description Batch 1 of 3

Graphify is running in assistant/skill mode (no API key). You are the host
assistant (Claude Code / Codex / Gemini CLI). Read the prompt below and write
your JSON answer to the answer file.

## Prompt

You are documenting nodes in a knowledge graph.
For each entry below, write ONE concise factual plain-language sentence
describing what it is or does. Use only the provided context.
For a code symbol (kind=code-symbol — a function, class, or constant),
describe what the function/symbol does based on its name, source location
and neighbors — e.g. "Resolves the configured ontology profile from graphify.yaml.".
Write every description in English (en). Do not switch languages.
No marketing language.
Respond ONLY with a JSON object mapping each node id (as a string) to its
one-sentence description — no prose, no markdown fences.

- "dominio_reembolso_reembolso": "Reembolso" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/Reembolso.java:L16 | neighbors=[Reembolso.java, .aprovar(), .exigirStatus(), .getCategoria(), .getCnpjFornecedor(), .getColaborador()]
- "api_reembolsoapitest_reembolsoapitest": "ReembolsoApiTest" | kind=code-symbol | source=src/test/java/com/orbe/reembolso/api/ReembolsoApiTest.java:L27 | neighbors=[ReembolsoApiTest.java, .aprovaReembolsoSubmetido(), .decidir(), .idDe(), .limpar(), .listaPorColaborador()]
- "api_reembolsoapitest_reembolsoapitest_submeter": ".submeter()" | kind=code-symbol | source=src/test/java/com/orbe/reembolso/api/ReembolsoApiTest.java:L126 | neighbors=[ReembolsoApiTest, .aprovaReembolsoSubmetido(), .listaPorColaborador(), .naoAprovaDuasVezes(), .recusaCnpjMalFormatado(), .recusaDespesaForaDoPrazo()]
- "servico_reembolsoservice_reembolsoservice": "ReembolsoService" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/servico/ReembolsoService.java:L14 | neighbors=[ReembolsoService.java, .agora(), .aprovar(), .buscar(), .listarPorColaborador(), .ReembolsoService()]
- "api_reembolsocontroller_reembolsocontroller": "ReembolsoController" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/api/ReembolsoController.java:L20 | neighbors=[ReembolsoController.java, .aprovar(), .buscar(), .listar(), .ReembolsoController(), .rejeitar()]
- "api_tratadordeerros_tratadordeerros": "TratadorDeErros" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/api/TratadorDeErros.java:L19 | neighbors=[TratadorDeErros.java, .duplicado(), .entradaInvalida(), .naoEncontrado(), .problema(), .regraDeNegocio()]
- "api_tratadordeerros_tratadordeerros_problema": ".problema()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/api/TratadorDeErros.java:L54 | neighbors=[TratadorDeErros, .duplicado(), .entradaInvalida(), .naoEncontrado(), .regraDeNegocio(), .transicao()]
- "servico_reembolsorepository_reembolsorepository": "ReembolsoRepository" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/servico/ReembolsoRepository.java:L11 | neighbors=[ReembolsoRepository.java, JpaRepository, Long, Reembolso, .existsByColaboradorAndCnpjFornecedorAn…, .findByColaboradorOrderByDataDespesaDes…]
- "reembolso": "Reembolso" | kind=code-symbol | neighbors=[ReembolsoController.java, ReembolsoResponse.java, ReembolsoRepository.java, ReembolsoRepository, ReembolsoService.java]
- "servico_politicadeprazotest_politicadeprazotest": "PoliticaDePrazoTest" | kind=code-symbol | source=src/test/java/com/orbe/reembolso/servico/PoliticaDePrazoTest.java:L12 | neighbors=[PoliticaDePrazoTest.java, .aceitaDespesaDe59DiasAtras(), .aceitaDespesaDeHoje(), .rejeitaDespesaDe61DiasAtras(), .rejeitaDespesaNoFuturo()]
- "api_reembolsoapitest_reembolsoapitest_aprovareembolsosubmetido": ".aprovaReembolsoSubmetido()" | kind=code-symbol | source=src/test/java/com/orbe/reembolso/api/ReembolsoApiTest.java:L74 | neighbors=[ReembolsoApiTest, .decidir(), .idDe(), .submeter()]
- "api_reembolsoapitest_reembolsoapitest_idde": ".idDe()" | kind=code-symbol | source=src/test/java/com/orbe/reembolso/api/ReembolsoApiTest.java:L144 | neighbors=[ReembolsoApiTest, .aprovaReembolsoSubmetido(), .naoAprovaDuasVezes(), .rejeitaComMotivo()]
- "api_reembolsoapitest_reembolsoapitest_naoaprovaduasvezes": ".naoAprovaDuasVezes()" | kind=code-symbol | source=src/test/java/com/orbe/reembolso/api/ReembolsoApiTest.java:L84 | neighbors=[ReembolsoApiTest, .decidir(), .idDe(), .submeter()]
- "runtimeexception": "RuntimeException" | kind=code-symbol | neighbors=[RecursoNaoEncontradoException, ReembolsoDuplicadoException, RegraDeNegocioException, TransicaoDeStatusInvalidaException]
- "scripts_sddcheck_sddcheck": "SddCheck" | kind=code-symbol | source=scripts/SddCheck.java:L27 | neighbors=[SddCheck.java, .lerTarefas(), .main(), .verificar()]
- "servico_reembolsoservice_reembolsoservice_agora": ".agora()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/servico/ReembolsoService.java:L70 | neighbors=[ReembolsoService, .aprovar(), .rejeitar(), .submeter()]
- "api_reembolsoapitest_reembolsoapitest_decidir": ".decidir()" | kind=code-symbol | source=src/test/java/com/orbe/reembolso/api/ReembolsoApiTest.java:L136 | neighbors=[ReembolsoApiTest, .aprovaReembolsoSubmetido(), .naoAprovaDuasVezes()]
- "api_reembolsoapitest_reembolsoapitest_rejeitacommotivo": ".rejeitaComMotivo()" | kind=code-symbol | source=src/test/java/com/orbe/reembolso/api/ReembolsoApiTest.java:L94 | neighbors=[ReembolsoApiTest, .idDe(), .submeter()]
- "dominio_recursonaoencontradoexception_recursonaoencontradoexception": "RecursoNaoEncontradoException" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/RecursoNaoEncontradoException.java:L4 | neighbors=[RecursoNaoEncontradoException.java, .RecursoNaoEncontradoException(), RuntimeException]
- "dominio_reembolso_reembolso_aprovar": ".aprovar()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/Reembolso.java:L74 | neighbors=[Reembolso, .exigirStatus(), .registrarDecisao()]
- "dominio_reembolso_reembolso_exigirstatus": ".exigirStatus()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/Reembolso.java:L86 | neighbors=[Reembolso, .aprovar(), .rejeitar()]
- "dominio_reembolso_reembolso_registrardecisao": ".registrarDecisao()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/Reembolso.java:L92 | neighbors=[Reembolso, .aprovar(), .rejeitar()]
- "dominio_reembolso_reembolso_rejeitar": ".rejeitar()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/Reembolso.java:L80 | neighbors=[Reembolso, .exigirStatus(), .registrarDecisao()]
- "dominio_reembolsoduplicadoexception_reembolsoduplicadoexception": "ReembolsoDuplicadoException" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/ReembolsoDuplicadoException.java:L4 | neighbors=[ReembolsoDuplicadoException.java, .ReembolsoDuplicadoException(), RuntimeException]
- "dominio_regradenegocioexception_regradenegocioexception": "RegraDeNegocioException" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/RegraDeNegocioException.java:L4 | neighbors=[RegraDeNegocioException.java, .RegraDeNegocioException(), RuntimeException]
- "dominio_transicaodestatusinvalidaexception_transicaodestatusinvalidaexception": "TransicaoDeStatusInvalidaException" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/TransicaoDeStatusInvalidaException.java:L4 | neighbors=[TransicaoDeStatusInvalidaException.java, .TransicaoDeStatusInvalidaException(), RuntimeException]
- "scripts_sddcheck_sddcheck_verificar": ".verificar()" | kind=code-symbol | source=scripts/SddCheck.java:L71 | neighbors=[SddCheck, .main(), .lerTarefas()]
- "servico_politicadeprazo_politicadeprazo": "PoliticaDePrazo" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/servico/PoliticaDePrazo.java:L14 | neighbors=[PoliticaDePrazo.java, .PoliticaDePrazo(), .validar()]
- "servico_reembolsorepository": "ReembolsoRepository.java" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/servico/ReembolsoRepository.java:L1 | neighbors=[JpaRepository, Reembolso, ReembolsoRepository]
- "servico_reembolsoservice_reembolsoservice_aprovar": ".aprovar()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/servico/ReembolsoService.java:L48 | neighbors=[ReembolsoService, .agora(), .buscar()]
- "servico_reembolsoservice_reembolsoservice_buscar": ".buscar()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/servico/ReembolsoService.java:L60 | neighbors=[ReembolsoService, .aprovar(), .rejeitar()]
- "servico_reembolsoservice_reembolsoservice_rejeitar": ".rejeitar()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/servico/ReembolsoService.java:L54 | neighbors=[ReembolsoService, .agora(), .buscar()]
- "suporte_relogiodeteste_relogiodeteste": "RelogioDeTeste" | kind=code-symbol | source=src/test/java/com/orbe/reembolso/suporte/RelogioDeTeste.java:L14 | neighbors=[RelogioDeTeste.java, .fixo(), .relogioFixo()]
- "api_reembolsoapitest_reembolsoapitest_listaporcolaborador": ".listaPorColaborador()" | kind=code-symbol | source=src/test/java/com/orbe/reembolso/api/ReembolsoApiTest.java:L108 | neighbors=[ReembolsoApiTest, .submeter()]
- "api_reembolsoapitest_reembolsoapitest_recusacnpjmalformatado": ".recusaCnpjMalFormatado()" | kind=code-symbol | source=src/test/java/com/orbe/reembolso/api/ReembolsoApiTest.java:L52 | neighbors=[ReembolsoApiTest, .submeter()]
- "api_reembolsoapitest_reembolsoapitest_recusadespesaforadoprazo": ".recusaDespesaForaDoPrazo()" | kind=code-symbol | source=src/test/java/com/orbe/reembolso/api/ReembolsoApiTest.java:L67 | neighbors=[ReembolsoApiTest, .submeter()]
- "api_reembolsoapitest_reembolsoapitest_recusareembolsoduplicado": ".recusaReembolsoDuplicado()" | kind=code-symbol | source=src/test/java/com/orbe/reembolso/api/ReembolsoApiTest.java:L59 | neighbors=[ReembolsoApiTest, .submeter()]
- "api_reembolsoapitest_reembolsoapitest_submetereembolsovalido": ".submeteReembolsoValido()" | kind=code-symbol | source=src/test/java/com/orbe/reembolso/api/ReembolsoApiTest.java:L43 | neighbors=[ReembolsoApiTest, .submeter()]
- "api_reembolsocontroller": "ReembolsoController.java" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/api/ReembolsoController.java:L1 | neighbors=[ReembolsoController, Reembolso]
- "api_reembolsoresponse": "ReembolsoResponse.java" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/api/ReembolsoResponse.java:L1 | neighbors=[de(), Reembolso]

## Instructions

Write a single JSON object mapping each node id to a one-sentence description
to: c:\Users\Arabe\desenv\sdd-lab-reembolso\.graphify\description-instructions\batch-000.json

Keep each description factual and concise (one sentence). No markdown, no prose
outside the JSON object. It is acceptable to omit a node if context is
insufficient — but include every node you can ground confidently.

Example answer format:
```json
{
  "node_id_1": "Resolves the configured ontology profile from graphify.yaml.",
  "node_id_2": "Colonel James Barclay, an antagonist in The Crooked Man."
}
```

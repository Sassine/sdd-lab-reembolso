# Node Description Batch 2 of 3

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

- "api_tratadordeerros_tratadordeerros_duplicado": ".duplicado()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/api/TratadorDeErros.java:L29 | neighbors=[TratadorDeErros, .problema()]
- "api_tratadordeerros_tratadordeerros_entradainvalida": ".entradaInvalida()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/api/TratadorDeErros.java:L44 | neighbors=[TratadorDeErros, .problema()]
- "api_tratadordeerros_tratadordeerros_naoencontrado": ".naoEncontrado()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/api/TratadorDeErros.java:L39 | neighbors=[TratadorDeErros, .problema()]
- "api_tratadordeerros_tratadordeerros_regradenegocio": ".regraDeNegocio()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/api/TratadorDeErros.java:L24 | neighbors=[TratadorDeErros, .problema()]
- "api_tratadordeerros_tratadordeerros_transicao": ".transicao()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/api/TratadorDeErros.java:L34 | neighbors=[TratadorDeErros, .problema()]
- "config_clockconfig_clockconfig": "ClockConfig" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/config/ClockConfig.java:L13 | neighbors=[ClockConfig.java, .clock()]
- "jparepository": "JpaRepository" | kind=code-symbol | neighbors=[ReembolsoRepository.java, ReembolsoRepository]
- "reembolso_reembolsoapplication_reembolsoapplication": "ReembolsoApplication" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/ReembolsoApplication.java:L6 | neighbors=[ReembolsoApplication.java, .main()]
- "scripts_sddcheck_sddcheck_lertarefas": ".lerTarefas()" | kind=code-symbol | source=scripts/SddCheck.java:L162 | neighbors=[SddCheck, .verificar()]
- "scripts_sddcheck_sddcheck_main": ".main()" | kind=code-symbol | source=scripts/SddCheck.java:L38 | neighbors=[SddCheck, .verificar()]
- "servico_reembolsoservice": "ReembolsoService.java" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/servico/ReembolsoService.java:L1 | neighbors=[Reembolso, ReembolsoService]
- "servico_reembolsoservice_reembolsoservice_submeter": ".submeter()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/servico/ReembolsoService.java:L28 | neighbors=[ReembolsoService, .agora()]
- "suporte_relogiodeteste_relogiodeteste_fixo": ".fixo()" | kind=code-symbol | source=src/test/java/com/orbe/reembolso/suporte/RelogioDeTeste.java:L19 | neighbors=[RelogioDeTeste, .relogioFixo()]
- "suporte_relogiodeteste_relogiodeteste_relogiofixo": ".relogioFixo()" | kind=code-symbol | source=src/test/java/com/orbe/reembolso/suporte/RelogioDeTeste.java:L24 | neighbors=[RelogioDeTeste, .fixo()]
- "api_novoreembolsorequest": "NovoReembolsoRequest.java" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/api/NovoReembolsoRequest.java:L1 | neighbors=[paraComando()]
- "api_novoreembolsorequest_paracomando": "paraComando()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/api/NovoReembolsoRequest.java:L24 | neighbors=[NovoReembolsoRequest.java]
- "api_reembolsoapitest": "ReembolsoApiTest.java" | kind=code-symbol | source=src/test/java/com/orbe/reembolso/api/ReembolsoApiTest.java:L1 | neighbors=[ReembolsoApiTest]
- "api_reembolsoapitest_reembolsoapitest_limpar": ".limpar()" | kind=code-symbol | source=src/test/java/com/orbe/reembolso/api/ReembolsoApiTest.java:L38 | neighbors=[ReembolsoApiTest]
- "api_reembolsoapitest_reembolsoapitest_retorna404paraidinexistente": ".retorna404ParaIdInexistente()" | kind=code-symbol | source=src/test/java/com/orbe/reembolso/api/ReembolsoApiTest.java:L120 | neighbors=[ReembolsoApiTest]
- "api_reembolsocontroller_reembolsocontroller_aprovar": ".aprovar()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/api/ReembolsoController.java:L47 | neighbors=[ReembolsoController]
- "api_reembolsocontroller_reembolsocontroller_buscar": ".buscar()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/api/ReembolsoController.java:L37 | neighbors=[ReembolsoController]
- "api_reembolsocontroller_reembolsocontroller_listar": ".listar()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/api/ReembolsoController.java:L42 | neighbors=[ReembolsoController]
- "api_reembolsocontroller_reembolsocontroller_reembolsocontroller": ".ReembolsoController()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/api/ReembolsoController.java:L26 | neighbors=[ReembolsoController]
- "api_reembolsocontroller_reembolsocontroller_rejeitar": ".rejeitar()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/api/ReembolsoController.java:L52 | neighbors=[ReembolsoController]
- "api_reembolsocontroller_reembolsocontroller_submeter": ".submeter()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/api/ReembolsoController.java:L30 | neighbors=[ReembolsoController]
- "api_reembolsoresponse_de": "de()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/api/ReembolsoResponse.java:L25 | neighbors=[ReembolsoResponse.java]
- "api_tratadordeerros": "TratadorDeErros.java" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/api/TratadorDeErros.java:L1 | neighbors=[TratadorDeErros]
- "config_clockconfig": "ClockConfig.java" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/config/ClockConfig.java:L1 | neighbors=[ClockConfig]
- "config_clockconfig_clockconfig_clock": ".clock()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/config/ClockConfig.java:L18 | neighbors=[ClockConfig]
- "dominio_recursonaoencontradoexception": "RecursoNaoEncontradoException.java" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/RecursoNaoEncontradoException.java:L1 | neighbors=[RecursoNaoEncontradoException]
- "dominio_recursonaoencontradoexception_recursonaoencontradoexception_recursonaoencontradoexception": ".RecursoNaoEncontradoException()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/RecursoNaoEncontradoException.java:L6 | neighbors=[RecursoNaoEncontradoException]
- "dominio_reembolso": "Reembolso.java" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/Reembolso.java:L1 | neighbors=[Reembolso]
- "dominio_reembolso_reembolso_getcategoria": ".getCategoria()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/Reembolso.java:L100 | neighbors=[Reembolso]
- "dominio_reembolso_reembolso_getcnpjfornecedor": ".getCnpjFornecedor()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/Reembolso.java:L103 | neighbors=[Reembolso]
- "dominio_reembolso_reembolso_getcolaborador": ".getColaborador()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/Reembolso.java:L99 | neighbors=[Reembolso]
- "dominio_reembolso_reembolso_getdatadespesa": ".getDataDespesa()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/Reembolso.java:L102 | neighbors=[Reembolso]
- "dominio_reembolso_reembolso_getdecididoem": ".getDecididoEm()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/Reembolso.java:L109 | neighbors=[Reembolso]
- "dominio_reembolso_reembolso_getdecididopor": ".getDecididoPor()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/Reembolso.java:L107 | neighbors=[Reembolso]
- "dominio_reembolso_reembolso_getdescricao": ".getDescricao()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/Reembolso.java:L104 | neighbors=[Reembolso]
- "dominio_reembolso_reembolso_getid": ".getId()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/Reembolso.java:L98 | neighbors=[Reembolso]

## Instructions

Write a single JSON object mapping each node id to a one-sentence description
to: c:\Users\Arabe\desenv\sdd-lab-reembolso\.graphify\description-instructions\batch-001.json

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

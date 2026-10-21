# Node Description Batch 3 of 3

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

- "dominio_reembolso_reembolso_getmotivodecisao": ".getMotivoDecisao()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/Reembolso.java:L108 | neighbors=[Reembolso]
- "dominio_reembolso_reembolso_getstatus": ".getStatus()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/Reembolso.java:L105 | neighbors=[Reembolso]
- "dominio_reembolso_reembolso_getsubmetidoem": ".getSubmetidoEm()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/Reembolso.java:L106 | neighbors=[Reembolso]
- "dominio_reembolso_reembolso_getvalor": ".getValor()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/Reembolso.java:L101 | neighbors=[Reembolso]
- "dominio_reembolso_reembolso_reembolso": ".Reembolso()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/Reembolso.java:L58 | neighbors=[Reembolso]
- "dominio_reembolsoduplicadoexception": "ReembolsoDuplicadoException.java" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/ReembolsoDuplicadoException.java:L1 | neighbors=[ReembolsoDuplicadoException]
- "dominio_reembolsoduplicadoexception_reembolsoduplicadoexception_reembolsoduplicadoexception": ".ReembolsoDuplicadoException()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/ReembolsoDuplicadoException.java:L6 | neighbors=[ReembolsoDuplicadoException]
- "dominio_regradenegocioexception": "RegraDeNegocioException.java" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/RegraDeNegocioException.java:L1 | neighbors=[RegraDeNegocioException]
- "dominio_regradenegocioexception_regradenegocioexception_regradenegocioexception": ".RegraDeNegocioException()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/RegraDeNegocioException.java:L6 | neighbors=[RegraDeNegocioException]
- "dominio_transicaodestatusinvalidaexception": "TransicaoDeStatusInvalidaException.java" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/TransicaoDeStatusInvalidaException.java:L1 | neighbors=[TransicaoDeStatusInvalidaException]
- "dominio_transicaodestatusinvalidaexception_transicaodestatusinvalidaexception_transicaodestatusinvalidaexception": ".TransicaoDeStatusInvalidaException()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/TransicaoDeStatusInvalidaException.java:L6 | neighbors=[TransicaoDeStatusInvalidaException]
- "long": "Long" | kind=code-symbol | neighbors=[ReembolsoRepository]
- "reembolso_reembolsoapplication": "ReembolsoApplication.java" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/ReembolsoApplication.java:L1 | neighbors=[ReembolsoApplication]
- "reembolso_reembolsoapplication_reembolsoapplication_main": ".main()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/ReembolsoApplication.java:L9 | neighbors=[ReembolsoApplication]
- "scripts_sddcheck": "SddCheck.java" | kind=code-symbol | source=scripts/SddCheck.java:L1 | neighbors=[SddCheck]
- "servico_politicadeprazo": "PoliticaDePrazo.java" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/servico/PoliticaDePrazo.java:L1 | neighbors=[PoliticaDePrazo]
- "servico_politicadeprazo_politicadeprazo_politicadeprazo": ".PoliticaDePrazo()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/servico/PoliticaDePrazo.java:L21 | neighbors=[PoliticaDePrazo]
- "servico_politicadeprazo_politicadeprazo_validar": ".validar()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/servico/PoliticaDePrazo.java:L25 | neighbors=[PoliticaDePrazo]
- "servico_politicadeprazotest": "PoliticaDePrazoTest.java" | kind=code-symbol | source=src/test/java/com/orbe/reembolso/servico/PoliticaDePrazoTest.java:L1 | neighbors=[PoliticaDePrazoTest]
- "servico_politicadeprazotest_politicadeprazotest_aceitadespesade59diasatras": ".aceitaDespesaDe59DiasAtras()" | kind=code-symbol | source=src/test/java/com/orbe/reembolso/servico/PoliticaDePrazoTest.java:L21 | neighbors=[PoliticaDePrazoTest]
- "servico_politicadeprazotest_politicadeprazotest_aceitadespesadehoje": ".aceitaDespesaDeHoje()" | kind=code-symbol | source=src/test/java/com/orbe/reembolso/servico/PoliticaDePrazoTest.java:L16 | neighbors=[PoliticaDePrazoTest]
- "servico_politicadeprazotest_politicadeprazotest_rejeitadespesade61diasatras": ".rejeitaDespesaDe61DiasAtras()" | kind=code-symbol | source=src/test/java/com/orbe/reembolso/servico/PoliticaDePrazoTest.java:L26 | neighbors=[PoliticaDePrazoTest]
- "servico_politicadeprazotest_politicadeprazotest_rejeitadespesanofuturo": ".rejeitaDespesaNoFuturo()" | kind=code-symbol | source=src/test/java/com/orbe/reembolso/servico/PoliticaDePrazoTest.java:L33 | neighbors=[PoliticaDePrazoTest]
- "servico_reembolsorepository_reembolsorepository_existsbycolaboradorandcnpjfornecedorandvaloranddatadespesa": ".existsByColaboradorAndCnpjFornecedorAndValorAndDataDespesa()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/servico/ReembolsoRepository.java:L13 | neighbors=[ReembolsoRepository]
- "servico_reembolsorepository_reembolsorepository_findbycolaboradororderbydatadespesadesciddesc": ".findByColaboradorOrderByDataDespesaDescIdDesc()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/servico/ReembolsoRepository.java:L16 | neighbors=[ReembolsoRepository]
- "servico_reembolsoservice_reembolsoservice_listarporcolaborador": ".listarPorColaborador()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/servico/ReembolsoService.java:L65 | neighbors=[ReembolsoService]
- "servico_reembolsoservice_reembolsoservice_reembolsoservice": ".ReembolsoService()" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/servico/ReembolsoService.java:L22 | neighbors=[ReembolsoService]
- "suporte_relogiodeteste": "RelogioDeTeste.java" | kind=code-symbol | source=src/test/java/com/orbe/reembolso/suporte/RelogioDeTeste.java:L1 | neighbors=[RelogioDeTeste]
- "api_decisaorequest": "DecisaoRequest.java" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/api/DecisaoRequest.java:L1
- "dominio_categoria": "Categoria.java" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/Categoria.java:L1
- "dominio_statusreembolso": "StatusReembolso.java" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/dominio/StatusReembolso.java:L1
- "servico_novoreembolso": "NovoReembolso.java" | kind=code-symbol | source=src/main/java/com/orbe/reembolso/servico/NovoReembolso.java:L1

## Instructions

Write a single JSON object mapping each node id to a one-sentence description
to: c:\Users\Arabe\desenv\sdd-lab-reembolso\.graphify\description-instructions\batch-002.json

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

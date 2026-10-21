# Community Labeling

Graphify is running in assistant/skill mode (no API key). You are the host
assistant (Claude Code / Codex / Gemini CLI). Read the community listing below
and write 2-5 word plain-language names for each.

## Language

LANGUAGE: each community line ends with a `[lang=…]` marker giving the
language of its source nodes. Write that community's name in EXACTLY that
language. Do not normalize every name to one common language.

## Communities

Community 0: Reembolso, Reembolso.java, .aprovar(, .exigirStatus(, .getCategoria(, .getCnpjFornecedor(, .getColaborador(, .getDataDespesa(, .getDecididoEm(, .getDecididoPor(, .getDescricao(, .getId( [lang=en]
Community 1: ReembolsoApiTest, ReembolsoApiTest.java, .aprovaReembolsoSubmetido(, .decidir(, .idDe(, .limpar(, .listaPorColaborador(, .naoAprovaDuasVezes(, .recusaCnpjMalFormatado(, .recusaDespesaForaDoPrazo(, .recusaReembolsoDuplicado(, .rejeitaComMotivo( [lang=en]
Community 2: RecursoNaoEncontradoException, ReembolsoDuplicadoException, RecursoNaoEncontradoException.java, .RecursoNaoEncontradoException(, ReembolsoDuplicadoException.java, .ReembolsoDuplicadoException(, RegraDeNegocioException.java, RegraDeNegocioException, .RegraDeNegocioException(, TransicaoDeStatusInvalidaException.java, TransicaoDeStatusInvalidaException, .TransicaoDeStatusInvalidaException( [lang=en]
Community 3: ReembolsoRepository, ReembolsoController.java, ReembolsoResponse.java, de(, JpaRepository, Long, Reembolso, ReembolsoRepository.java, .existsByColaboradorAndCnpjFornecedorAndValorAndDataDespesa(, .findByColaboradorOrderByDataDespesaDescIdDesc(, ReembolsoService.java [lang=nl]
Community 4: TratadorDeErros, TratadorDeErros.java, .duplicado(, .entradaInvalida(, .naoEncontrado(, .problema(, .regraDeNegocio(, .transicao( [lang=en]
Community 5: ReembolsoService, .agora(, .aprovar(, .buscar(, .listarPorColaborador(, .ReembolsoService(, .rejeitar(, .submeter( [lang=en]
Community 6: ReembolsoController, .aprovar(, .buscar(, .listar(, .ReembolsoController(, .rejeitar(, .submeter( [lang=en]
Community 7: PoliticaDePrazoTest, PoliticaDePrazoTest.java, .aceitaDespesaDe59DiasAtras(, .aceitaDespesaDeHoje(, .rejeitaDespesaDe61DiasAtras(, .rejeitaDespesaNoFuturo( [lang=en]
Community 8: SddCheck, SddCheck.java, .lerTarefas(, .main(, .verificar( [lang=en]
Community 9: PoliticaDePrazo.java, PoliticaDePrazo, .PoliticaDePrazo(, .validar( [lang=en]
Community 10: RelogioDeTeste.java, RelogioDeTeste, .fixo(, .relogioFixo( [lang=en]
Community 11: ClockConfig.java, ClockConfig, .clock( [lang=en]
Community 12: ReembolsoApplication.java, ReembolsoApplication, .main( [lang=en]
Community 13: NovoReembolsoRequest.java, paraComando( [lang=en]
Community 14: DecisaoRequest.java [lang=en]
Community 15: Categoria.java [lang=en]
Community 16: StatusReembolso.java [lang=en]
Community 17: NovoReembolso.java [lang=en]

## Instructions

Write a single JSON object mapping each community id (as a string) to its
2-5 word name to: c:\Users\Arabe\desenv\sdd-lab-reembolso\.graphify\label-instructions\communities.json

Example:
```json
{
  "0": "Authentication Flow",
  "1": "Authentication Flow",
  "2": "Authentication Flow"
}
```

Then re-run `graphify update` (or `graphify label`) to ingest the names.

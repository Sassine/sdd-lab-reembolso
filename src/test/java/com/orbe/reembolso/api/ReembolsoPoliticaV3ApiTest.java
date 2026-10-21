package com.orbe.reembolso.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.jayway.jsonpath.JsonPath;
import com.orbe.reembolso.config.ClockConfig;
import com.orbe.reembolso.servico.ReembolsoRepository;
import com.orbe.reembolso.suporte.RelogioPoliticaV3DeTeste;

@SpringBootTest
@AutoConfigureMockMvc
@Import(RelogioPoliticaV3DeTeste.class)
class ReembolsoPoliticaV3ApiTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ReembolsoRepository repositorio;

    @Autowired
    Clock clock;

    @BeforeEach
    void limpar() {
        repositorio.deleteAll();
        agora(RelogioPoliticaV3DeTeste.AGORA);
    }

    @Test
    void aceitaRefeicaoNoTeto() throws Exception {
        long id = submeterRefeicao("120.00", "11222333000181");

        verificarAguardandoGestor(id);
        consultar(id).andExpect(jsonPath("$.valor")
                .value(comparesEqualTo(new BigDecimal("120.00")), BigDecimal.class));
        recusarRefeicaoPorTeto("0.01", "11222333000182");
        assertThat(repositorio.count()).isEqualTo(1);
        verificarAguardandoGestor(id);
    }

    @Test
    void recusaRefeicaoAcimaDoTeto() throws Exception {
        recusarRefeicaoPorTeto("120.01", "11222333000181");

        mvc.perform(get("/reembolsos").param("colaborador", "ana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        assertThat(repositorio.count()).isZero();
        submeterRefeicao("120.00", "11222333000182");
        assertThat(repositorio.count()).isEqualTo(1);
    }

    @Test
    void somaPendentesEAprovadasSemContarDuasVezes() throws Exception {
        long pendente = submeterRefeicao("50.00", "11222333000181");
        long aprovado = submeterRefeicao("30.00", "11222333000182");
        aprovar(aprovado, "GESTOR", "bruno").andExpect(status().isOk());
        long ultimo = submeterRefeicao("40.00", "11222333000183");

        aprovar(ultimo, "GESTOR", "bruno")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"));
        recusarRefeicaoPorTeto("0.01", "11222333000184");

        verificarAguardandoGestor(pendente);
        for (long id : new long[] {aprovado, ultimo}) {
            consultar(id)
                    .andExpect(jsonPath("$.status").value("APROVADA"))
                    .andExpect(jsonPath("$.historico", hasSize(1)))
                    .andExpect(jsonPath("$.historico[0].etapa").value("GESTOR"))
                    .andExpect(jsonPath("$.historico[0].resultado").value("APROVADA"))
                    .andExpect(jsonPath("$.historico[0].responsavel").value("bruno"));
        }
        assertThat(repositorio.count()).isEqualTo(3);
    }

    @Test
    void recusaExcedenteSomadoSemAlterarAnterior() throws Exception {
        long id = submeterRefeicao("80.00", "11222333000181");
        Object antes = JsonPath.read(consultar(id).andReturn().getResponse().getContentAsString(), "$");

        recusarRefeicaoPorTeto("40.01", "11222333000182");

        Object depois = JsonPath.read(consultar(id).andReturn().getResponse().getContentAsString(), "$");
        assertThat(depois).isEqualTo(antes);
        mvc.perform(get("/reembolsos").param("colaborador", "ana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(id))
                .andExpect(jsonPath("$[0].valor")
                        .value(comparesEqualTo(new BigDecimal("80.00")), BigDecimal.class))
                .andExpect(jsonPath("$[0].status").value("SUBMETIDA"))
                .andExpect(jsonPath("$[0].historico", hasSize(0)));
        assertThat(repositorio.count()).isEqualTo(1);
        submeterRefeicao("40.00", "11222333000183");
    }

    @Test
    void rejeicaoLiberaTetoSemLiberarDuplicidade() throws Exception {
        long rejeitado = submeterRefeicao("80.00", "11222333000181");
        recusarRejeicaoSemAlterar(rejeitado, declaracaoRejeicao("GESTOR", "bruno", "   "),
                422, "motivo");
        recusarRefeicaoPorTeto("120.00", "11222333000182");
        verificarAguardandoGestor(rejeitado);

        rejeitarDeclaracao(rejeitado, declaracaoRejeicao("GESTOR", "bruno", "Despesa particular"))
                .andExpect(status().isOk());
        verificarRejeicao(rejeitado, "GESTOR", "bruno", "2026-11-03T10:00:00",
                "Despesa particular", 1);

        // O rejeitado ainda impede duplicidade, inclusive com categoria diferente.
        for (String categoria : new String[] {"REFEICAO", "TRANSPORTE"}) {
            enviar(categoria, "80.00")
                    .andExpect(status().isConflict())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.status").value(409));
        }
        assertThat(repositorio.count()).isEqualTo(1);
        long novo = submeterRefeicao("120.00", "11222333000182");
        verificarAguardandoGestor(novo);
        recusarSemAlterar(rejeitado, declaracaoGestor(), 409, "REJEITADA");
        verificarRejeicao(rejeitado, "GESTOR", "bruno", "2026-11-03T10:00:00",
                "Despesa particular", 1);
        mvc.perform(get("/reembolsos").param("colaborador", "ana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(novo))
                .andExpect(jsonPath("$[1].id").value(rejeitado))
                .andExpect(jsonPath("$[1].historico", hasSize(1)));
        assertThat(repositorio.count()).isEqualTo(2);
    }

    @Test
    void separaTetoPorColaboradorEData() throws Exception {
        long original = submeterRefeicao("120.00", "11222333000181");
        ResultActions outroColaborador = enviarRefeicao("bia", "120.00", "2026-11-02", "11222333000181")
                .andExpect(status().isCreated());
        long outroId = ((Number) JsonPath.read(outroColaborador.andReturn().getResponse()
                .getContentAsString(), "$.id")).longValue();
        ResultActions outraData = enviarRefeicao("ana", "120.00", "2026-11-03", "11222333000181")
                .andExpect(status().isCreated());
        long outraDataId = ((Number) JsonPath.read(outraData.andReturn().getResponse()
                .getContentAsString(), "$.id")).longValue();

        recusarRefeicaoPorTeto("0.01", "11222333000182");
        consultar(outroId)
                .andExpect(jsonPath("$.colaborador").value("bia"))
                .andExpect(jsonPath("$.dataDespesa").value("2026-11-02"))
                .andExpect(jsonPath("$.valor").value(120.00))
                .andExpect(jsonPath("$.status").value("SUBMETIDA"));
        consultar(outraDataId)
                .andExpect(jsonPath("$.colaborador").value("ana"))
                .andExpect(jsonPath("$.dataDespesa").value("2026-11-03"))
                .andExpect(jsonPath("$.valor").value(120.00))
                .andExpect(jsonPath("$.status").value("SUBMETIDA"));
        verificarAguardandoGestor(original);
        mvc.perform(get("/reembolsos").param("colaborador", "ana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(outraDataId))
                .andExpect(jsonPath("$[1].id").value(original));
        assertThat(repositorio.count()).isEqualTo(3);
    }

    @Test
    void outrasCategoriasNaoConsomemTetoDeRefeicao() throws Exception {
        submeter("TRANSPORTE", "120.00");
        submeter("HOSPEDAGEM", "120.01");
        submeter("OUTROS", "120.02");
        long refeicao = submeterRefeicao("120.00", "11222333000182");

        recusarRefeicaoPorTeto("0.01", "11222333000183");
        verificarAguardandoGestor(refeicao);
        assertThat(repositorio.count()).isEqualTo(4);
    }

    @Test
    void refeicaoV2NaoConsomeTetoV3() throws Exception {
        agora(LocalDateTime.of(2026, 10, 31, 10, 0));
        ResultActions legado = enviarRefeicao("ana", "100.00", "2026-10-30", "11222333000181")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.politica").value("V2"));
        long idLegado = ((Number) JsonPath.read(legado.andReturn().getResponse()
                .getContentAsString(), "$.id")).longValue();
        agora(RelogioPoliticaV3DeTeste.AGORA);

        ResultActions novo = enviarRefeicao("ana", "120.00", "2026-10-30", "11222333000182")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.politica").value("V3"));
        long idNovo = ((Number) JsonPath.read(novo.andReturn().getResponse()
                .getContentAsString(), "$.id")).longValue();

        aprovar(idNovo, "GESTOR", "bruno").andExpect(status().isOk());
        consultar(idNovo)
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.valor").value(120.00))
                .andExpect(jsonPath("$.historico", hasSize(1)));
        consultar(idLegado)
                .andExpect(jsonPath("$.status").value("SUBMETIDA"))
                .andExpect(jsonPath("$.politica").value("V2"))
                .andExpect(jsonPath("$.valor").value(100.00))
                .andExpect(jsonPath("$.historico", hasSize(0)));
        assertThat(repositorio.count()).isEqualTo(2);
    }

    private long submeterRefeicao(String valor, String cnpj) throws Exception {
        ResultActions resultado = enviarRefeicao("ana", valor, "2026-11-02", cnpj)
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("SUBMETIDA"))
                .andExpect(jsonPath("$.politica").value("V3"))
                .andExpect(jsonPath("$.valor").value(comparesEqualTo(new BigDecimal(valor)), BigDecimal.class))
                .andExpect(jsonPath("$.historico", hasSize(0)));
        long id = ((Number) JsonPath.read(resultado.andReturn().getResponse()
                .getContentAsString(), "$.id")).longValue();
        resultado.andExpect(header().string("Location", "/reembolsos/" + id));
        consultar(id)
                .andExpect(jsonPath("$.valor").value(comparesEqualTo(new BigDecimal(valor)), BigDecimal.class));
        return id;
    }

    private void recusarRefeicaoPorTeto(String valor, String cnpj) throws Exception {
        Object antes = JsonPath.read(mvc.perform(get("/reembolsos").param("colaborador", "ana"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), "$");
        long quantidadeAntes = repositorio.count();

        enviarRefeicao("ana", valor, "2026-11-02", cnpj)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.detail").value(containsString("120,00")));

        Object depois = JsonPath.read(mvc.perform(get("/reembolsos").param("colaborador", "ana"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(), "$");
        assertThat(depois).as("Recusa integral não cria pedido nem altera reservas ou decisões").isEqualTo(antes);
        assertThat(repositorio.count()).isEqualTo(quantidadeAntes);
    }

    private ResultActions enviarRefeicao(String colaborador, String valor, String data, String cnpj)
            throws Exception {
        return mvc.perform(post("/reembolsos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"colaborador":"%s","categoria":"REFEICAO","valor":%s,
                         "dataDespesa":"%s","cnpjFornecedor":"%s"}
                        """.formatted(colaborador, valor, data, cnpj)));
    }

    @Test
    void aprova500SomenteComGestor() throws Exception {
        long id = submeter("TRANSPORTE", "500.00");

        aprovar(id, "GESTOR", " bruno ")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.etapaPendente").value(nullValue()))
                .andExpect(jsonPath("$.motivoDecisao").value(nullValue()));

        consultar(id)
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.politica").value("V3"))
                .andExpect(jsonPath("$.etapaPendente").value(nullValue()))
                .andExpect(jsonPath("$.decididoPor").value("bruno"))
                .andExpect(jsonPath("$.decididoEm").value("2026-11-03T10:00:00"))
                .andExpect(jsonPath("$.historico", hasSize(1)))
                .andExpect(jsonPath("$.historico[0].etapa").value("GESTOR"))
                .andExpect(jsonPath("$.historico[0].resultado").value("APROVADA"))
                .andExpect(jsonPath("$.historico[0].responsavel").value("bruno"))
                .andExpect(jsonPath("$.historico[0].quando").value("2026-11-03T10:00:00"))
                .andExpect(jsonPath("$.historico[0].motivo").value(nullValue()));
    }

    @Test
    void aprova500e01EmDuasEtapas() throws Exception {
        aprovarEmDuasEtapas("500.01");
    }

    @Test
    void recusaFinanceiroAntesDoGestor() throws Exception {
        long id = submeter("HOSPEDAGEM", "5000.00");

        aprovar(id, "FINANCEIRO", "carla")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Transição de status inválida"));

        consultar(id)
                .andExpect(jsonPath("$.status").value("SUBMETIDA"))
                .andExpect(jsonPath("$.etapaPendente").value("GESTOR"))
                .andExpect(jsonPath("$.historico", hasSize(0)))
                .andExpect(jsonPath("$.decididoPor").value(nullValue()))
                .andExpect(jsonPath("$.decididoEm").value(nullValue()));
    }

    @Test
    void aprova5000EmDuasEtapas() throws Exception {
        aprovarEmDuasEtapas("5000.00");
    }

    @Test
    void recusa5000e01OrientandoViagens() throws Exception {
        enviar("HOSPEDAGEM", "5000.01")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value(containsString("adiantamento")))
                .andExpect(jsonPath("$.detail").value(containsString("Viagens")));

        mvc.perform(get("/reembolsos").param("colaborador", "ana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        assertThat(repositorio.count()).isZero();
    }

    @Test
    void recusaAutoaprovacao() throws Exception {
        long id = submeter("TRANSPORTE", "100.00");
        String corpo = JsonPath.parse(declaracaoGestor())
                .set("$.responsavel", " ana ")
                .set("$.gestorImediato", " ana ").jsonString();

        recusarSemAlterar(id, corpo, 422, "próprio pedido");
        verificarAguardandoGestor(id);
    }

    @Test
    void recusaMesmoAprovadorNoFinanceiro() throws Exception {
        long id = submeter("HOSPEDAGEM", "800.00");
        aprovar(id, "GESTOR", "bruno").andExpect(status().isOk());
        agora(RelogioPoliticaV3DeTeste.AGORA.plusHours(1));
        String corpo = JsonPath.parse(declaracaoGestor())
                .set("$.responsavel", " bruno ")
                .set("$.papel", "FINANCEIRO")
                .set("$.etapa", "FINANCEIRO")
                .set("$.gestorImediato", " diego ").jsonString();

        recusarSemAlterar(id, corpo, 422, "pessoa distinta");
        consultar(id)
                .andExpect(jsonPath("$.status").value("AGUARDANDO_FINANCEIRO"))
                .andExpect(jsonPath("$.etapaPendente").value("FINANCEIRO"))
                .andExpect(jsonPath("$.decididoPor").value("bruno"))
                .andExpect(jsonPath("$.decididoEm").value("2026-11-03T10:00:00"))
                .andExpect(jsonPath("$.motivoDecisao").value(nullValue()))
                .andExpect(jsonPath("$.historico", hasSize(1)))
                .andExpect(jsonPath("$.historico[0].etapa").value("GESTOR"))
                .andExpect(jsonPath("$.historico[0].resultado").value("APROVADA"))
                .andExpect(jsonPath("$.historico[0].responsavel").value("bruno"))
                .andExpect(jsonPath("$.historico[0].quando").value("2026-11-03T10:00:00"))
                .andExpect(jsonPath("$.historico[0].motivo").value(nullValue()));
    }

    @Test
    void recusaQuemNaoEOGestorDeclarado() throws Exception {
        long id = submeter("TRANSPORTE", "100.00");
        String corpo = JsonPath.parse(declaracaoGestor())
                .set("$.responsavel", "diego").jsonString();

        recusarSemAlterar(id, corpo, 422, "gestor imediato declarado");
        verificarAguardandoGestor(id);
    }

    @Test
    void recusaVinculoAusente() throws Exception {
        long id = submeter("TRANSPORTE", "100.00");
        for (String campo : new String[] {"colaboradorVinculado", "gestorImediato"}) {
            String explicacao = campo.equals("colaboradorVinculado") ? "vínculo" : "gestor imediato";
            recusarSemAlterar(id, JsonPath.parse(declaracaoGestor()).delete("$." + campo).jsonString(),
                    422, explicacao);
            recusarSemAlterar(id, JsonPath.parse(declaracaoGestor()).set("$." + campo, null).jsonString(),
                    422, explicacao);
            recusarSemAlterar(id, JsonPath.parse(declaracaoGestor()).set("$." + campo, "   ").jsonString(),
                    422, explicacao);
        }
        verificarAguardandoGestor(id);
    }

    @Test
    void mudancaDeclaradaDeGestorNaoReescreveHistorico() throws Exception {
        long id = submeter("HOSPEDAGEM", "800.00");
        aprovar(id, "GESTOR", "bruno").andExpect(status().isOk());
        agora(RelogioPoliticaV3DeTeste.AGORA.plusHours(1));
        String corpo = JsonPath.parse(declaracaoGestor())
                .set("$.responsavel", " carla ")
                .set("$.papel", "FINANCEIRO")
                .set("$.etapa", "FINANCEIRO")
                .set("$.gestorImediato", " diego ").jsonString();

        aprovarDeclaracao(id, corpo)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"));

        consultar(id)
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.etapaPendente").value(nullValue()))
                .andExpect(jsonPath("$.decididoPor").value("carla"))
                .andExpect(jsonPath("$.decididoEm").value("2026-11-03T11:00:00"))
                .andExpect(jsonPath("$.motivoDecisao").value(nullValue()))
                .andExpect(jsonPath("$.historico", hasSize(2)))
                .andExpect(jsonPath("$.historico[0].etapa").value("GESTOR"))
                .andExpect(jsonPath("$.historico[0].resultado").value("APROVADA"))
                .andExpect(jsonPath("$.historico[0].responsavel").value("bruno"))
                .andExpect(jsonPath("$.historico[0].quando").value("2026-11-03T10:00:00"))
                .andExpect(jsonPath("$.historico[0].motivo").value(nullValue()))
                .andExpect(jsonPath("$.historico[1].etapa").value("FINANCEIRO"))
                .andExpect(jsonPath("$.historico[1].resultado").value("APROVADA"))
                .andExpect(jsonPath("$.historico[1].responsavel").value("carla"))
                .andExpect(jsonPath("$.historico[1].quando").value("2026-11-03T11:00:00"))
                .andExpect(jsonPath("$.historico[1].motivo").value(nullValue()));
    }

    @Test
    void recusaIdentidadeAusenteOuBrancaComoFormatoInvalido() throws Exception {
        long id = submeter("TRANSPORTE", "100.00");
        recusarSemAlterar(id, JsonPath.parse(declaracaoGestor()).delete("$.responsavel").jsonString(),
                400, "inválidos").andExpect(jsonPath("$.campos.responsavel").exists());
        recusarSemAlterar(id, JsonPath.parse(declaracaoGestor()).set("$.responsavel", null).jsonString(),
                400, "inválidos").andExpect(jsonPath("$.campos.responsavel").exists());
        recusarSemAlterar(id, JsonPath.parse(declaracaoGestor()).set("$.responsavel", "   ").jsonString(),
                400, "inválidos").andExpect(jsonPath("$.campos.responsavel").exists());
        verificarAguardandoGestor(id);
    }

    @Test
    void separacaoConsideraAprovadorHistoricoNaoNovoGestorDeclarado() throws Exception {
        long id = submeter("HOSPEDAGEM", "800.00");
        aprovar(id, "GESTOR", "bruno").andExpect(status().isOk());
        agora(RelogioPoliticaV3DeTeste.AGORA.plusHours(1));
        String corpo = JsonPath.parse(declaracaoGestor())
                .set("$.responsavel", " diego ")
                .set("$.papel", "FINANCEIRO")
                .set("$.etapa", "FINANCEIRO")
                .set("$.gestorImediato", " diego ").jsonString();

        aprovarDeclaracao(id, corpo).andExpect(status().isOk());

        consultar(id)
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.etapaPendente").value(nullValue()))
                .andExpect(jsonPath("$.decididoPor").value("diego"))
                .andExpect(jsonPath("$.decididoEm").value("2026-11-03T11:00:00"))
                .andExpect(jsonPath("$.motivoDecisao").value(nullValue()))
                .andExpect(jsonPath("$.historico", hasSize(2)))
                .andExpect(jsonPath("$.historico[0].etapa").value("GESTOR"))
                .andExpect(jsonPath("$.historico[0].responsavel").value("bruno"))
                .andExpect(jsonPath("$.historico[0].quando").value("2026-11-03T10:00:00"))
                .andExpect(jsonPath("$.historico[1].etapa").value("FINANCEIRO"))
                .andExpect(jsonPath("$.historico[1].responsavel").value("diego"))
                .andExpect(jsonPath("$.historico[1].quando").value("2026-11-03T11:00:00"));
    }

    @Test
    void recusaPapelAusenteOuIncompativelSemRegularizarDeclaracao() throws Exception {
        long id = submeter("TRANSPORTE", "100.00");
        recusarSemAlterar(id, JsonPath.parse(declaracaoGestor()).delete("$.papel").jsonString(),
                422, "papel");
        recusarSemAlterar(id, JsonPath.parse(declaracaoGestor()).set("$.papel", null).jsonString(),
                422, "papel");
        recusarSemAlterar(id, JsonPath.parse(declaracaoGestor()).set("$.papel", "FINANCEIRO").jsonString(),
                422, "papel");
        verificarAguardandoGestor(id);
    }

    @Test
    void recusaVinculoDivergenteSemNormalizarCaixa() throws Exception {
        long id = submeter("TRANSPORTE", "100.00");
        for (String vinculo : new String[] {"outra", "Ana"}) {
            recusarSemAlterar(id, JsonPath.parse(declaracaoGestor())
                    .set("$.colaboradorVinculado", vinculo).jsonString(), 422, "vínculo");
        }
        recusarSemAlterar(id, JsonPath.parse(declaracaoGestor())
                .set("$.gestorImediato", "Bruno").jsonString(), 422, "gestor imediato");
        verificarAguardandoGestor(id);
    }

    @Test
    void exigeEtapaExplicitaSemInferirDoPapel() throws Exception {
        long id = submeter("TRANSPORTE", "100.00");
        recusarSemAlterar(id, JsonPath.parse(declaracaoGestor()).delete("$.etapa").jsonString(),
                422, "etapa");
        recusarSemAlterar(id, JsonPath.parse(declaracaoGestor()).set("$.etapa", null).jsonString(),
                422, "etapa");
        verificarAguardandoGestor(id);
    }

    @Test
    void confereEtapaAntesDaAutorizacao() throws Exception {
        long id = submeter("HOSPEDAGEM", "800.00");
        String corpo = """
                {"responsavel":"ana","papel":"GESTOR","etapa":"FINANCEIRO"}
                """;

        recusarSemAlterar(id, corpo, 409, "FINANCEIRO")
                .andExpect(jsonPath("$.title").value("Transição de status inválida"));
        verificarAguardandoGestor(id);
    }

    @Test
    void rejeitaNoFinanceiroPreservandoGestor() throws Exception {
        long id = submeter("HOSPEDAGEM", "800.00");
        aprovar(id, "GESTOR", "bruno").andExpect(status().isOk());
        agora(RelogioPoliticaV3DeTeste.AGORA.plusHours(1));

        rejeitarDeclaracao(id, declaracaoRejeicao("FINANCEIRO", " carla ",
                "Despesa sem justificativa de serviço"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJEITADA"));

        verificarRejeicao(id, "FINANCEIRO", "carla", "2026-11-03T11:00:00",
                "Despesa sem justificativa de serviço", 2)
                .andExpect(jsonPath("$.historico[0].etapa").value("GESTOR"))
                .andExpect(jsonPath("$.historico[0].resultado").value("APROVADA"))
                .andExpect(jsonPath("$.historico[0].responsavel").value("bruno"))
                .andExpect(jsonPath("$.historico[0].quando").value("2026-11-03T10:00:00"))
                .andExpect(jsonPath("$.historico[0].motivo").value(nullValue()));
        agora(RelogioPoliticaV3DeTeste.AGORA.plusHours(2));
        recusarSemAlterar(id, declaracaoRejeicao("FINANCEIRO", "carla", "Outro motivo"),
                409, "REJEITADA");
        recusarRejeicaoSemAlterar(id, declaracaoRejeicao("FINANCEIRO", "carla", "Outro motivo"),
                409, "REJEITADA");
    }

    @Test
    void rejeitaNoGestorComMotivo() throws Exception {
        long id = submeter("TRANSPORTE", "100.00");

        rejeitarDeclaracao(id, declaracaoRejeicao("GESTOR", " bruno ", "Despesa particular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.motivoDecisao").value("Despesa particular"));

        verificarRejeicao(id, "GESTOR", "bruno", "2026-11-03T10:00:00", "Despesa particular", 1);
        recusarSemAlterar(id, declaracaoGestor(), 409, "REJEITADA");
    }

    @Test
    void recusaRejeicaoSemMotivo() throws Exception {
        long id = submeter("TRANSPORTE", "100.00");
        recusarRejeicaoSemAlterar(id, declaracaoGestor(), 422, "motivo");
        recusarRejeicaoSemAlterar(id, JsonPath.parse(declaracaoGestor())
                .put("$", "motivo", null).jsonString(), 422, "motivo");
        recusarRejeicaoSemAlterar(id, declaracaoRejeicao("GESTOR", "bruno", ""), 422, "motivo");
        verificarAguardandoGestor(id);
    }

    @Test
    void financeiroNaoRejeitaAntesDoGestor() throws Exception {
        long id = submeter("TRANSPORTE", "100.00");
        recusarRejeicaoSemAlterar(id, declaracaoRejeicao("FINANCEIRO", "carla", "Despesa particular"),
                409, "FINANCEIRO");
        verificarAguardandoGestor(id);
    }

    @Test
    void recusaAutorrejeicao() throws Exception {
        long id = submeter("TRANSPORTE", "100.00");
        String corpo = JsonPath.parse(declaracaoRejeicao("GESTOR", " ana ", "Cancelar despesa"))
                .set("$.gestorImediato", " ana ").jsonString();
        recusarRejeicaoSemAlterar(id, corpo, 422, "próprio pedido");
        verificarAguardandoGestor(id);
    }

    @Test
    void gestorNaoRejeitaTambemNoFinanceiro() throws Exception {
        long id = submeter("HOSPEDAGEM", "800.00");
        aprovar(id, "GESTOR", "bruno").andExpect(status().isOk());
        agora(RelogioPoliticaV3DeTeste.AGORA.plusHours(1));
        String corpo = JsonPath.parse(declaracaoRejeicao("FINANCEIRO", " bruno ", "Despesa particular"))
                .set("$.gestorImediato", " diego ").jsonString();
        recusarRejeicaoSemAlterar(id, corpo, 422, "pessoa distinta");
        consultar(id)
                .andExpect(jsonPath("$.status").value("AGUARDANDO_FINANCEIRO"))
                .andExpect(jsonPath("$.etapaPendente").value("FINANCEIRO"))
                .andExpect(jsonPath("$.decididoPor").value("bruno"))
                .andExpect(jsonPath("$.decididoEm").value("2026-11-03T10:00:00"))
                .andExpect(jsonPath("$.motivoDecisao").value(nullValue()))
                .andExpect(jsonPath("$.historico", hasSize(1)))
                .andExpect(jsonPath("$.historico[0].resultado").value("APROVADA"))
                .andExpect(jsonPath("$.historico[0].responsavel").value("bruno"));
    }

    @Test
    void historicoContemSomenteDecisoesAceitas() throws Exception {
        long id = submeter("TRANSPORTE", "100.00");
        String semAtribuicao = JsonPath.parse(declaracaoGestor())
                .set("$.responsavel", "carla").jsonString();
        recusarSemAlterar(id, semAtribuicao, 422, "gestor imediato");
        agora(RelogioPoliticaV3DeTeste.AGORA.plusHours(1));

        rejeitarDeclaracao(id, declaracaoRejeicao("GESTOR", "bruno", "Despesa particular"))
                .andExpect(status().isOk());

        verificarRejeicao(id, "GESTOR", "bruno", "2026-11-03T11:00:00", "Despesa particular", 1);
        mvc.perform(get("/reembolsos").param("colaborador", "ana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status").value("REJEITADA"))
                .andExpect(jsonPath("$[0].historico", hasSize(1)))
                .andExpect(jsonPath("$[0].historico[0].resultado").value("REJEITADA"))
                .andExpect(jsonPath("$[0].historico[0].responsavel").value("bruno"))
                .andExpect(jsonPath("$[0].historico[0].motivo").value("Despesa particular"));
    }

    @Test
    void recusaMotivoSoComEspacos() throws Exception {
        long id = submeter("TRANSPORTE", "100.00");
        recusarRejeicaoSemAlterar(id, declaracaoRejeicao("GESTOR", "bruno", "   "), 422, "motivo");
        verificarAguardandoGestor(id);
    }

    @Test
    void rejeicaoExigeDeclaracoesSemInferirEtapaOuPapel() throws Exception {
        long id = submeter("TRANSPORTE", "100.00");
        String declaracao = declaracaoRejeicao("GESTOR", "bruno", "Despesa particular");
        for (String campo : new String[] {"etapa", "papel", "colaboradorVinculado", "gestorImediato"}) {
            String explicacao = switch (campo) {
                case "colaboradorVinculado" -> "vínculo";
                case "gestorImediato" -> "gestor imediato";
                default -> campo;
            };
            recusarRejeicaoSemAlterar(id, JsonPath.parse(declaracao).delete("$." + campo).jsonString(),
                    422, explicacao);
            recusarRejeicaoSemAlterar(id, JsonPath.parse(declaracao).set("$." + campo, null).jsonString(),
                    422, explicacao);
        }
        for (String campo : new String[] {"colaboradorVinculado", "gestorImediato"}) {
            recusarRejeicaoSemAlterar(id, JsonPath.parse(declaracao).set("$." + campo, "   ").jsonString(),
                    422, campo.equals("colaboradorVinculado") ? "vínculo" : "gestor imediato");
        }
        recusarRejeicaoSemAlterar(id, JsonPath.parse(declaracao)
                .set("$.responsavel", "diego").jsonString(), 422, "gestor imediato");
        recusarRejeicaoSemAlterar(id, JsonPath.parse(declaracao)
                .set("$.papel", "FINANCEIRO").jsonString(), 422, "papel");
        recusarRejeicaoSemAlterar(id, JsonPath.parse(declaracao)
                .set("$.colaboradorVinculado", "outra").jsonString(), 422, "vínculo");
        for (String corpo : new String[] {
                JsonPath.parse(declaracao).delete("$.responsavel").jsonString(),
                JsonPath.parse(declaracao).set("$.responsavel", null).jsonString(),
                JsonPath.parse(declaracao).set("$.responsavel", "   ").jsonString()}) {
            recusarRejeicaoSemAlterar(id, corpo, 400, "inválidos")
                    .andExpect(jsonPath("$.campos.responsavel").exists());
        }
        verificarAguardandoGestor(id);
    }

    @Test
    void rejeicaoFinanceiraRecusadaPreservaAprovacaoAnterior() throws Exception {
        long id = submeter("HOSPEDAGEM", "800.00");
        aprovar(id, "GESTOR", "bruno").andExpect(status().isOk());
        agora(RelogioPoliticaV3DeTeste.AGORA.plusHours(1));
        recusarRejeicaoSemAlterar(id, declaracaoRejeicao("FINANCEIRO", "carla", "   "), 422, "motivo");
        recusarRejeicaoSemAlterar(id, declaracaoRejeicao("GESTOR", "bruno", "Despesa particular"),
                409, "GESTOR");
        recusarRejeicaoSemAlterar(id, JsonPath.parse(declaracaoRejeicao(
                "FINANCEIRO", "carla", "Despesa particular")).set("$.papel", "GESTOR").jsonString(),
                422, "papel");
    }

    private String declaracaoRejeicao(String etapa, String responsavel, String motivo) {
        return JsonPath.parse(declaracaoGestor())
                .set("$.etapa", etapa).set("$.papel", etapa).set("$.responsavel", responsavel)
                .put("$", "motivo", motivo).jsonString();
    }

    private ResultActions rejeitarDeclaracao(long id, String corpo) throws Exception {
        return mvc.perform(post("/reembolsos/{id}/rejeicao", id)
                .contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private ResultActions recusarRejeicaoSemAlterar(long id, String corpo, int codigo, String explicacao)
            throws Exception {
        Object antes = JsonPath.read(consultar(id).andReturn().getResponse().getContentAsString(), "$");
        ResultActions resultado = rejeitarDeclaracao(id, corpo)
                .andExpect(status().is(codigo))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(codigo))
                .andExpect(jsonPath("$.detail").value(containsString(explicacao)));
        Object depois = JsonPath.read(consultar(id).andReturn().getResponse().getContentAsString(), "$");
        assertThat(depois).as("Rejeição recusada não altera pedido, autoria ou histórico").isEqualTo(antes);
        return resultado;
    }

    private ResultActions verificarRejeicao(long id, String etapa, String responsavel, String quando,
                                            String motivo, int eventos) throws Exception {
        String evento = "$.historico[" + (eventos - 1) + "]";
        return consultar(id)
                .andExpect(jsonPath("$.status").value("REJEITADA"))
                .andExpect(jsonPath("$.politica").value("V3"))
                .andExpect(jsonPath("$.etapaPendente").value(nullValue()))
                .andExpect(jsonPath("$.decididoPor").value(responsavel))
                .andExpect(jsonPath("$.decididoEm").value(quando))
                .andExpect(jsonPath("$.motivoDecisao").value(motivo))
                .andExpect(jsonPath("$.historico", hasSize(eventos)))
                .andExpect(jsonPath(evento + ".etapa").value(etapa))
                .andExpect(jsonPath(evento + ".resultado").value("REJEITADA"))
                .andExpect(jsonPath(evento + ".responsavel").value(responsavel))
                .andExpect(jsonPath(evento + ".quando").value(quando))
                .andExpect(jsonPath(evento + ".motivo").value(motivo));
    }

    private String declaracaoGestor() {
        return """
                {"responsavel":" bruno ","papel":"GESTOR","colaboradorVinculado":" ana ",
                 "gestorImediato":" bruno ","etapa":"GESTOR"}
                """;
    }

    private ResultActions aprovarDeclaracao(long id, String corpo) throws Exception {
        return mvc.perform(post("/reembolsos/{id}/aprovacao", id)
                .contentType(MediaType.APPLICATION_JSON).content(corpo));
    }

    private ResultActions recusarSemAlterar(long id, String corpo, int codigo, String explicacao)
            throws Exception {
        Object antes = JsonPath.read(consultar(id).andReturn().getResponse().getContentAsString(), "$");
        ResultActions resultado = aprovarDeclaracao(id, corpo)
                .andExpect(status().is(codigo))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(codigo))
                .andExpect(jsonPath("$.detail").value(containsString(explicacao)));
        Object depois = JsonPath.read(consultar(id).andReturn().getResponse().getContentAsString(), "$");
        assertThat(depois).as("Recusa não altera pedido, autoria ou histórico").isEqualTo(antes);
        return resultado;
    }

    private void verificarAguardandoGestor(long id) throws Exception {
        consultar(id)
                .andExpect(jsonPath("$.status").value("SUBMETIDA"))
                .andExpect(jsonPath("$.etapaPendente").value("GESTOR"))
                .andExpect(jsonPath("$.decididoPor").value(nullValue()))
                .andExpect(jsonPath("$.decididoEm").value(nullValue()))
                .andExpect(jsonPath("$.motivoDecisao").value(nullValue()))
                .andExpect(jsonPath("$.historico", hasSize(0)));
    }

    private void aprovarEmDuasEtapas(String valor) throws Exception {
        long id = submeter("HOSPEDAGEM", valor);
        aprovar(id, "GESTOR", "bruno")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AGUARDANDO_FINANCEIRO"))
                .andExpect(jsonPath("$.etapaPendente").value("FINANCEIRO"))
                .andExpect(jsonPath("$.historico", hasSize(1)));

        consultar(id)
                .andExpect(jsonPath("$.status").value("AGUARDANDO_FINANCEIRO"))
                .andExpect(jsonPath("$.etapaPendente").value("FINANCEIRO"))
                .andExpect(jsonPath("$.decididoPor").value("bruno"))
                .andExpect(jsonPath("$.historico", hasSize(1)))
                .andExpect(jsonPath("$.historico[0].responsavel").value("bruno"))
                .andExpect(jsonPath("$.historico[0].quando").value("2026-11-03T10:00:00"));

        agora(RelogioPoliticaV3DeTeste.AGORA.plusHours(1));
        aprovar(id, "FINANCEIRO", "carla")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.etapaPendente").value(nullValue()));

        consultar(id)
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.politica").value("V3"))
                .andExpect(jsonPath("$.etapaPendente").value(nullValue()))
                .andExpect(jsonPath("$.decididoPor").value("carla"))
                .andExpect(jsonPath("$.decididoEm").value("2026-11-03T11:00:00"))
                .andExpect(jsonPath("$.motivoDecisao").value(nullValue()))
                .andExpect(jsonPath("$.historico", hasSize(2)))
                .andExpect(jsonPath("$.historico[0].etapa").value("GESTOR"))
                .andExpect(jsonPath("$.historico[0].resultado").value("APROVADA"))
                .andExpect(jsonPath("$.historico[0].responsavel").value("bruno"))
                .andExpect(jsonPath("$.historico[0].quando").value("2026-11-03T10:00:00"))
                .andExpect(jsonPath("$.historico[0].motivo").value(nullValue()))
                .andExpect(jsonPath("$.historico[1].etapa").value("FINANCEIRO"))
                .andExpect(jsonPath("$.historico[1].resultado").value("APROVADA"))
                .andExpect(jsonPath("$.historico[1].responsavel").value("carla"))
                .andExpect(jsonPath("$.historico[1].quando").value("2026-11-03T11:00:00"))
                .andExpect(jsonPath("$.historico[1].motivo").value(nullValue()));

        mvc.perform(get("/reembolsos").param("colaborador", "ana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].politica").value("V3"))
                .andExpect(jsonPath("$[0].historico", hasSize(2)))
                .andExpect(jsonPath("$[0].historico[0].responsavel").value("bruno"))
                .andExpect(jsonPath("$[0].historico[1].responsavel").value("carla"));
    }

    private long submeter(String categoria, String valor) throws Exception {
        ResultActions resultado = enviar(categoria, valor)
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("SUBMETIDA"))
                .andExpect(jsonPath("$.politica").value("V3"))
                .andExpect(jsonPath("$.etapaPendente").value("GESTOR"))
                .andExpect(jsonPath("$.historico", hasSize(0)))
                .andExpect(jsonPath("$.versao").doesNotExist());
        String corpo = resultado.andReturn().getResponse().getContentAsString();
        long id = ((Number) JsonPath.read(corpo, "$.id")).longValue();
        resultado.andExpect(header().string("Location", "/reembolsos/" + id));
        consultar(id).andExpect(jsonPath("$.valor").value(comparesEqualTo(new BigDecimal(valor)), BigDecimal.class));
        return id;
    }

    private ResultActions enviar(String categoria, String valor) throws Exception {
        return mvc.perform(post("/reembolsos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"colaborador":"ana","categoria":"%s","valor":%s,
                         "dataDespesa":"2026-11-02","cnpjFornecedor":"11222333000181"}
                        """.formatted(categoria, valor)));
    }

    private ResultActions aprovar(long id, String etapa, String responsavel) throws Exception {
        return mvc.perform(post("/reembolsos/{id}/aprovacao", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"responsavel":"%s","papel":"%s","colaboradorVinculado":" ana ",
                         "gestorImediato":" bruno ","etapa":"%s","motivo":"não registrar na aprovação"}
                        """.formatted(responsavel, etapa, etapa)));
    }

    private ResultActions consultar(long id) throws Exception {
        return mvc.perform(get("/reembolsos/{id}", id)).andExpect(status().isOk());
    }

    private void agora(LocalDateTime quando) {
        doReturn(quando.atZone(ClockConfig.FUSO_PADRAO).toInstant()).when(clock).instant();
    }
}

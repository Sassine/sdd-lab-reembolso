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

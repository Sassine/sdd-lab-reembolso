package com.orbe.reembolso.api;

import static com.orbe.reembolso.suporte.RelogioDeTeste.HOJE;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.orbe.reembolso.servico.ReembolsoRepository;
import com.orbe.reembolso.suporte.RelogioDeTeste;

/** Spec 002 pela API: CA1–CA9, CA15 e o papel obrigatório. Ana solicita, Bruno é gestor, Carla é do Financeiro. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(RelogioDeTeste.class)
class AprovacaoPorAlcadaApiTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ReembolsoRepository repositorio;

    @BeforeEach
    void limpar() {
        repositorio.deleteAll();
    }

    @Test
    void ca1_ateQuinhentosSoGestor() throws Exception {
        long id = pedidoDaAna("500.00");

        aprovar(id, "bruno", "GESTOR")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.aprovadorGestor").value("bruno"))
                .andExpect(jsonPath("$.decididoPor").value("bruno"));
    }

    @Test
    void ca2_ca3_acimaDeQuinhentosPassaPeloFinanceiro() throws Exception {
        long id = pedidoDaAna("500.01");

        aprovar(id, "bruno", "GESTOR")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AGUARDANDO_FINANCEIRO"))
                .andExpect(jsonPath("$.decididoEm").value(nullValue()));

        aprovar(id, "carla", "FINANCEIRO")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.aprovadorGestor").value("bruno"))
                .andExpect(jsonPath("$.aprovadorFinanceiro").value("carla"))
                .andExpect(jsonPath("$.decididoPor").value("carla"));
    }

    @Test
    void ca4_acimaDeCincoMilNaoEReembolso() throws Exception {
        submeter("ana", "5000.01")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value(containsString("adiantamento")));

        mvc.perform(get("/reembolsos").param("colaborador", "ana"))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void ca5_cincoMilExatosAindaEReembolso() throws Exception {
        submeter("ana", "5000.00")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUBMETIDA"));
    }

    @Test
    void ca6_financeiroNaoAprovaAntesDoGestor() throws Exception {
        long id = pedidoDaAna("800.00");

        aprovar(id, "carla", "FINANCEIRO")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Transição de status inválida"));

        mvc.perform(get("/reembolsos/{id}", id)).andExpect(jsonPath("$.status").value("SUBMETIDA"));
    }

    @Test
    void ca7_gestorNaoAprovaDeNovo() throws Exception {
        long id = pedidoDaAna("800.00");
        aprovar(id, "bruno", "GESTOR").andExpect(status().isOk());

        aprovar(id, "davi", "GESTOR").andExpect(status().isConflict());
    }

    @Test
    void ca8_ninguemAprovaOProprioPedido() throws Exception {
        long id = pedidoDaAna("100.00");

        aprovar(id, " Ana ", "GESTOR")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value(containsString("não pode aprová-lo")));
    }

    @Test
    void ca9_financeiroNaoPodeSerOGestor() throws Exception {
        long id = pedidoDaAna("800.00");
        aprovar(id, "bruno", "GESTOR").andExpect(status().isOk());

        aprovar(id, "bruno", "FINANCEIRO").andExpect(status().isUnprocessableEntity());

        mvc.perform(get("/reembolsos/{id}", id))
                .andExpect(jsonPath("$.status").value("AGUARDANDO_FINANCEIRO"))
                .andExpect(jsonPath("$.aprovadorFinanceiro").value(nullValue()));
    }

    @Test
    void ca15_financeiroRejeitaPedidoAguardando() throws Exception {
        long id = pedidoDaAna("800.00");
        aprovar(id, "bruno", "GESTOR").andExpect(status().isOk());

        mvc.perform(post("/reembolsos/{id}/rejeicao", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"responsavel": "carla", "motivo": "Nota fiscal de outra empresa"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJEITADA"))
                .andExpect(jsonPath("$.motivoDecisao").value("Nota fiscal de outra empresa"));
    }

    @Test
    void papelEObrigatorio() throws Exception {
        long id = pedidoDaAna("100.00");

        mvc.perform(post("/reembolsos/{id}/aprovacao", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"responsavel": "bruno"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.papel").exists());
    }

    private long pedidoDaAna(String valor) throws Exception {
        String corpo = submeter("ana", valor).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(corpo, "$.id")).longValue();
    }

    private ResultActions submeter(String colaborador, String valor) throws Exception {
        return mvc.perform(post("/reembolsos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"colaborador": "%s", "categoria": "OUTROS", "valor": %s,
                         "dataDespesa": "%s", "cnpjFornecedor": "11222333000181"}
                        """.formatted(colaborador, valor, HOJE.minusDays(2))));
    }

    private ResultActions aprovar(long id, String responsavel, String papel) throws Exception {
        return mvc.perform(post("/reembolsos/{id}/aprovacao", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"responsavel": "%s", "papel": "%s"}
                        """.formatted(responsavel, papel)));
    }
}

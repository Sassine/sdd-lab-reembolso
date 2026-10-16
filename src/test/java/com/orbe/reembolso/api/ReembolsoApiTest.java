package com.orbe.reembolso.api;

import static com.orbe.reembolso.suporte.RelogioDeTeste.HOJE;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

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

@SpringBootTest
@AutoConfigureMockMvc
@Import(RelogioDeTeste.class)
class ReembolsoApiTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ReembolsoRepository repositorio;

    @BeforeEach
    void limpar() {
        repositorio.deleteAll();
    }

    @Test
    void submeteReembolsoValido() throws Exception {
        submeter("ana", "REFEICAO", "85.50", HOJE.minusDays(3), "11222333000181")
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("SUBMETIDA"))
                .andExpect(jsonPath("$.valor").value(85.50));
    }

    @Test
    void recusaCnpjMalFormatado() throws Exception {
        submeter("ana", "REFEICAO", "85.50", HOJE, "123")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.cnpjFornecedor").exists());
    }

    @Test
    void recusaReembolsoDuplicado() throws Exception {
        submeter("ana", "TRANSPORTE", "42.00", HOJE.minusDays(1), "11222333000181").andExpect(status().isCreated());
        submeter("ana", "TRANSPORTE", "42.00", HOJE.minusDays(1), "11222333000181")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Reembolso duplicado"));
    }

    @Test
    void recusaDespesaForaDoPrazo() throws Exception {
        submeter("ana", "HOSPEDAGEM", "300.00", HOJE.minusDays(90), "11222333000181")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("Prazo")));
    }

    @Test
    void aprovaReembolsoSubmetido() throws Exception {
        long id = idDe(submeter("ana", "OUTROS", "120.00", HOJE.minusDays(2), "11222333000181"));

        decidir(id, "aprovacao", "bruno")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.decididoPor").value("bruno"));
    }

    @Test
    void naoAprovaDuasVezes() throws Exception {
        long id = idDe(submeter("ana", "OUTROS", "120.00", HOJE.minusDays(2), "11222333000181"));
        decidir(id, "aprovacao", "bruno").andExpect(status().isOk());

        decidir(id, "aprovacao", "carla")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Transição de status inválida"));
    }

    @Test
    void rejeitaComMotivo() throws Exception {
        long id = idDe(submeter("ana", "OUTROS", "120.00", HOJE.minusDays(2), "11222333000181"));

        mvc.perform(post("/reembolsos/{id}/rejeicao", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"responsavel": "bruno", "motivo": "Nota fiscal ilegível"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJEITADA"))
                .andExpect(jsonPath("$.motivoDecisao").value("Nota fiscal ilegível"));
    }

    @Test
    void listaPorColaborador() throws Exception {
        submeter("ana", "OUTROS", "10.00", HOJE.minusDays(5), "11222333000181");
        submeter("ana", "OUTROS", "20.00", HOJE.minusDays(1), "11222333000181");
        submeter("bia", "OUTROS", "30.00", HOJE.minusDays(1), "11222333000181");

        mvc.perform(get("/reembolsos").param("colaborador", "ana"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].valor").value(20.00));
    }

    @Test
    void retorna404ParaIdInexistente() throws Exception {
        mvc.perform(get("/reembolsos/{id}", 999_999))
                .andExpect(status().isNotFound());
    }

    private ResultActions submeter(String colaborador, String categoria, String valor, LocalDate data, String cnpj)
            throws Exception {
        return mvc.perform(post("/reembolsos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"colaborador": "%s", "categoria": "%s", "valor": %s,
                         "dataDespesa": "%s", "cnpjFornecedor": "%s", "descricao": "teste"}
                        """.formatted(colaborador, categoria, valor, data, cnpj)));
    }

    private ResultActions decidir(long id, String acao, String responsavel) throws Exception {
        return mvc.perform(post("/reembolsos/{id}/" + acao, id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"responsavel": "%s"}
                        """.formatted(responsavel)));
    }

    private long idDe(ResultActions resultado) throws Exception {
        String corpo = resultado.andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(corpo, "$.id")).longValue();
    }
}

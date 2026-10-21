package com.orbe.reembolso.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.jayway.jsonpath.JsonPath;
import com.orbe.reembolso.dominio.Categoria;
import com.orbe.reembolso.dominio.Reembolso;
import com.orbe.reembolso.dominio.StatusReembolso;
import com.orbe.reembolso.servico.ReembolsoRepository;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:vigencia;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
@Import(ReembolsoVigenciaApiTest.Configuracao.class)
class ReembolsoVigenciaApiTest {

    private static final LocalDate DESPESA = LocalDate.of(2026, 10, 31);
    private static final LocalDateTime ANTES = LocalDateTime.of(2026, 10, 31, 23, 59);
    private static final Instant CORTE = Instant.parse("2026-11-01T03:00:00Z");
    private static final Instant DEPOIS = Instant.parse("2026-11-02T15:00:00Z");
    private static final String MOMENTO_DECISAO = "2026-11-02T12:00:00";

    @Autowired
    MockMvc mvc;

    @Autowired
    ReembolsoRepository repositorio;

    @Autowired
    RelogioContextual clock;

    private String colaborador;

    @BeforeEach
    void preparar(TestInfo teste) {
        // Cada teste tem dados próprios; não apaga dados de outra execução paralela.
        colaborador = teste.getTestMethod().orElseThrow().getName();
        clock.fixar(CORTE);
    }

    @AfterEach
    void liberarRelogio() {
        clock.limpar();
    }

    @Test
    void aplicaV3NaSubmissaoExatamenteNoCorte() throws Exception {
        long id = idDe(submeter("HOSPEDAGEM", "800.00", DESPESA, "11222333000181")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.politica").value("V3"))
                .andExpect(jsonPath("$.submetidoEm").value("2026-11-01T00:00:00"))
                .andExpect(jsonPath("$.dataDespesa").value("2026-10-31"))
                .andExpect(jsonPath("$.status").value("SUBMETIDA"))
                .andExpect(jsonPath("$.etapaPendente").value("GESTOR"))
                .andExpect(jsonPath("$.historico", hasSize(0))));
        assertThat(clock.leituras()).isEqualTo(1);

        clock.fixar(DEPOIS);
        aprovarV3(id, "GESTOR", "gestor")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AGUARDANDO_FINANCEIRO"))
                .andExpect(jsonPath("$.etapaPendente").value("FINANCEIRO"))
                .andExpect(jsonPath("$.historico", hasSize(1)));
        aprovarV3(id, "FINANCEIRO", "financeiro")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"));

        consultar(id)
                .andExpect(jsonPath("$.politica").value("V3"))
                .andExpect(jsonPath("$.submetidoEm").value("2026-11-01T00:00:00"))
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.etapaPendente").value(nullValue()))
                .andExpect(jsonPath("$.decididoPor").value("financeiro"))
                .andExpect(jsonPath("$.historico", hasSize(2)))
                .andExpect(jsonPath("$.historico[0].etapa").value("GESTOR"))
                .andExpect(jsonPath("$.historico[0].responsavel").value("gestor"))
                .andExpect(jsonPath("$.historico[0].resultado").value("APROVADA"))
                .andExpect(jsonPath("$.historico[0].quando").value(MOMENTO_DECISAO))
                .andExpect(jsonPath("$.historico[1].etapa").value("FINANCEIRO"))
                .andExpect(jsonPath("$.historico[1].responsavel").value("financeiro"))
                .andExpect(jsonPath("$.historico[1].resultado").value("APROVADA"))
                .andExpect(jsonPath("$.historico[1].quando").value(MOMENTO_DECISAO));
        assertThat(repositorio.findById(id).orElseThrow().getSubmetidoEm())
                .isEqualTo(LocalDateTime.of(2026, 11, 1, 0, 0));
    }

    @Test
    void aprovaLegadoAcimaDe5000DepoisDoCorte() throws Exception {
        long id = legado(Categoria.HOSPEDAGEM, "5000.01", DESPESA, false);
        clock.fixar(DEPOIS);

        decidirLegado(id, "aprovacao")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.motivoDecisao").value(nullValue()))
                .andExpect(jsonPath("$.historico", hasSize(0)))
                .andExpect(jsonPath("$.valor").value(5000.01))
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.decididoPor").value("responsavel-antigo"))
                .andExpect(jsonPath("$.decididoEm").value(MOMENTO_DECISAO));
        verificarLegado(id, "APROVADA")
                .andExpect(jsonPath("$.submetidoEm").value("2026-10-31T23:59:00"))
                .andExpect(jsonPath("$.decididoPor").value("responsavel-antigo"));
        decidirLegado(id, "aprovacao").andExpect(status().isConflict());
        verificarLegado(id, "APROVADA")
                .andExpect(jsonPath("$.decididoEm").value(MOMENTO_DECISAO));
        assertThat(repositorio.findById(id).orElseThrow().getStatus()).isEqualTo(StatusReembolso.APROVADA);
    }

    @Test
    void refeicaoLegadaNaoConsomeTetoV3() throws Exception {
        long antigo = legado(Categoria.REFEICAO, "100.00", DESPESA, false);
        long novo = idDe(submeter("REFEICAO", "120.00", DESPESA, "11222333000182")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valor").value(120.00))
                .andExpect(jsonPath("$.politica").value("V3"))
                .andExpect(jsonPath("$.etapaPendente").value("GESTOR")));

        submeter("REFEICAO", "0.01", DESPESA, "11222333000183")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value(containsString("120")));
        verificarLegado(antigo, "SUBMETIDA");
        clock.fixar(DEPOIS);
        decidirLegado(antigo, "aprovacao").andExpect(status().isOk());
        aprovarV3(novo, "GESTOR", "gestor")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.historico", hasSize(1)));
        verificarLegado(antigo, "APROVADA");
        consultar(novo)
                .andExpect(jsonPath("$.politica").value("V3"))
                .andExpect(jsonPath("$.valor").value(120.00))
                .andExpect(jsonPath("$.status").value("APROVADA"));
        assertThat(repositorio.findByColaboradorOrderByDataDespesaDescIdDesc(colaborador)).hasSize(2);
    }

    @Test
    void consultaLegadoSemInventarEtapas() throws Exception {
        long id = legado(Categoria.HOSPEDAGEM, "800.00", DESPESA, true);
        clock.fixar(DEPOIS);

        verificarLegado(id, "APROVADA")
                .andExpect(jsonPath("$.valor").value(800.00))
                .andExpect(jsonPath("$.submetidoEm").value("2026-10-31T23:59:00"))
                .andExpect(jsonPath("$.decididoPor").value("autor-anterior"))
                .andExpect(jsonPath("$.decididoEm").value("2026-10-31T23:59:30"))
                .andExpect(jsonPath("$.motivoDecisao").value(nullValue()));
        Reembolso persistido = repositorio.findById(id).orElseThrow();
        assertThat(persistido.getStatus()).isEqualTo(StatusReembolso.APROVADA);
        assertThat(persistido.getDecididoPor()).isEqualTo("autor-anterior");
        assertThat(persistido.getHistorico()).isEmpty();
    }

    @Test
    void imediatamenteAntesDoCorteAindaAplicaV2() throws Exception {
        clock.fixar(CORTE.minusNanos(1_000)); // Precisão persistida de microssegundos.
        long id = idDe(submeter("HOSPEDAGEM", "5000.01", DESPESA, "11222333000181")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.politica").value("V2"))
                .andExpect(jsonPath("$.submetidoEm").value("2026-10-31T23:59:59.999999"))
                .andExpect(jsonPath("$.etapaPendente").value(nullValue()))
                .andExpect(jsonPath("$.historico", hasSize(0))));
        assertThat(clock.leituras()).isEqualTo(1);

        clock.fixar(DEPOIS);
        decidirLegado(id, "aprovacao").andExpect(status().isOk());
        verificarLegado(id, "APROVADA")
                .andExpect(jsonPath("$.submetidoEm").value("2026-10-31T23:59:59.999999"));
    }

    @Test
    void rejeitaLegadoSemMotivoDepoisDaVigencia() throws Exception {
        long id = legado(Categoria.HOSPEDAGEM, "800.00", DESPESA, false);
        clock.fixar(DEPOIS);

        decidirLegado(id, "rejeicao")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJEITADA"))
                .andExpect(jsonPath("$.decididoPor").value("responsavel-antigo"))
                .andExpect(jsonPath("$.decididoEm").value(MOMENTO_DECISAO))
                .andExpect(jsonPath("$.etapaPendente").value(nullValue()))
                .andExpect(jsonPath("$.historico", hasSize(0)))
                .andExpect(jsonPath("$.motivoDecisao").value(nullValue()));
        verificarLegado(id, "REJEITADA")
                .andExpect(jsonPath("$.decididoPor").value("responsavel-antigo"))
                .andExpect(jsonPath("$.decididoEm").value(MOMENTO_DECISAO));
        assertThat(repositorio.findById(id).orElseThrow().getStatus()).isEqualTo(StatusReembolso.REJEITADA);
    }

    @Test
    void listagemPreservaLegadosEOrdenaPorDataDespesaEIdDecrescentes() throws Exception {
        long primeiroNoDia = legado(Categoria.HOSPEDAGEM, "900.00", DESPESA, true);
        long ultimoNoDia = legado(Categoria.HOSPEDAGEM, "1000.00", DESPESA, false);
        long maisAntigo = legado(Categoria.HOSPEDAGEM, "800.00", DESPESA.minusDays(1), true);
        clock.fixar(DEPOIS);

        mvc.perform(get("/reembolsos").param("colaborador", colaborador))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id").value(ultimoNoDia))
                .andExpect(jsonPath("$[1].id").value(primeiroNoDia))
                .andExpect(jsonPath("$[2].id").value(maisAntigo))
                .andExpect(jsonPath("$[0].status").value("SUBMETIDA"))
                .andExpect(jsonPath("$[0].decididoPor").value(nullValue()))
                .andExpect(jsonPath("$[1].status").value("APROVADA"))
                .andExpect(jsonPath("$[1].decididoPor").value("autor-anterior"))
                .andExpect(jsonPath("$[1].decididoEm").value("2026-10-31T23:59:30"))
                .andExpect(jsonPath("$[0].politica").value("V2"))
                .andExpect(jsonPath("$[1].politica").value("V2"))
                .andExpect(jsonPath("$[2].politica").value("V2"))
                .andExpect(jsonPath("$[0].etapaPendente").value(nullValue()))
                .andExpect(jsonPath("$[1].etapaPendente").value(nullValue()))
                .andExpect(jsonPath("$[2].etapaPendente").value(nullValue()))
                .andExpect(jsonPath("$[0].historico", hasSize(0)))
                .andExpect(jsonPath("$[1].historico", hasSize(0)))
                .andExpect(jsonPath("$[2].historico", hasSize(0)));
        verificarLegado(primeiroNoDia, "APROVADA")
                .andExpect(jsonPath("$.decididoPor").value("autor-anterior"));
    }

    @Test
    void preservaPrazoDe60DiasEDuplicidadeNaVigencia() throws Exception {
        // Às 02:59 UTC já é novembro no Clock, mas ainda é outubro em Brasília.
        clock.fixar(CORTE.minusSeconds(60));
        long id = idDe(submeter("OUTROS", "10.00", DESPESA.minusDays(59), "11222333000181")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.politica").value("V2"))
                .andExpect(jsonPath("$.submetidoEm").value("2026-10-31T23:59:00")));
        assertThat(clock.leituras()).isEqualTo(1);
        submeter("OUTROS", "20.00", DESPESA.minusDays(60), "11222333000182")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value(containsString("Prazo")));

        clock.fixar(CORTE);
        submeter("OUTROS", "10.00", DESPESA.minusDays(59), "11222333000181")
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value(containsString("Prazo")));
        // A duplicidade continua valendo inclusive entre regimes, para despesa no prazo.
        long legado = legado(Categoria.HOSPEDAGEM, "800.00", DESPESA, false);
        submeter("HOSPEDAGEM", "800.00", DESPESA, "11222333000181")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Reembolso duplicado"));
        verificarLegado(id, "SUBMETIDA");
        verificarLegado(legado, "SUBMETIDA");
        assertThat(repositorio.findByColaboradorOrderByDataDespesaDescIdDesc(colaborador)).hasSize(2);
    }

    private long legado(Categoria categoria, String valor, LocalDate data, boolean aprovado) {
        Reembolso reembolso = new Reembolso(colaborador, categoria, new BigDecimal(valor), data,
                "11222333000181", "fixture v2", ANTES);
        if (aprovado) {
            reembolso.aprovar("autor-anterior", ANTES.plusSeconds(30));
        }
        return repositorio.saveAndFlush(reembolso).getId();
    }

    private ResultActions submeter(String categoria, String valor, LocalDate data, String cnpj) throws Exception {
        return mvc.perform(post("/reembolsos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"colaborador":"%s","categoria":"%s","valor":%s,
                         "dataDespesa":"%s","cnpjFornecedor":"%s","descricao":"teste vigencia"}
                        """.formatted(colaborador, categoria, valor, data, cnpj)));
    }

    private ResultActions aprovarV3(long id, String etapa, String responsavel) throws Exception {
        return mvc.perform(post("/reembolsos/{id}/aprovacao", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"responsavel":"%s","etapa":"%s","papel":"%s",
                         "colaboradorVinculado":"%s","gestorImediato":"gestor"}
                        """.formatted(responsavel, etapa, etapa, colaborador)));
    }

    private ResultActions decidirLegado(long id, String acao) throws Exception {
        return mvc.perform(post("/reembolsos/{id}/" + acao, id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"responsavel":"responsavel-antigo"}
                        """));
    }

    private ResultActions consultar(long id) throws Exception {
        return mvc.perform(get("/reembolsos/{id}", id)).andExpect(status().isOk());
    }

    private ResultActions verificarLegado(long id, String estado) throws Exception {
        return consultar(id)
                .andExpect(jsonPath("$.politica").value("V2"))
                .andExpect(jsonPath("$.status").value(estado))
                .andExpect(jsonPath("$.etapaPendente").value(nullValue()))
                .andExpect(jsonPath("$.historico", hasSize(0)));
    }

    private long idDe(ResultActions resultado) throws Exception {
        return ((Number) JsonPath.read(resultado.andReturn().getResponse().getContentAsString(), "$.id"))
                .longValue();
    }

    @TestConfiguration
    static class Configuracao {
        @Bean
        @Primary
        RelogioContextual relogioVigencia() {
            return new RelogioContextual();
        }
    }

    /** Contexto por thread: não modifica relógios de outros testes ou classes. */
    static class RelogioContextual extends Clock {
        private final ThreadLocal<Instant> instante = new ThreadLocal<>();
        private final ThreadLocal<Integer> leituras = ThreadLocal.withInitial(() -> 0);

        void fixar(Instant agora) {
            instante.set(agora);
            leituras.set(0);
        }

        int leituras() {
            return leituras.get();
        }

        void limpar() {
            instante.remove();
            leituras.remove();
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return Clock.fixed(instant(), zone);
        }

        @Override
        public Instant instant() {
            Instant agora = instante.get();
            if (agora == null) {
                throw new IllegalStateException("Defina o instante no contexto deste teste");
            }
            leituras.set(leituras.get() + 1);
            return agora;
        }
    }
}

package com.orbe.reembolso.servico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockingDetails;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbe.reembolso.dominio.Categoria;
import com.orbe.reembolso.dominio.Reembolso;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PessimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@AutoConfigureMockMvc
@Import(ReembolsoConcorrenciaTest.Relogio.class)
class ReembolsoConcorrenciaTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 11, 2, 12, 0);
    @TestConfiguration
    static class Relogio {
        @Bean
        @Primary
        Clock clockConcorrencia() {
            return Clock.fixed(Instant.parse("2026-11-02T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));
        }
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired PlatformTransactionManager gerenciador;
    @Autowired EntityManager em;
    @Autowired JdbcTemplate jdbc;
    @MockitoSpyBean ReembolsoRepository repositorio;
    @MockitoSpyBean ControleDeTetoDiario controle;
    private TransactionTemplate transacao;

    @BeforeEach
    void preparar() {
        transacao = new TransactionTemplate(gerenciador);
        transacao.executeWithoutResult(tx -> {
            em.createQuery("delete from DecisaoReembolso").executeUpdate();
            em.createQuery("delete from Reembolso").executeUpdate();
        });
    }

    @Test
    void aceitaUmaDasAprovacoesDaMesmaEtapa() throws Exception {
        disputar(criar("100.00", false), "GESTOR", false, false);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void preservaAutoriaDoGestorVencedorAposRollback(boolean rejeitarSegundo) throws Exception {
        disputar(criar("100.00", false), "GESTOR", rejeitarSegundo, false, "gestor-a", "gestor-b");
    }

    @Test
    void resolveDisputaEntreAprovacaoERejeicao() throws Exception {
        disputar(criar("100.00", false), "GESTOR", true, false);
    }

    @Test
    void naoReinterpretaEtapaConcorrente() throws Exception {
        disputar(criar("800.00", false), "GESTOR", false, false);
    }

    @Test
    void protegeEtapaFinanceira() throws Exception {
        long id = criar("800.00", false);
        assertThat(decidir(id, "GESTOR", "gestor-inicial", false, false)
                .getResponse().getStatus()).isEqualTo(200);
        disputar(id, "FINANCEIRO", true, false);
    }

    @Test
    void protegeDecisaoUnicaLegada() throws Exception {
        disputar(criar("5000.01", true), null, false, true);
    }

    @Test
    void financeiroObservandoSubmetidaFalhaAntesDeEsperarGestor() throws Exception {
        long id = transacao.execute(tx -> repositorio.saveAndFlush(new Reembolso(
                "solicitante", Categoria.REFEICAO, new BigDecimal("80.00"),
                AGORA.toLocalDate(), "12345678000190", "Etapa explícita", AGORA)).getId());
        doAnswer(invocacao -> {
            throw new AssertionError("Etapa incompatível não pode esperar o lock para ser reinterpretada");
        }).when(controle).garantirExistencia(any());
        for (boolean rejeitar : new boolean[] {false, true}) {
            MvcResult resultado = decidir(id, "FINANCEIRO", "financeiro", rejeitar, false);
            assertThat(resultado.getResponse().getStatus()).isEqualTo(409);
            assertThat(resultado.getResponse().getContentType()).startsWith("application/problem+json");
            assertThat(json.readTree(resultado.getResponse().getContentAsString()).path("status").asInt())
                    .isEqualTo(409);
        }
        transacao.executeWithoutResult(tx -> {
            Reembolso atual = repositorio.findById(id).orElseThrow();
            assertThat(atual.getStatus().name()).isEqualTo("SUBMETIDA");
            assertThat(atual.getHistorico()).isEmpty();
            assertThat(atual.getDecididoPor()).isNull();
            assertThat(atual.getDecididoEm()).isNull();
            assertThat(atual.getMotivoDecisao()).isNull();
            assertThat(versao(id)).isZero();
        });
    }

    @ParameterizedTest
    @MethodSource("conflitosDePersistencia")
    void traduzExcecoesDeVersaoELockSemDeixarDecisaoParcial(RuntimeException falha) throws Exception {
        long id = criar("100.00", false);
        doThrow(falha).when(repositorio).saveAndFlush(any(Reembolso.class));
        MvcResult resultado = decidir(id, "GESTOR", "gestor", false, false);
        assertThat(resultado.getResponse().getStatus()).isEqualTo(409);
        assertThat(resultado.getResponse().getContentType()).startsWith("application/problem+json");
        JsonNode problema = json.readTree(resultado.getResponse().getContentAsString());
        assertThat(problema.path("status").asInt()).isEqualTo(409);
        assertThat(problema.path("type").asText()).isEqualTo("about:blank");
        assertThat(problema.path("title").asText()).isEqualTo("Conflito de concorrência");
        assertThat(problema.path("detail").asText())
                .isEqualTo("Outra operação disputou este pedido. Consulte o estado atual antes de decidir novamente.");
        conferirSemDecisao(id);
    }

    static Stream<RuntimeException> conflitosDePersistencia() {
        return Stream.of(new ObjectOptimisticLockingFailureException(Reembolso.class, 1L),
                new OptimisticLockException("versão concorrente"),
                new CannotAcquireLockException("lock concorrente"),
                new PessimisticLockException("lock concorrente"),
                new LockTimeoutException("timeout do lock"));
    }

    @Test
    void naoConverteFalhaSqlGenericaEmConflitoNemDeixaEscritaParcial() {
        long id = criar("100.00", false);
        var falha = new DataIntegrityViolationException("falha SQL não relacionada à concorrência");
        doThrow(falha).when(repositorio).saveAndFlush(any(Reembolso.class));
        assertThatThrownBy(() -> decidir(id, "GESTOR", "gestor", false, false))
                .hasCause(falha);
        conferirSemDecisao(id);
    }

    private void conferirSemDecisao(long id) {
        transacao.executeWithoutResult(tx -> {
            Reembolso atual = repositorio.findById(id).orElseThrow();
            assertThat(atual.getStatus().name()).isEqualTo("SUBMETIDA");
            assertThat(atual.getHistorico()).isEmpty();
            assertThat(atual.getDecididoPor()).isNull();
            assertThat(atual.getDecididoEm()).isNull();
            assertThat(atual.getMotivoDecisao()).isNull();
            assertThat(versao(id)).isZero();
            assertThat(jdbc.queryForObject("select count(*) from decisao_reembolso", Long.class)).isZero();
        });
    }

    private long criar(String valor, boolean legado) {
        return transacao.execute(tx -> repositorio.saveAndFlush(new Reembolso(
                "solicitante", Categoria.TRANSPORTE, new BigDecimal(valor),
                LocalDate.of(2026, 10, 31), "12345678000190", "Corrida T014",
                legado ? LocalDateTime.of(2026, 10, 31, 23, 59) : AGORA)).getId());
    }

    private void disputar(long id, String etapa, boolean rejeitarSegundo, boolean legado) throws Exception {
        String autorA = "GESTOR".equals(etapa) ? "gestor" : "responsavel-a";
        String autorB = "GESTOR".equals(etapa) ? "gestor" : "responsavel-b";
        disputar(id, etapa, rejeitarSegundo, legado, autorA, autorB);
    }

    private void disputar(long id, String etapa, boolean rejeitarSegundo, boolean legado,
            String autorA, String autorB) throws Exception {
        var barreira = new CyclicBarrier(2);
        var contextos = new ConcurrentLinkedQueue<Object>();
        var versoesLidas = new ConcurrentLinkedQueue<Long>();
        var sincronizar = new ThreadLocal<Boolean>();
        var delegado = mockingDetails(repositorio).getMockCreationSettings().getDefaultAnswer();
        doAnswer(invocacao -> {
            @SuppressWarnings("unchecked")
            Optional<Reembolso> encontrado = (Optional<Reembolso>) delegado.answer(invocacao);
            if (Boolean.TRUE.equals(sincronizar.get()) && encontrado.isPresent()) {
                // Ambas as transações carregam a mesma versão e o histórico antes de escrever.
                encontrado.get().getHistorico().size();
                contextos.add(em.getDelegate());
                versoesLidas.add(versao(id));
                barreira.await(10, TimeUnit.SECONDS);
            }
            return encontrado;
        }).when(repositorio).findById(anyLong());

        long versaoAntes = versao(id);
        var executor = Executors.newFixedThreadPool(2);
        MvcResult primeiro;
        MvcResult segundo;
        try {
            var a = executor.submit(() -> {
                sincronizar.set(true);
                try { return decidir(id, etapa, autorA, false, legado); }
                finally { sincronizar.remove(); }
            });
            var b = executor.submit(() -> {
                sincronizar.set(true);
                try { return decidir(id, etapa, autorB, rejeitarSegundo, legado); }
                finally { sincronizar.remove(); }
            });
            primeiro = a.get(20, TimeUnit.SECONDS);
            segundo = b.get(20, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }
        assertThat(contextos).hasSize(2);
        assertThat(contextos.toArray()[0]).isNotSameAs(contextos.toArray()[1]);
        assertThat(versoesLidas).containsExactly(versaoAntes, versaoAntes);
        assertThat(new int[] {primeiro.getResponse().getStatus(), segundo.getResponse().getStatus()})
                .containsExactlyInAnyOrder(200, 409);
        MvcResult vencedor = primeiro.getResponse().getStatus() == 200 ? primeiro : segundo;
        MvcResult perdedor = vencedor == primeiro ? segundo : primeiro;
        JsonNode aceito = json.readTree(vencedor.getResponse().getContentAsString());
        JsonNode conflito = json.readTree(perdedor.getResponse().getContentAsString());
        assertThat(perdedor.getResponse().getContentType()).startsWith("application/problem+json");
        assertThat(conflito.path("status").asInt()).isEqualTo(409);
        assertThat(conflito.path("type").asText()).isEqualTo("about:blank");
        assertThat(conflito.path("title").asText()).isEqualTo("Conflito de concorrência");
        assertThat(conflito.path("detail").asText())
                .isEqualTo("Outra operação disputou este pedido. Consulte o estado atual antes de decidir novamente.");
        String autorVencedor = vencedor == primeiro ? autorA : autorB;
        assertThat(aceito.path("decididoPor").asText()).isEqualTo(autorVencedor);
        boolean rejeitada = vencedor == segundo && rejeitarSegundo;
        String esperado = rejeitada ? "REJEITADA"
                : "GESTOR".equals(etapa) && aceito.path("valor").decimalValue().compareTo(new BigDecimal("500")) > 0
                ? "AGUARDANDO_FINANCEIRO" : "APROVADA";
        assertThat(aceito.path("status").asText()).isEqualTo(esperado);
        assertThat(aceito.path("politica").asText()).isEqualTo(legado ? "V2" : "V3");
        assertThat(aceito.path("etapaPendente").asText())
                .isEqualTo(esperado.equals("AGUARDANDO_FINANCEIRO") ? "FINANCEIRO" : "null");
        assertThat(aceito.path("decididoEm").asText()).isEqualTo(AGORA.toString() + ":00");
        if (rejeitada) assertThat(aceito.path("motivoDecisao").asText()).isEqualTo("Despesa particular");
        else assertThat(aceito.path("motivoDecisao").isNull()).isTrue();

        // Os dois HTTP já terminaram (inclusive rollback). Nenhum contexto da corrida é reutilizado.
        transacao.executeWithoutResult(tx -> {
            assertThat(contextos).doesNotContain(em.getDelegate());
            Reembolso atual = repositorio.findById(id).orElseThrow();
            assertThat(atual.getStatus().name()).isEqualTo(esperado);
            assertThat(atual.getDecididoPor()).isEqualTo(autorVencedor);
            assertThat(atual.getDecididoEm()).isEqualTo(AGORA);
            assertThat(atual.getMotivoDecisao()).isEqualTo(rejeitada ? "Despesa particular" : null);
            assertThat(versao(id)).isEqualTo(versaoAntes + 1);
            int anteriores = "FINANCEIRO".equals(etapa) ? 1 : 0;
            assertThat(atual.getHistorico()).hasSize(legado ? 0 : anteriores + 1);
            assertThat(aceito.path("historico").size()).isEqualTo(atual.getHistorico().size());
            if (!legado) {
                if (anteriores == 1) {
                    var gestor = atual.getHistorico().getFirst();
                    assertThat(gestor.getOrdem()).isEqualTo(1);
                    assertThat(gestor.getEtapa().name()).isEqualTo("GESTOR");
                    assertThat(gestor.getResultado().name()).isEqualTo("APROVADA");
                    assertThat(gestor.getResponsavel()).isEqualTo("gestor-inicial");
                    assertThat(gestor.getQuando()).isEqualTo(AGORA);
                    assertThat(gestor.getMotivo()).isNull();
                }
                var evento = atual.getHistorico().getLast();
                assertThat(evento.getOrdem()).isEqualTo(anteriores + 1);
                assertThat(evento.getEtapa().name()).isEqualTo(etapa);
                assertThat(evento.getResultado().name()).isEqualTo(rejeitada ? "REJEITADA" : "APROVADA");
                assertThat(evento.getResponsavel()).isEqualTo(autorVencedor);
                assertThat(evento.getQuando()).isEqualTo(AGORA);
                assertThat(evento.getMotivo()).isEqualTo(atual.getMotivoDecisao());
                JsonNode eventoHttp = aceito.path("historico").get(anteriores);
                assertThat(eventoHttp.path("etapa").asText()).isEqualTo(evento.getEtapa().name());
                assertThat(eventoHttp.path("resultado").asText()).isEqualTo(evento.getResultado().name());
                assertThat(eventoHttp.path("responsavel").asText()).isEqualTo(autorVencedor);
                assertThat(eventoHttp.path("quando").asText()).isEqualTo(AGORA.toString() + ":00");
                if (rejeitada) assertThat(eventoHttp.path("motivo").asText()).isEqualTo(evento.getMotivo());
                else assertThat(eventoHttp.path("motivo").isNull()).isTrue();
            }
            assertThat(jdbc.queryForObject("select count(*) from decisao_reembolso", Long.class))
                    .isEqualTo((long) atual.getHistorico().size());
        });
    }

    private long versao(long id) {
        return jdbc.queryForObject("select versao from reembolso where id = ?", Long.class, id);
    }

    private MvcResult decidir(long id, String etapa, String autor, boolean rejeitar, boolean legado) throws Exception {
        String corpo = legado ? json.writeValueAsString(java.util.Map.of("responsavel", autor))
                : json.writeValueAsString(java.util.Map.of("responsavel", autor, "papel", etapa,
                        "etapa", etapa, "colaboradorVinculado", "solicitante", "gestorImediato", autor,
                        "motivo", rejeitar ? "Despesa particular" : ""));
        return mvc.perform(post("/reembolsos/{id}/" + (rejeitar ? "rejeicao" : "aprovacao"), id)
                .contentType("application/json").content(corpo)).andReturn();
    }
}

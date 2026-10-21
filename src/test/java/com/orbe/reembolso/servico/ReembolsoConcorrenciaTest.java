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
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
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
import org.springframework.test.util.AopTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
    @MockitoSpyBean DiaRefeicaoRepository dias;
    private TransactionTemplate transacao;

    @BeforeEach
    void preparar() {
        transacao = new TransactionTemplate(gerenciador);
        transacao.executeWithoutResult(tx -> {
            em.createQuery("delete from DecisaoReembolso").executeUpdate();
            em.createQuery("delete from Reembolso").executeUpdate();
            dias.deleteAllInBatch();
        });
    }

    @Test
    void serializaRefeicoesComSaldoParcial() throws Exception {
        long inicial = submeterAceita("80.00", "11111111000111");
        disputarSubmissoes("30.00", "110.00", 2);
        conferirSemDecisao(inicial);
    }

    @Test
    void serializaPrimeirasRefeicoesDoDia() throws Exception {
        Runnable conferirCriacao = sincronizarCriacaoDaAncora();
        disputarSubmissoes("80.00", "80.00", 1);
        conferirCriacao.run();
    }

    private Runnable sincronizarCriacaoDaAncora() {
        var barreira = new CyclicBarrier(2);
        var consultas = new AtomicInteger();
        var externas = new ConcurrentLinkedQueue<Object>();
        var internas = new ConcurrentLinkedQueue<Object>();
        var conclusoes = new ConcurrentLinkedQueue<Integer>();
        var perdedor = new AtomicReference<Object>();
        var delegadoControle = mockingDetails(controle).getMockCreationSettings().getDefaultAnswer();
        doAnswer(invocacao -> {
            externas.add(em.getDelegate());
            return delegadoControle.answer(invocacao);
        }).when(controle).garantirExistencia(any());
        DiaRefeicaoRepository espiaoDias = AopTestUtils.getUltimateTargetObject(dias);
        var delegadoDias = mockingDetails(espiaoDias).getMockCreationSettings().getDefaultAnswer();
        doAnswer(invocacao -> {
            boolean existe = (boolean) delegadoDias.answer(invocacao);
            assertThat(externas).doesNotContain(em.getDelegate());
            assertThat(TransactionSynchronizationManager.getCurrentTransactionIsolationLevel())
                    .isEqualTo(TransactionDefinition.ISOLATION_READ_COMMITTED);
            if (consultas.incrementAndGet() <= 2) {
                assertThat(existe).isFalse();
                internas.add(em.getDelegate());
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) { conclusoes.add(status); }
                });
                // As duas REQUIRES_NEW observaram ausência real antes de tentar INSERT.
                barreira.await(10, TimeUnit.SECONDS);
            } else {
                assertThat(existe).isTrue();
                assertThat(conclusoes).contains(TransactionSynchronization.STATUS_ROLLED_BACK);
                assertThat(em.getDelegate()).isNotSameAs(perdedor.get());
                assertThat(internas).doesNotContain(em.getDelegate());
            }
            return existe;
        }).when(espiaoDias).existsById(any());
        doAnswer(invocacao -> {
            try {
                return delegadoDias.answer(invocacao);
            } catch (DataIntegrityViolationException falha) {
                perdedor.set(em.getDelegate());
                Throwable causa = falha;
                while (!(causa instanceof SQLException) && causa.getCause() != null) causa = causa.getCause();
                assertThat(causa).isInstanceOf(SQLException.class);
                var sql = (SQLException) causa;
                assertThat(sql.getSQLState()).isEqualTo("23505");
                assertThat(sql.getErrorCode()).isEqualTo(23505);
                assertThat(sql.getMessage()).startsWith(
                        "Unique index or primary key violation: \"PUBLIC.PRIMARY_KEY_")
                        .contains("ON PUBLIC.DIA_REFEICAO(DATA_DESPESA, COLABORADOR)");
                throw falha;
            }
        }).when(espiaoDias).inserir(any(), any());
        return () -> {
            assertThat(consultas).hasValue(3);
            assertThat(perdedor.get()).isNotNull();
            assertThat(externas).hasSize(2);
            assertThat(internas).hasSize(2);
            assertThat(internas.toArray()[0]).isNotSameAs(internas.toArray()[1]);
            assertThat(conclusoes).containsExactlyInAnyOrder(
                    TransactionSynchronization.STATUS_COMMITTED, TransactionSynchronization.STATUS_ROLLED_BACK);
        };
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void rejeicaoConcorrenteLiberaReservaSomenteAposCommit(boolean rejeicaoPrimeiro) throws Exception {
        long rejeitado = submeterAceita("80.00", "11111111000111");
        long aprovado = submeterAceita("40.00", "44444444000144");
        assertThat(decidir(aprovado, "GESTOR", "gestor", false, false)
                .getResponse().getStatus()).isEqualTo(200);
        var pausou = new CountDownLatch(1);
        var liberar = new CountDownLatch(1);
        var sessaoTitular = new AtomicLong();
        var primeira = new AtomicBoolean(true);
        var contextos = new ConcurrentLinkedQueue<Object>();
        Runnable pausar = () -> {
            sessaoTitular.set(jdbc.queryForObject("select session_id()", Long.class));
            pausou.countDown();
            aguardar(liberar);
        };
        ControleDeTetoDiario espiao = AopTestUtils.getUltimateTargetObject(controle);
        var delegadoControle = mockingDetails(espiao).getMockCreationSettings().getDefaultAnswer();
        doAnswer(invocacao -> {
            contextos.add(em.getDelegate());
            Object resultado = delegadoControle.answer(invocacao);
            if (!rejeicaoPrimeiro && primeira.compareAndSet(true, false)) pausar.run();
            return resultado;
        }).when(espiao).bloquear(any());
        var delegadoRepositorio = mockingDetails(repositorio).getMockCreationSettings().getDefaultAnswer();
        doAnswer(invocacao -> {
            Object salvo = delegadoRepositorio.answer(invocacao);
            if (rejeicaoPrimeiro && primeira.compareAndSet(true, false)) {
                // Estado e evento já foram escritos; o commit ainda não aconteceu.
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void beforeCommit(boolean readOnly) { pausar.run(); }
                });
            }
            return salvo;
        }).when(repositorio).saveAndFlush(any(Reembolso.class));

        var executor = Executors.newFixedThreadPool(2);
        MvcResult submissao;
        MvcResult rejeicao;
        try {
            var titular = executor.submit(() -> rejeicaoPrimeiro
                    ? decidir(rejeitado, "GESTOR", "gestor", true, false)
                    : submeter("80.00", "22222222000122"));
            assertThat(pausou.await(10, TimeUnit.SECONDS)).isTrue();
            var concorrente = executor.submit(() -> rejeicaoPrimeiro
                    ? submeter("80.00", "22222222000122")
                    : decidir(rejeitado, "GESTOR", "gestor", true, false));
            aguardarDisputaNoBanco(sessaoTitular.get());
            assertThat(concorrente.isDone()).isFalse();
            transacao.executeWithoutResult(tx -> {
                conferirReserva("120.00", 2);
                assertThat(repositorio.findById(rejeitado).orElseThrow().getStatus().name()).isEqualTo("SUBMETIDA");
                assertThat(jdbc.queryForObject("select count(*) from decisao_reembolso", Long.class)).isEqualTo(1);
            });
            liberar.countDown();
            MvcResult a = titular.get(20, TimeUnit.SECONDS);
            MvcResult b = concorrente.get(20, TimeUnit.SECONDS);
            rejeicao = rejeicaoPrimeiro ? a : b;
            submissao = rejeicaoPrimeiro ? b : a;
        } finally {
            liberar.countDown();
            executor.shutdownNow();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }
        assertThat(contextos).hasSize(2);
        assertThat(contextos.toArray()[0]).isNotSameAs(contextos.toArray()[1]);
        assertThat(rejeicao.getResponse().getStatus()).isEqualTo(200);
        if (rejeicaoPrimeiro) assertThat(submissao.getResponse().getStatus()).isEqualTo(201);
        else conferirTeto(submissao);
        transacao.executeWithoutResult(tx -> {
            assertThat(contextos).doesNotContain(em.getDelegate());
            conferirReserva(rejeicaoPrimeiro ? "120.00" : "40.00", rejeicaoPrimeiro ? 3 : 2);
            assertThat(jdbc.queryForObject("select count(*) from reembolso where cnpj_fornecedor = ?",
                    Long.class, "22222222000122")).isEqualTo(rejeicaoPrimeiro ? 1 : 0);
        });
        // Repetir após os dois commits prova saldo liberado, sem armar outra pausa.
        if (!rejeicaoPrimeiro) submeterAceita("80.00", "22222222000122");
        MvcResult duplicada = submeter("80.00", "11111111000111");
        assertThat(duplicada.getResponse().getStatus()).isEqualTo(409);
        assertThat(json.readTree(duplicada.getResponse().getContentAsString()).path("title").asText())
                .isEqualTo("Reembolso duplicado");
        transacao.executeWithoutResult(tx -> {
            conferirReserva("120.00", 3);
            Reembolso antigo = repositorio.findById(rejeitado).orElseThrow();
            assertThat(antigo.getValor()).isEqualByComparingTo("80.00");
            assertThat(antigo.getStatus().name()).isEqualTo("REJEITADA");
            assertThat(antigo.getDecididoPor()).isEqualTo("gestor");
            assertThat(antigo.getDecididoEm()).isEqualTo(AGORA);
            assertThat(antigo.getMotivoDecisao()).isEqualTo("Despesa particular");
            assertThat(antigo.getHistorico()).hasSize(1);
            var evento = antigo.getHistorico().getFirst();
            assertThat(evento.getEtapa().name()).isEqualTo("GESTOR");
            assertThat(evento.getResultado().name()).isEqualTo("REJEITADA");
            assertThat(evento.getResponsavel()).isEqualTo("gestor");
            assertThat(evento.getQuando()).isEqualTo(AGORA);
            assertThat(evento.getMotivo()).isEqualTo("Despesa particular");
            Reembolso mantido = repositorio.findById(aprovado).orElseThrow();
            assertThat(mantido.getValor()).isEqualByComparingTo("40.00");
            assertThat(mantido.getStatus().name()).isEqualTo("APROVADA");
            assertThat(mantido.getHistorico()).hasSize(1);
            assertThat(mantido.getHistorico().getFirst().getResultado().name()).isEqualTo("APROVADA");
            assertThat(jdbc.queryForObject("select count(*) from decisao_reembolso", Long.class)).isEqualTo(2);
        });
    }

    private static void aguardar(CountDownLatch sinal) {
        try {
            assertThat(sinal.await(10, TimeUnit.SECONDS)).isTrue();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Espera interrompida", e);
        }
    }

    private void disputarSubmissoes(String valor, String total, int quantidade) throws Exception {
        var barreira = new CyclicBarrier(2);
        var contextos = new ConcurrentLinkedQueue<Object>();
        ControleDeTetoDiario espiao = AopTestUtils.getUltimateTargetObject(controle);
        var delegadoControle = mockingDetails(espiao).getMockCreationSettings().getDefaultAnswer();
        doAnswer(invocacao -> {
            assertThat(TransactionSynchronizationManager.getCurrentTransactionIsolationLevel())
                    .isEqualTo(TransactionDefinition.ISOLATION_READ_COMMITTED);
            contextos.add(em.getDelegate());
            barreira.await(10, TimeUnit.SECONDS);
            return delegadoControle.answer(invocacao);
        }).when(espiao).bloquear(any());
        var delegadoRepositorio = mockingDetails(repositorio).getMockCreationSettings().getDefaultAnswer();
        doAnswer(invocacao -> {
            Object salvo = delegadoRepositorio.answer(invocacao);
            // O vencedor já escreveu, mas ainda não confirmou. O banco deve mostrar
            // a outra sessão efetivamente esperando seu lock, não só duas threads prontas.
            aguardarDisputaNoBanco();
            return salvo;
        }).when(repositorio).saveAndFlush(any(Reembolso.class));

        var executor = Executors.newFixedThreadPool(2);
        MvcResult a;
        MvcResult b;
        try {
            var primeira = executor.submit(() -> submeter(valor, "22222222000122"));
            var segunda = executor.submit(() -> submeter(valor, "33333333000133"));
            a = primeira.get(20, TimeUnit.SECONDS);
            b = segunda.get(20, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
            assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
        }
        assertThat(contextos).hasSize(2);
        assertThat(contextos.toArray()[0]).isNotSameAs(contextos.toArray()[1]);
        assertThat(new int[] {a.getResponse().getStatus(), b.getResponse().getStatus()})
                .containsExactlyInAnyOrder(201, 422);
        MvcResult aceita = a.getResponse().getStatus() == 201 ? a : b;
        MvcResult recusada = aceita == a ? b : a;
        conferirTeto(recusada);
        JsonNode criada = json.readTree(aceita.getResponse().getContentAsString());
        assertThat(criada.path("valor").decimalValue()).isEqualByComparingTo(valor);
        assertThat(criada.path("status").asText()).isEqualTo("SUBMETIDA");
        assertThat(criada.path("politica").asText()).isEqualTo("V3");
        assertThat(criada.path("historico")).isEmpty();
        String notaAceita = aceita == a ? "22222222000122" : "33333333000133";
        String notaRecusada = aceita == a ? "33333333000133" : "22222222000122";
        transacao.executeWithoutResult(tx -> {
            assertThat(contextos).doesNotContain(em.getDelegate());
            conferirReserva(total, quantidade);
            assertThat(repositorio.findById(criada.path("id").asLong())).get()
                    .extracting(Reembolso::getCnpjFornecedor).isEqualTo(notaAceita);
            assertThat(jdbc.queryForObject("select count(*) from reembolso where cnpj_fornecedor = ?",
                    Long.class, notaRecusada)).isZero();
            assertThat(jdbc.queryForObject("select count(*) from decisao_reembolso", Long.class)).isZero();
        });
    }

    private void aguardarDisputaNoBanco() {
        aguardarDisputaNoBanco(jdbc.queryForObject("select session_id()", Long.class));
    }

    private void aguardarDisputaNoBanco(long titular) {
        long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < limite) {
            if (Boolean.TRUE.equals(jdbc.queryForObject("""
                    select count(*) > 0 from information_schema.sessions
                    where blocker_id = ? and session_state = 'BLOCKED'
                      and lower(executing_statement) like '%dia_refeicao%for update%'
                    """, Boolean.class, titular))) return;
            Thread.onSpinWait();
        }
        throw new AssertionError("Nenhuma sessão disputou o lock da transação ainda aberta");
    }

    private void conferirReserva(String total, int quantidade) {
        assertThat(jdbc.queryForObject("""
                select coalesce(sum(valor), 0) from reembolso
                where colaborador = 'solicitante' and data_despesa = date '2026-11-02'
                  and categoria = 'REFEICAO' and submetido_em >= timestamp '2026-11-01 00:00:00'
                  and status in ('SUBMETIDA', 'AGUARDANDO_FINANCEIRO', 'APROVADA', 'PAGA')
                """, BigDecimal.class)).isEqualByComparingTo(total);
        assertThat(repositorio.count()).isEqualTo(quantidade);
        assertThat(dias.count()).isEqualTo(1);
    }

    private void conferirTeto(MvcResult resultado) throws Exception {
        assertThat(resultado.getResponse().getStatus()).isEqualTo(422);
        assertThat(resultado.getResponse().getContentType()).startsWith("application/problem+json");
        JsonNode problema = json.readTree(resultado.getResponse().getContentAsString());
        assertThat(problema.path("status").asInt()).isEqualTo(422);
        assertThat(problema.path("type").asText()).isEqualTo("about:blank");
        assertThat(problema.path("detail").asText()).isEqualTo("O limite diário de refeições é de R$ 120,00.");
    }

    private long submeterAceita(String valor, String nota) throws Exception {
        MvcResult resultado = submeter(valor, nota);
        assertThat(resultado.getResponse().getStatus()).isEqualTo(201);
        return json.readTree(resultado.getResponse().getContentAsString()).path("id").asLong();
    }

    private MvcResult submeter(String valor, String nota) throws Exception {
        return mvc.perform(post("/reembolsos").contentType("application/json")
                .content(json.writeValueAsString(java.util.Map.of("colaborador", "solicitante",
                        "categoria", "REFEICAO", "valor", new BigDecimal(valor),
                        "dataDespesa", AGORA.toLocalDate(), "cnpjFornecedor", nota,
                        "descricao", "Reserva concorrente T015")))).andReturn();
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

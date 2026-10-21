package com.orbe.reembolso.servico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.orbe.reembolso.dominio.ChaveDiaRefeicao;
import com.orbe.reembolso.dominio.DiaRefeicao;

@DataJpaTest(properties = "spring.jpa.open-in-view=false", showSql = false)
@Import(ControleDeTetoDiario.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ControleDeTetoDiarioTest {

    private static final LocalDate DATA = LocalDate.of(2026, 11, 2);
    private static final ChaveDiaRefeicao CHAVE = new ChaveDiaRefeicao("colaborador-a", DATA);

    @Autowired
    private ControleDeTetoDiario controle;
    @Autowired
    private DiaRefeicaoRepository repositorio;
    @Autowired
    private PlatformTransactionManager gerenciador;
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    @Autowired
    private JdbcTemplate jdbc;

    private TransactionTemplate transacao;

    @BeforeEach
    void limpar() {
        transacao = new TransactionTemplate(gerenciador);
        transacao.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transacao.executeWithoutResult(status -> repositorio.deleteAllInBatch());
    }

    @Test
    void criaAncoraPersistenteSemSaldoEReutilizaMesmaChave() {
        controle.garantirExistencia(CHAVE);
        controle.garantirExistencia(new ChaveDiaRefeicao("colaborador-a", DATA));

        assertThat(repositorio.count()).isEqualTo(1);
        assertThat(repositorio.findById(CHAVE)).get()
                .extracting(DiaRefeicao::getChave).isEqualTo(CHAVE);
        assertThat(jdbc.queryForList("""
                select column_name from information_schema.columns
                where table_name = 'DIA_REFEICAO' order by column_name
                """, String.class)).containsExactly("COLABORADOR", "DATA_DESPESA");
    }

    @Test
    void chaveCompostaDistingueColaboradorEData() {
        controle.garantirExistencia(CHAVE);
        controle.garantirExistencia(new ChaveDiaRefeicao("colaborador-b", DATA));
        controle.garantirExistencia(new ChaveDiaRefeicao("colaborador-a", DATA.plusDays(1)));

        assertThat(repositorio.count()).isEqualTo(3);
        assertThat(new ChaveDiaRefeicao("colaborador-a", DATA)).isEqualTo(CHAVE)
                .hasSameHashCodeAs(CHAVE);
    }

    @Test
    void aceitaIdentificadorCom80CaracteresERecusaMaior() {
        var chave = new ChaveDiaRefeicao("a".repeat(80), DATA);
        controle.garantirExistencia(chave);
        assertThat(repositorio.existsById(chave)).isTrue();
        assertThatThrownBy(() -> new ChaveDiaRefeicao("a".repeat(81), DATA))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void bancoImpedeDuplicacaoDaChaveComposta() {
        controle.garantirExistencia(CHAVE);
        assertThatThrownBy(() -> transacao.executeWithoutResult(status -> {
            repositorio.inserir(CHAVE.getColaborador(), CHAVE.getDataDespesa());
            repositorio.flush();
        })).isInstanceOf(DataIntegrityViolationException.class);
        assertThat(repositorio.count()).isEqualTo(1);
    }

    @Test
    void ancoraSobreviveAoRollbackDoComandoExterno() {
        transacao.executeWithoutResult(status -> {
            controle.garantirExistencia(CHAVE);
            controle.bloquear(CHAVE);
            status.setRollbackOnly();
        });
        assertThat(repositorio.existsById(CHAVE)).isTrue();
    }

    @Test
    void criacaoEFlushOcorremEmTransacaoIndependenteAntesDoLock() {
        var delegado = mock(DiaRefeicaoRepository.class, delegatesTo(repositorio));
        var local = new ControleDeTetoDiario(delegado, gerenciador);
        var externa = new AtomicReference<EntityManager>();
        doAnswer(invocacao -> {
            assertThat(entityManagerAtual()).isNotSameAs(externa.get());
            assertThat(TransactionSynchronizationManager.getCurrentTransactionIsolationLevel())
                    .isEqualTo(TransactionDefinition.ISOLATION_READ_COMMITTED);
            repositorio.inserir(invocacao.getArgument(0), invocacao.getArgument(1));
            return null;
        }).when(delegado).inserir(any(), any());
        doAnswer(invocacao -> {
            assertThat(entityManagerAtual()).isNotSameAs(externa.get());
            repositorio.flush();
            return null;
        }).when(delegado).flush();

        transacao.executeWithoutResult(status -> {
            externa.set(entityManagerAtual());
            local.garantirExistencia(CHAVE);
            assertThat(entityManagerAtual()).isSameAs(externa.get());
            controle.bloquear(CHAVE);
        });
        verify(delegado).flush();
    }

    @Test
    void corridaRealRevertePerdedorEConfirmaEmNovaTransacao() throws Exception {
        var delegado = mock(DiaRefeicaoRepository.class, delegatesTo(repositorio));
        var local = new ControleDeTetoDiario(delegado, gerenciador);
        var barreira = new CyclicBarrier(2);
        var consultas = new AtomicInteger();
        var conclusoes = new CopyOnWriteArrayList<Integer>();
        var perdedor = new AtomicReference<EntityManager>();
        doAnswer(invocacao -> {
            boolean existe = repositorio.existsById(CHAVE);
            if (consultas.incrementAndGet() <= 2) {
                assertThat(existe).isFalse();
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        conclusoes.add(status);
                    }
                });
                barreira.await(10, TimeUnit.SECONDS);
            } else {
                assertThat(conclusoes).contains(TransactionSynchronization.STATUS_ROLLED_BACK);
                assertThat(entityManagerAtual()).isNotSameAs(perdedor.get());
                assertThat(existe).isTrue();
            }
            return existe;
        }).when(delegado).existsById(CHAVE);
        doAnswer(invocacao -> {
            try {
                repositorio.inserir(invocacao.getArgument(0), invocacao.getArgument(1));
            } catch (DataIntegrityViolationException falha) {
                perdedor.set(entityManagerAtual());
                throw falha;
            }
            return null;
        }).when(delegado).inserir(any(), any());

        // Sem transações externas ocupando conexões enquanto aguardam REQUIRES_NEW.
        try (var executor = Executors.newFixedThreadPool(2)) {
            var primeira = executor.submit(() -> local.garantirExistencia(CHAVE));
            var segunda = executor.submit(() -> local.garantirExistencia(CHAVE));
            primeira.get(15, TimeUnit.SECONDS);
            segunda.get(15, TimeUnit.SECONDS);
        }
        assertThat(conclusoes).containsExactlyInAnyOrder(
                TransactionSynchronization.STATUS_COMMITTED, TransactionSynchronization.STATUS_ROLLED_BACK);
        assertThat(consultas).hasValue(3);
        assertThat(repositorio.count()).isEqualTo(1);
    }

    @Test
    void colisaoSemConfirmacaoDaAncoraNaoEhIgnorada() {
        controle.garantirExistencia(CHAVE);
        var falha = (DataIntegrityViolationException) catchThrowable(() ->
                transacao.executeWithoutResult(status ->
                        repositorio.inserir(CHAVE.getColaborador(), CHAVE.getDataDespesa())));
        var falso = mock(DiaRefeicaoRepository.class);
        when(falso.existsById(CHAVE)).thenReturn(false);
        doThrow(falha).when(falso).inserir(any(), any());

        assertThatThrownBy(() -> new ControleDeTetoDiario(falso, gerenciador).garantirExistencia(CHAVE))
                .isSameAs(falha);
        verify(falso, times(2)).existsById(CHAVE);
    }

    @Test
    void outraRestricaoUnicaNaMesmaTabelaNaoEhConfundidaComCorridaDaPk() {
        controle.garantirExistencia(CHAVE);
        jdbc.execute("create unique index uk_t008_data on dia_refeicao(data_despesa)");
        try {
            assertThatThrownBy(() -> controle.garantirExistencia(
                    new ChaveDiaRefeicao("colaborador-b", DATA)))
                    .isInstanceOf(DataIntegrityViolationException.class);
            assertThat(repositorio.count()).isEqualTo(1);
        } finally {
            jdbc.execute("drop index uk_t008_data");
        }
    }

    @Test
    void falhaDeIntegridadeNoFlushNaoEhIgnoradaMesmoSeAncoraExiste() {
        controle.garantirExistencia(CHAVE);
        var falso = mock(DiaRefeicaoRepository.class);
        var falha = new DataIntegrityViolationException("outra restricao",
                new SQLException("restricao diferente", "23502", 23502));
        when(falso.existsById(CHAVE)).thenReturn(false, true);
        doThrow(falha).when(falso).flush();

        assertThatThrownBy(() -> new ControleDeTetoDiario(falso, gerenciador).garantirExistencia(CHAVE))
                .isSameAs(falha);
        verify(falso).existsById(CHAVE);
    }

    @Test
    void falhaOperacionalEhPropagadaSemConfirmacaoOuRetry() {
        var falso = mock(DiaRefeicaoRepository.class);
        var falha = new DataAccessResourceFailureException("banco indisponivel");
        when(falso.existsById(CHAVE)).thenThrow(falha);

        assertThatThrownBy(() -> new ControleDeTetoDiario(falso, gerenciador).garantirExistencia(CHAVE))
                .isSameAs(falha);
        verify(falso).existsById(CHAVE);
    }

    @Test
    void bloqueioExigeTransacaoDeNegocioExistente() {
        controle.garantirExistencia(CHAVE);
        assertThatThrownBy(() -> controle.bloquear(CHAVE))
                .isInstanceOf(IllegalTransactionStateException.class);
    }

    @Test
    void bloqueioDeAncoraAusenteFalhaSemCriarAncoraImplicitamente() {
        assertThatThrownBy(() -> transacao.executeWithoutResult(status -> controle.bloquear(CHAVE)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(repositorio.count()).isZero();
    }

    @Test
    void lockPersistenteDuraAteFimDaTransacaoDeNegocio() throws Exception {
        controle.garantirExistencia(CHAVE);
        var bloqueou = new CountDownLatch(1);
        var liberar = new CountDownLatch(1);
        try (var executor = Executors.newSingleThreadExecutor()) {
            var titular = executor.submit(() -> transacao.executeWithoutResult(status -> {
                controle.bloquear(CHAVE);
                bloqueou.countDown();
                aguardar(liberar);
            }));
            try {
                assertThat(bloqueou.await(10, TimeUnit.SECONDS)).isTrue();
                Throwable falha = catchThrowable(() -> transacao.executeWithoutResult(status -> {
                    jdbc.execute("set lock_timeout 200");
                    controle.bloquear(CHAVE);
                }));
                assertThat(falha).isInstanceOf(PessimisticLockingFailureException.class);
                assertThat(causaSql(falha).getErrorCode()).isEqualTo(50200);
            } finally {
                liberar.countDown();
            }
            titular.get(15, TimeUnit.SECONDS);
        }
        // Uma nova transação pode adquirir o lock após o commit do titular.
        transacao.executeWithoutResult(status -> controle.bloquear(CHAVE));
    }

    private EntityManager entityManagerAtual() {
        return EntityManagerFactoryUtils.getTransactionalEntityManager(entityManagerFactory);
    }

    private static void aguardar(CountDownLatch sinal) {
        try {
            if (!sinal.await(10, TimeUnit.SECONDS)) {
                throw new AssertionError("Timeout aguardando sincronizacao");
            }
        } catch (InterruptedException falha) {
            Thread.currentThread().interrupt();
            throw new AssertionError(falha);
        }
    }

    private static SQLException causaSql(Throwable falha) {
        for (Throwable causa = falha; causa != null; causa = causa.getCause()) {
            if (causa instanceof SQLException sql) {
                return sql;
            }
        }
        throw new AssertionError("Excecao JDBC ausente", falha);
    }
}

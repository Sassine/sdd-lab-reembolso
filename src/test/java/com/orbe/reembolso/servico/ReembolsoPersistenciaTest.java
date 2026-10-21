package com.orbe.reembolso.servico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.orbe.reembolso.dominio.Categoria;
import com.orbe.reembolso.dominio.DecisaoReembolso;
import com.orbe.reembolso.dominio.EtapaAprovacao;
import com.orbe.reembolso.dominio.Reembolso;
import com.orbe.reembolso.dominio.StatusReembolso;
import com.orbe.reembolso.dominio.TransicaoDeStatusInvalidaException;

@DataJpaTest(properties = "spring.jpa.open-in-view=false", showSql = false)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ReembolsoPersistenciaTest {

    private static final LocalDate DATA = LocalDate.of(2026, 10, 30);
    private static final LocalDateTime SUBMISSAO = LocalDateTime.of(2026, 11, 1, 0, 0);
    private static final LocalDateTime GESTOR_EM = SUBMISSAO.plusHours(1);
    private static final LocalDateTime FINANCEIRO_EM = SUBMISSAO.plusHours(2);

    @Autowired
    private ReembolsoRepository repositorio;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JdbcTemplate jdbc;

    private TransactionTemplate transacao;

    @BeforeEach
    void limpar() {
        transacao = new TransactionTemplate(transactionManager);
        transacao.executeWithoutResult(status -> repositorio.deleteAll());
    }

    @Test
    void buscaPorIdCarregaHistoricoOrdenadoForaDaTransacao() {
        // A segunda decisão aceita tem momento anterior: a sequência não pode ser cronológica.
        LocalDateTime financeiroEm = GESTOR_EM.minusMinutes(1);
        Long id = salvar(novo(DATA));
        aprovarGestor(id);
        transacao.executeWithoutResult(status -> {
            Reembolso pedido = repositorio.findById(id).orElseThrow();
            pedido.rejeitar(EtapaAprovacao.FINANCEIRO, "financeiro-ficticio",
                    "Comprovante insuficiente", financeiroEm);
            repositorio.flush();
        });

        Reembolso lido = repositorio.findById(id).orElseThrow();

        // A transação do repositório já terminou; não há open-in-view nem contexto de teste aberto.
        assertThat(lido.getHistorico()).extracting(DecisaoReembolso::getOrdem,
                DecisaoReembolso::getEtapa, DecisaoReembolso::getResultado,
                DecisaoReembolso::getResponsavel, DecisaoReembolso::getQuando,
                DecisaoReembolso::getMotivo).containsExactly(
                        tuple(1, EtapaAprovacao.GESTOR, StatusReembolso.APROVADA,
                                "gestor-ficticio", GESTOR_EM, null),
                        tuple(2, EtapaAprovacao.FINANCEIRO, StatusReembolso.REJEITADA,
                                "financeiro-ficticio", financeiroEm, "Comprovante insuficiente"));
        assertThat(lido.getStatus()).isEqualTo(StatusReembolso.REJEITADA);
        assertThat(lido.getDecididoPor()).isEqualTo("financeiro-ficticio");
        assertThat(lido.getDecididoEm()).isEqualTo(financeiroEm);
        assertThat(lido.getMotivoDecisao()).isEqualTo("Comprovante insuficiente");
        assertThat(lido.getSubmetidoEm()).isEqualTo(SUBMISSAO);
    }

    @Test
    void listagemCarregaHistoricosEPreservaOrdemDeDataEId() {
        // A listagem também deve preservar a aceitação, não ordenar pelo momento.
        LocalDateTime financeiroEm = GESTOR_EM.minusMinutes(1);
        Long antigo = salvar(novo(DATA.minusDays(1)));
        Long primeiro = salvar(novo(DATA));
        Long segundo = salvar(novo(DATA));
        aprovarGestor(primeiro);
        aprovarGestor(segundo);
        transacao.executeWithoutResult(status -> repositorio.findById(segundo).orElseThrow()
                .aprovar(EtapaAprovacao.FINANCEIRO, "financeiro-ficticio", financeiroEm, false));
        Reembolso outro = new Reembolso("outro-ficticio", Categoria.REFEICAO,
                new BigDecimal("10.00"), DATA, "12345678000190", null, SUBMISSAO);
        salvar(outro);

        List<Reembolso> lidos = repositorio.findByColaboradorOrderByDataDespesaDescIdDesc("colaborador-ficticio");

        assertThat(lidos).extracting(Reembolso::getId).containsExactly(segundo, primeiro, antigo);
        assertThat(lidos.get(0).getHistorico()).extracting(DecisaoReembolso::getOrdem).containsExactly(1, 2);
        assertThat(lidos.get(0).getHistorico()).extracting(DecisaoReembolso::getEtapa)
                .containsExactly(EtapaAprovacao.GESTOR, EtapaAprovacao.FINANCEIRO);
        assertThat(lidos.get(0).getHistorico())
                .extracting(DecisaoReembolso::getOrdem, DecisaoReembolso::getEtapa,
                        DecisaoReembolso::getResponsavel, DecisaoReembolso::getQuando)
                .containsExactly(
                        tuple(1, EtapaAprovacao.GESTOR, "gestor-ficticio", GESTOR_EM),
                        tuple(2, EtapaAprovacao.FINANCEIRO, "financeiro-ficticio", financeiroEm));
        assertThat(lidos.get(0).getStatus()).isEqualTo(StatusReembolso.APROVADA);
        assertThat(lidos.get(0).getMotivoDecisao()).isNull();
        assertThat(lidos.get(1).getHistorico()).hasSize(1);
        assertThat(lidos.get(2).getHistorico()).isEmpty();
    }

    @Test
    void rollbackDaSegundaDecisaoPreservaPedidoEEventoAnterior() {
        Long id = salvar(novo(DATA));
        aprovarGestor(id);

        transacao.executeWithoutResult(status -> {
            Reembolso pedido = repositorio.findById(id).orElseThrow();
            pedido.rejeitar(EtapaAprovacao.FINANCEIRO, "financeiro-ficticio",
                    "Motivo revertido", FINANCEIRO_EM);
            repositorio.flush();
            assertThat(contarEventos(id)).isEqualTo(2);
            status.setRollbackOnly();
        });

        Reembolso lido = repositorio.findById(id).orElseThrow();
        assertThat(lido.getStatus()).isEqualTo(StatusReembolso.AGUARDANDO_FINANCEIRO);
        assertThat(lido.getDecididoPor()).isEqualTo("gestor-ficticio");
        assertThat(lido.getDecididoEm()).isEqualTo(GESTOR_EM);
        assertThat(lido.getMotivoDecisao()).isNull();
        assertThat(lido.getHistorico()).extracting(DecisaoReembolso::getOrdem,
                DecisaoReembolso::getResponsavel).containsExactly(tuple(1, "gestor-ficticio"));
        assertThat(contarEventos(id)).isEqualTo(1);
    }

    @Test
    void rollbackDaPrimeiraDecisaoNaoDeixaEventoNemCamposSingulares() {
        Long id = salvar(novo(DATA));

        transacao.executeWithoutResult(status -> {
            Reembolso pedido = repositorio.findById(id).orElseThrow();
            pedido.aprovar(EtapaAprovacao.GESTOR, "gestor-ficticio", GESTOR_EM, true);
            repositorio.flush();
            assertThat(contarEventos(id)).isEqualTo(1);
            status.setRollbackOnly();
        });

        Reembolso lido = repositorio.findById(id).orElseThrow();
        assertThat(lido.getStatus()).isEqualTo(StatusReembolso.SUBMETIDA);
        assertThat(lido.getDecididoPor()).isNull();
        assertThat(lido.getDecididoEm()).isNull();
        assertThat(lido.getMotivoDecisao()).isNull();
        assertThat(lido.getHistorico()).isEmpty();
        assertThat(contarEventos(id)).isZero();
    }

    @Test
    void submissaoETentativaRecusadaNaoGeramEventos() {
        Long id = salvar(novo(DATA));
        assertThat(repositorio.findById(id).orElseThrow().getHistorico()).isEmpty();
        assertThat(contarEventos(id)).isZero();

        transacao.executeWithoutResult(status -> {
            Reembolso pedido = repositorio.findById(id).orElseThrow();
            assertThatThrownBy(() -> pedido.aprovar(EtapaAprovacao.FINANCEIRO,
                    "financeiro-ficticio", FINANCEIRO_EM, false))
                    .isInstanceOf(TransicaoDeStatusInvalidaException.class);
            repositorio.flush();
        });

        assertThat(repositorio.findById(id).orElseThrow().getStatus()).isEqualTo(StatusReembolso.SUBMETIDA);
        assertThat(contarEventos(id)).isZero();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void decisoesLegadasContinuamSemHistoricoRetroativo(boolean aprovar) {
        LocalDateTime antesDaV3 = SUBMISSAO.minusSeconds(1);
        Reembolso legado = new Reembolso("colaborador-ficticio", Categoria.REFEICAO,
                new BigDecimal("6000.00"), DATA, "12345678000190", null, antesDaV3);
        if (aprovar) {
            legado.aprovar("gestor-legado", GESTOR_EM);
        } else {
            legado.rejeitar("gestor-legado", "Motivo legado", GESTOR_EM);
        }
        Long id = salvar(legado);

        Reembolso lido = repositorio.findById(id).orElseThrow();
        assertThat(lido.getStatus()).isEqualTo(aprovar ? StatusReembolso.APROVADA : StatusReembolso.REJEITADA);
        assertThat(lido.getDecididoPor()).isEqualTo("gestor-legado");
        assertThat(lido.getDecididoEm()).isEqualTo(GESTOR_EM);
        assertThat(lido.getMotivoDecisao()).isEqualTo(aprovar ? null : "Motivo legado");
        assertThat(lido.getSubmetidoEm()).isEqualTo(antesDaV3);
        assertThat(lido.getValor()).isEqualByComparingTo("6000.00");
        assertThat(lido.getHistorico()).isEmpty();
        assertThat(contarEventos(id)).isZero();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void versaoRejeitaCopiaObsoletaEPreservaDecisaoAceita(boolean v3) {
        Reembolso novo = new Reembolso("colaborador-ficticio", Categoria.REFEICAO,
                new BigDecimal("6000.00"), DATA, "12345678000190", null,
                v3 ? SUBMISSAO : SUBMISSAO.minusSeconds(1));
        Long id = salvar(novo);
        // Leituras em transações distintas representam duas decisões sobre a mesma versão.
        Reembolso aceita = repositorio.findById(id).orElseThrow();
        Reembolso obsoleta = repositorio.findById(id).orElseThrow();
        if (v3) {
            aceita.aprovar(EtapaAprovacao.GESTOR, "gestor-aceito", GESTOR_EM, true);
            obsoleta.rejeitar(EtapaAprovacao.GESTOR, "gestor-obsoleto", "Não deve persistir", FINANCEIRO_EM);
        } else {
            aceita.aprovar("gestor-aceito", GESTOR_EM);
            obsoleta.rejeitar("gestor-obsoleto", "Não deve persistir", FINANCEIRO_EM);
        }
        transacao.executeWithoutResult(status -> repositorio.saveAndFlush(aceita));

        assertThatThrownBy(() -> transacao.executeWithoutResult(status -> repositorio.saveAndFlush(obsoleta)))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);

        Reembolso sobrevivente = repositorio.findById(id).orElseThrow();
        assertThat(sobrevivente.getStatus())
                .isEqualTo(v3 ? StatusReembolso.AGUARDANDO_FINANCEIRO : StatusReembolso.APROVADA);
        assertThat(sobrevivente.getDecididoPor()).isEqualTo("gestor-aceito");
        assertThat(sobrevivente.getDecididoEm()).isEqualTo(GESTOR_EM);
        assertThat(sobrevivente.getMotivoDecisao()).isNull();
        if (v3) {
            assertThat(sobrevivente.getHistorico()).extracting(DecisaoReembolso::getOrdem,
                    DecisaoReembolso::getResultado, DecisaoReembolso::getResponsavel)
                    .containsExactly(tuple(1, StatusReembolso.APROVADA, "gestor-aceito"));
        } else {
            assertThat(sobrevivente.getHistorico()).isEmpty();
        }
        assertThat(contarEventos(id)).isEqualTo(v3 ? 1 : 0);
    }

    @Test
    void deleteAllContinuaLimpandoPedidosComHistorico() {
        Long id = salvar(novo(DATA));
        aprovarGestor(id);
        assertThat(contarEventos(id)).isEqualTo(1);

        transacao.executeWithoutResult(status -> {
            repositorio.deleteAll();
            repositorio.flush();
        });

        assertThat(repositorio.findAll()).isEmpty();
        assertThat(contarEventos(id)).isZero();
    }

    private Reembolso novo(LocalDate dataDespesa) {
        return new Reembolso("colaborador-ficticio", Categoria.REFEICAO, new BigDecimal("6000.00"),
                dataDespesa, "12345678000190", "Despesa fictícia", SUBMISSAO);
    }

    private Long salvar(Reembolso pedido) {
        return transacao.execute(status -> repositorio.saveAndFlush(pedido).getId());
    }

    private void aprovarGestor(Long id) {
        transacao.executeWithoutResult(status -> repositorio.findById(id).orElseThrow()
                .aprovar(EtapaAprovacao.GESTOR, "gestor-ficticio", GESTOR_EM, true));
    }

    private long contarEventos(Long id) {
        return jdbc.queryForObject("select count(*) from decisao_reembolso where reembolso_id = ?", Long.class, id);
    }
}

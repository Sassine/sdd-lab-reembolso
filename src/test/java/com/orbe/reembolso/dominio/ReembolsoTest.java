package com.orbe.reembolso.dominio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.test.util.ReflectionTestUtils;

class ReembolsoTest {

    private static final LocalDateTime GESTOR_EM = LocalDateTime.parse("2026-11-03T10:00:00");
    private static final LocalDateTime FINANCEIRO_EM = GESTOR_EM.plusHours(1);

    @Test
    void concluiAprovacaoDoGestorQuandoPoliticaNaoExigeFinanceiro() {
        Reembolso reembolso = novo();

        reembolso.aprovar(EtapaAprovacao.GESTOR, "gestor", GESTOR_EM, false);

        assertThat(reembolso.getStatus()).isEqualTo(StatusReembolso.APROVADA);
        assertUltimaDecisao(reembolso, "gestor", GESTOR_EM, null);
        assertThat(reembolso.getHistorico()).hasSize(1);
        assertEvento(reembolso.getHistorico().getFirst(), 1, EtapaAprovacao.GESTOR,
                StatusReembolso.APROVADA, "gestor", GESTOR_EM, null);
    }

    @Test
    void aguardaFinanceiroSemAprovacaoFinalEPreservaGestorAoConcluir() {
        Reembolso reembolso = novo();
        assertThat(reembolso.getHistorico()).isEmpty();

        reembolso.aprovar(EtapaAprovacao.GESTOR, "gestor", GESTOR_EM, true);

        assertThat(reembolso.getStatus()).isEqualTo(StatusReembolso.AGUARDANDO_FINANCEIRO);
        assertUltimaDecisao(reembolso, "gestor", GESTOR_EM, null);
        assertThat(reembolso.getHistorico()).hasSize(1);
        DecisaoReembolso gestor = reembolso.getHistorico().getFirst();
        assertEvento(gestor, 1, EtapaAprovacao.GESTOR,
                StatusReembolso.APROVADA, "gestor", GESTOR_EM, null);

        reembolso.aprovar(EtapaAprovacao.FINANCEIRO, "financeiro", FINANCEIRO_EM, true);

        assertThat(reembolso.getStatus()).isEqualTo(StatusReembolso.APROVADA);
        assertUltimaDecisao(reembolso, "financeiro", FINANCEIRO_EM, null);
        assertThat(reembolso.getHistorico()).hasSize(2);
        assertThat(reembolso.getHistorico().getFirst()).isSameAs(gestor);
        assertEvento(reembolso.getHistorico().get(1), 2, EtapaAprovacao.FINANCEIRO,
                StatusReembolso.APROVADA, "financeiro", FINANCEIRO_EM, null);
    }

    @Test
    void rejeitaNaEtapaDoGestorComAutoriaMomentoEMotivo() {
        Reembolso reembolso = novo();

        reembolso.rejeitar(EtapaAprovacao.GESTOR, "gestor", "Despesa indevida", GESTOR_EM);

        assertThat(reembolso.getStatus()).isEqualTo(StatusReembolso.REJEITADA);
        assertUltimaDecisao(reembolso, "gestor", GESTOR_EM, "Despesa indevida");
        assertThat(reembolso.getHistorico()).hasSize(1);
        assertEvento(reembolso.getHistorico().getFirst(), 1, EtapaAprovacao.GESTOR,
                StatusReembolso.REJEITADA, "gestor", GESTOR_EM, "Despesa indevida");
    }

    @Test
    void rejeicaoFinanceiraPreservaAprovacaoDoGestorEImpedeAprovacaoPosterior() {
        Reembolso reembolso = novo();
        reembolso.aprovar(EtapaAprovacao.GESTOR, "gestor", GESTOR_EM, true);
        DecisaoReembolso gestor = reembolso.getHistorico().getFirst();

        reembolso.rejeitar(EtapaAprovacao.FINANCEIRO, "financeiro", "Comprovante insuficiente",
                FINANCEIRO_EM);

        assertThat(reembolso.getStatus()).isEqualTo(StatusReembolso.REJEITADA);
        assertUltimaDecisao(reembolso, "financeiro", FINANCEIRO_EM, "Comprovante insuficiente");
        assertThat(reembolso.getHistorico()).hasSize(2);
        assertThat(reembolso.getHistorico().getFirst()).isSameAs(gestor);
        assertEvento(gestor, 1, EtapaAprovacao.GESTOR,
                StatusReembolso.APROVADA, "gestor", GESTOR_EM, null);
        assertEvento(reembolso.getHistorico().get(1), 2, EtapaAprovacao.FINANCEIRO,
                StatusReembolso.REJEITADA, "financeiro", FINANCEIRO_EM, "Comprovante insuficiente");
        assertRecusaSemMutacao(reembolso,
                () -> reembolso.aprovar(EtapaAprovacao.FINANCEIRO, "outro", FINANCEIRO_EM, false));
    }

    @Test
    void recusaFinanceiroAntesDoGestorSemAlterarPedidoOuHistorico() {
        Reembolso reembolso = novo();

        assertRecusaSemMutacao(reembolso,
                () -> reembolso.aprovar(EtapaAprovacao.FINANCEIRO, "financeiro", FINANCEIRO_EM, false));
        assertRecusaSemMutacao(reembolso,
                () -> reembolso.rejeitar(EtapaAprovacao.FINANCEIRO, "financeiro", "Motivo", FINANCEIRO_EM));
    }

    @Test
    void recusaEtapaDoGestorRepetidaSemReinterpretaLaComoFinanceiro() {
        Reembolso reembolso = novo();
        reembolso.aprovar(EtapaAprovacao.GESTOR, "gestor", GESTOR_EM, true);

        assertRecusaSemMutacao(reembolso,
                () -> reembolso.aprovar(EtapaAprovacao.GESTOR, "outro", FINANCEIRO_EM, false));
        assertRecusaSemMutacao(reembolso,
                () -> reembolso.rejeitar(EtapaAprovacao.GESTOR, "outro", "Motivo", FINANCEIRO_EM));
        assertRecusaSemMutacao(reembolso, () -> reembolso.aprovar("outro", FINANCEIRO_EM));
        assertRecusaSemMutacao(reembolso, () -> reembolso.rejeitar("outro", "Motivo", FINANCEIRO_EM));
    }

    @ParameterizedTest
    @EnumSource(value = StatusReembolso.class, names = {"APROVADA", "REJEITADA", "PAGA"})
    void estadosEncerradosNaoAceitamNovasDecisoes(StatusReembolso encerrado) {
        Reembolso reembolso = novo();
        if (encerrado == StatusReembolso.REJEITADA) {
            reembolso.rejeitar(EtapaAprovacao.GESTOR, "gestor", "Motivo", GESTOR_EM);
        } else {
            reembolso.aprovar(EtapaAprovacao.GESTOR, "gestor", GESTOR_EM, false);
            if (encerrado == StatusReembolso.PAGA) {
                // Não há comando de pagamento neste recorte: simula o estado já carregado.
                ReflectionTestUtils.setField(reembolso, "status", StatusReembolso.PAGA);
            }
        }

        for (EtapaAprovacao etapa : EtapaAprovacao.values()) {
            assertRecusaSemMutacao(reembolso,
                    () -> reembolso.aprovar(etapa, "outro", FINANCEIRO_EM, false));
            assertRecusaSemMutacao(reembolso,
                    () -> reembolso.rejeitar(etapa, "outro", "Outro motivo", FINANCEIRO_EM));
        }
        assertRecusaSemMutacao(reembolso, () -> reembolso.aprovar("outro", FINANCEIRO_EM));
        assertRecusaSemMutacao(reembolso, () -> reembolso.rejeitar("outro", "Outro motivo", FINANCEIRO_EM));
    }

    @Test
    void historicoSegueOrdemDeAceitacaoENaoPermiteAlteracaoExterna() {
        Reembolso reembolso = novo();
        reembolso.aprovar(EtapaAprovacao.GESTOR, "gestor", GESTOR_EM, true);
        // A ordenação é de aceitação, não uma ordenação posterior pelos horários.
        reembolso.aprovar(EtapaAprovacao.FINANCEIRO, "financeiro", GESTOR_EM.minusMinutes(1), false);
        var historico = reembolso.getHistorico();

        assertThat(historico).extracting(DecisaoReembolso::getEtapa)
                .containsExactly(EtapaAprovacao.GESTOR, EtapaAprovacao.FINANCEIRO);
        assertThat(historico).extracting(DecisaoReembolso::getOrdem).containsExactly(1, 2);
        assertThatThrownBy(() -> historico.add(historico.getFirst()))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> historico.removeFirst()).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> historico.set(0, historico.get(1)))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(reembolso.getHistorico()).containsExactlyElementsOf(historico);
    }

    @Test
    void aprovacaoLegadaMantemProjecaoSemFabricarEventoDeEtapa() {
        Reembolso reembolso = novo();

        reembolso.aprovar("responsavel-v2", GESTOR_EM);

        assertThat(reembolso.getStatus()).isEqualTo(StatusReembolso.APROVADA);
        assertUltimaDecisao(reembolso, "responsavel-v2", GESTOR_EM, null);
        assertThat(reembolso.getHistorico()).isEmpty();
    }

    @Test
    void rejeicaoLegadaContinuaAceitandoMotivoAusenteSemFabricarEventoDeEtapa() {
        Reembolso reembolso = novo();

        reembolso.rejeitar("responsavel-v2", null, GESTOR_EM);

        assertThat(reembolso.getStatus()).isEqualTo(StatusReembolso.REJEITADA);
        assertUltimaDecisao(reembolso, "responsavel-v2", GESTOR_EM, null);
        assertThat(reembolso.getHistorico()).isEmpty();
    }

    private Reembolso novo() {
        return new Reembolso("solicitante", Categoria.TRANSPORTE, new BigDecimal("650.00"),
                LocalDate.of(2026, 11, 2), "11222333000181", "Despesa de teste",
                LocalDateTime.parse("2026-11-02T12:00:00"));
    }

    private void assertRecusaSemMutacao(Reembolso reembolso, Runnable tentativa) {
        var status = reembolso.getStatus();
        var historico = reembolso.getHistorico();
        var responsavel = reembolso.getDecididoPor();
        var quando = reembolso.getDecididoEm();
        var motivo = reembolso.getMotivoDecisao();

        assertThatThrownBy(tentativa::run)
                .isInstanceOf(TransicaoDeStatusInvalidaException.class)
                .hasMessageContaining(status.name());
        assertThat(reembolso.getStatus()).isEqualTo(status);
        assertUltimaDecisao(reembolso, responsavel, quando, motivo);
        assertThat(reembolso.getHistorico()).containsExactlyElementsOf(historico);
    }

    private void assertUltimaDecisao(Reembolso reembolso, String responsavel,
                                    LocalDateTime quando, String motivo) {
        assertThat(reembolso.getDecididoPor()).isEqualTo(responsavel);
        assertThat(reembolso.getDecididoEm()).isEqualTo(quando);
        assertThat(reembolso.getMotivoDecisao()).isEqualTo(motivo);
    }

    private void assertEvento(DecisaoReembolso evento, int ordem, EtapaAprovacao etapa,
                             StatusReembolso resultado, String responsavel,
                             LocalDateTime quando, String motivo) {
        assertThat(evento.getOrdem()).isEqualTo(ordem);
        assertThat(evento.getEtapa()).isEqualTo(etapa);
        assertThat(evento.getResultado()).isEqualTo(resultado);
        assertThat(evento.getResponsavel()).isEqualTo(responsavel);
        assertThat(evento.getQuando()).isEqualTo(quando);
        assertThat(evento.getMotivo()).isEqualTo(motivo);
    }
}

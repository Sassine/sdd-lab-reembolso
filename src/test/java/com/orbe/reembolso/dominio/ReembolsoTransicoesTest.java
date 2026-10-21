package com.orbe.reembolso.dominio;

import static com.orbe.reembolso.dominio.PapelAprovador.FINANCEIRO;
import static com.orbe.reembolso.dominio.PapelAprovador.GESTOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/** Spec 002: CA1, CA2, CA3, CA6, CA7, CA15 no nível da entidade. */
class ReembolsoTransicoesTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 20, 10, 0);

    private Reembolso pedido(String valor) {
        return new Reembolso("ana", Categoria.OUTROS, new BigDecimal(valor), LocalDate.of(2026, 10, 15),
                "11222333000181", "teste", AGORA.minusDays(1));
    }

    @Test
    void gestorAprovaDiretoQuandoNaoExigeFinanceiro() {
        Reembolso r = pedido("500.00");
        r.aprovar("bruno", GESTOR, false, AGORA);

        assertThat(r.getStatus()).isEqualTo(StatusReembolso.APROVADA);
        assertThat(r.getAprovadorGestor()).isEqualTo("bruno");
        assertThat(r.getDecididoPor()).isEqualTo("bruno");
        assertThat(r.getDecididoEm()).isEqualTo(AGORA);
    }

    @Test
    void gestorDeixaAguardandoFinanceiroQuandoExigido() {
        Reembolso r = pedido("500.01");
        r.aprovar("bruno", GESTOR, true, AGORA);

        assertThat(r.getStatus()).isEqualTo(StatusReembolso.AGUARDANDO_FINANCEIRO);
        assertThat(r.getAprovadorGestor()).isEqualTo("bruno");
        assertThat(r.getDecididoEm()).isNull();
    }

    @Test
    void financeiroConcluiAprovacao() {
        Reembolso r = pedido("800.00");
        r.aprovar("bruno", GESTOR, true, AGORA);
        r.aprovar("carla", FINANCEIRO, true, AGORA.plusHours(1));

        assertThat(r.getStatus()).isEqualTo(StatusReembolso.APROVADA);
        assertThat(r.getAprovadorFinanceiro()).isEqualTo("carla");
        assertThat(r.getDecididoPor()).isEqualTo("carla");
    }

    @Test
    void financeiroNaoAprovaAntesDoGestor() {
        Reembolso r = pedido("800.00");

        assertThatThrownBy(() -> r.aprovar("carla", FINANCEIRO, true, AGORA))
                .isInstanceOf(TransicaoDeStatusInvalidaException.class);
        assertThat(r.getStatus()).isEqualTo(StatusReembolso.SUBMETIDA);
        assertThat(r.getAprovadorFinanceiro()).isNull();
    }

    @Test
    void gestorNaoAprovaDeNovoQuandoAguardaFinanceiro() {
        Reembolso r = pedido("800.00");
        r.aprovar("bruno", GESTOR, true, AGORA);

        assertThatThrownBy(() -> r.aprovar("bruno", GESTOR, true, AGORA))
                .isInstanceOf(TransicaoDeStatusInvalidaException.class);
        assertThat(r.getStatus()).isEqualTo(StatusReembolso.AGUARDANDO_FINANCEIRO);
    }

    @Test
    void rejeitaPedidoAguardandoFinanceiro() {
        Reembolso r = pedido("800.00");
        r.aprovar("bruno", GESTOR, true, AGORA);
        r.rejeitar("carla", "Sem nota fiscal", AGORA.plusHours(2));

        assertThat(r.getStatus()).isEqualTo(StatusReembolso.REJEITADA);
        assertThat(r.getMotivoDecisao()).isEqualTo("Sem nota fiscal");
    }

    @Test
    void naoRejeitaPedidoJaAprovado() {
        Reembolso r = pedido("100.00");
        r.aprovar("bruno", GESTOR, false, AGORA);

        assertThatThrownBy(() -> r.rejeitar("carla", "tarde demais", AGORA))
                .isInstanceOf(TransicaoDeStatusInvalidaException.class);
    }
}

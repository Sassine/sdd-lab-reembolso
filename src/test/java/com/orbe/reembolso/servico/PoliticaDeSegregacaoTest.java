package com.orbe.reembolso.servico;

import static com.orbe.reembolso.dominio.PapelAprovador.FINANCEIRO;
import static com.orbe.reembolso.dominio.PapelAprovador.GESTOR;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.orbe.reembolso.dominio.Categoria;
import com.orbe.reembolso.dominio.Reembolso;
import com.orbe.reembolso.dominio.RegraDeNegocioException;

/** Spec 002: CA8, CA9. */
class PoliticaDeSegregacaoTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 20, 10, 0);
    private final PoliticaDeSegregacao politica = new PoliticaDeSegregacao();

    private Reembolso pedidoDaAna(String valor) {
        return new Reembolso("ana", Categoria.OUTROS, new BigDecimal(valor), LocalDate.of(2026, 10, 15),
                "11222333000181", null, AGORA);
    }

    @Test
    void solicitanteNaoAprovaOProprioPedidoMesmoComOutraGrafia() {
        assertThatThrownBy(() -> politica.validar(pedidoDaAna("100.00"), "  ANA ", GESTOR))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("não pode aprová-lo");
    }

    @Test
    void financeiroNaoPodeSerQuemAprovouComoGestor() {
        Reembolso r = pedidoDaAna("800.00");
        r.aprovar("bruno", GESTOR, true, AGORA);

        assertThatThrownBy(() -> politica.validar(r, "Bruno", FINANCEIRO))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("outra pessoa");
    }

    @Test
    void financeiroDiferenteDoGestorPode() {
        Reembolso r = pedidoDaAna("800.00");
        r.aprovar("bruno", GESTOR, true, AGORA);

        assertThatCode(() -> politica.validar(r, "carla", FINANCEIRO)).doesNotThrowAnyException();
    }

    @Test
    void gestorDiferenteDoSolicitantePode() {
        assertThatCode(() -> politica.validar(pedidoDaAna("100.00"), "bruno", GESTOR)).doesNotThrowAnyException();
    }
}

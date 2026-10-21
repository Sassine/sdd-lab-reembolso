package com.orbe.reembolso.servico;

import static com.orbe.reembolso.suporte.RelogioDeTeste.HOJE;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.orbe.reembolso.dominio.RegraDeNegocioException;
import com.orbe.reembolso.suporte.RelogioDeTeste;

class PoliticaDePrazoTest {

    private final PoliticaDePrazo politica = new PoliticaDePrazo(RelogioDeTeste.fixo());

    @Test
    void aceitaDespesaDeHoje() {
        assertThatCode(() -> politica.validar(HOJE)).doesNotThrowAnyException();
    }

    @Test
    void aceitaDespesaDe59DiasAtras() {
        assertThatCode(() -> politica.validar(HOJE.minusDays(59))).doesNotThrowAnyException();
    }

    /** Bugfix 003: "até 60 dias" inclui o 60º. 20/10/2026 − 60 dias = 21/08/2026. */
    @Test
    void aceitaDespesaDe60DiasAtras() {
        assertThatCode(() -> politica.validar(HOJE.minusDays(60))).doesNotThrowAnyException();
    }

    @Test
    void rejeitaDespesaDe61DiasAtras() {
        assertThatThrownBy(() -> politica.validar(HOJE.minusDays(61)))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Prazo de submissão expirado");
    }

    @Test
    void rejeitaDespesaNoFuturo() {
        assertThatThrownBy(() -> politica.validar(HOJE.plusDays(1)))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("futuro");
    }
}

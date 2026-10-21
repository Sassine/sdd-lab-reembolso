package com.orbe.reembolso.servico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

import com.orbe.reembolso.dominio.RegraDeNegocioException;

/** Spec 002: CA1, CA2, CA4, CA5. */
class PoliticaDeAlcadaTest {

    private final PoliticaDeAlcada politica = new PoliticaDeAlcada();

    @ParameterizedTest
    @CsvSource({
            "0.01,    false",
            "500.00,  false",
            "500.01,  true",
            "5000.00, true"
    })
    void decideQuandoOFinanceiroEExigido(String valor, boolean exige) {
        assertThat(politica.exigeFinanceiro(new BigDecimal(valor))).isEqualTo(exige);
    }

    @Test
    void aceitaExatamenteCincoMil() {
        assertThatCode(() -> politica.validarSubmissao(new BigDecimal("5000.00"))).doesNotThrowAnyException();
    }

    @Test
    void recusaAcimaDeCincoMilOrientandoAdiantamento() {
        assertThatThrownBy(() -> politica.validarSubmissao(new BigDecimal("5000.01")))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("adiantamento");
    }
}

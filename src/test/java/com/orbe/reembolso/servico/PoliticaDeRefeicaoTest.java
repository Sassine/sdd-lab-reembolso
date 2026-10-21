package com.orbe.reembolso.servico;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.orbe.reembolso.dominio.RegraDeNegocioException;

class PoliticaDeRefeicaoTest {

    private final PoliticaDeRefeicao politica = new PoliticaDeRefeicao();

    @ParameterizedTest
    @CsvSource({
            "0.00, 120.00",
            "80.00, 40.00",
            "80.00, 39.99"
    })
    void aceitaSubmissaoAteOTetoIncluindoAcumulado(String totalPersistido, String valorProposto) {
        assertThatCode(() -> politica.validarSubmissao(
                new BigDecimal(totalPersistido), new BigDecimal(valorProposto)))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource({
            "0.00, 120.01",
            "80.00, 40.01"
    })
    void recusaSubmissaoAcimaDoTetoIncluindoAcumulado(String totalPersistido, String valorProposto) {
        assertThatThrownBy(() -> politica.validarSubmissao(
                new BigDecimal(totalPersistido), new BigDecimal(valorProposto)))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("diário")
                .hasMessageContaining("R$ 120,00");
    }

    @ParameterizedTest
    @ValueSource(strings = {"119.99", "120.00"})
    void aprovaTotalJaReservadoSemSomarNovamenteOProprioPedido(String totalPersistido) {
        assertThatCode(() -> politica.validarAprovacao(new BigDecimal(totalPersistido)))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"120.01", "160.00"})
    void recusaAprovacaoQuandoTotalJaReservadoExcedeOTeto(String totalPersistido) {
        assertThatThrownBy(() -> politica.validarAprovacao(new BigDecimal(totalPersistido)))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("diário")
                .hasMessageContaining("R$ 120,00");
    }
}

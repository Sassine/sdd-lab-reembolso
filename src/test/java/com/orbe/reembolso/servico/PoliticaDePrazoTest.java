package com.orbe.reembolso.servico;

import static com.orbe.reembolso.suporte.RelogioDeTeste.HOJE;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

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

    @Test
    void rejeitaDespesaDeExatamente60DiasAtras() {
        assertThatThrownBy(() -> politica.validar(HOJE.minusDays(60)))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("Prazo de submissão expirado");
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

    @Test
    void consideraHojeEmBrasiliaMesmoComClockEmUtc() {
        Clock clockUtc = Clock.fixed(Instant.parse("2026-11-01T02:30:00Z"), ZoneOffset.UTC);
        PoliticaDePrazo politicaUtc = new PoliticaDePrazo(clockUtc);

        assertThatThrownBy(() -> politicaUtc.validar(LocalDate.of(2026, 11, 1)))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("futuro");
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-11-01", "2026-09-03"})
    void aceitaHojeE59DiasPelaReferenciaCapturada(String dataDespesa) {
        assertThatCode(() -> politica.validar(
                LocalDate.parse(dataDespesa), LocalDate.of(2026, 11, 1)))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource({
            "2026-09-02, Prazo de submissão expirado",
            "2026-09-01, Prazo de submissão expirado",
            "2026-11-02, futuro"
    })
    void rejeita60Dias61DiasEFuturoPelaReferenciaCapturada(String dataDespesa, String mensagem) {
        assertThatThrownBy(() -> politica.validar(
                LocalDate.parse(dataDespesa), LocalDate.of(2026, 11, 1)))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining(mensagem);
    }
}

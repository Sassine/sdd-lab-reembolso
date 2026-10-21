package com.orbe.reembolso.servico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import com.orbe.reembolso.dominio.VersaoPolitica;

class PoliticaDeVigenciaTest {

    private final PoliticaDeVigencia politica = new PoliticaDeVigencia();

    @Test
    void aplicaV2ImediatamenteAntesDoCorteEmBrasilia() {
        assertThat(politica.classificar(LocalDateTime.parse("2026-10-31T23:59:59.999999999")))
                .isEqualTo(VersaoPolitica.V2);
    }

    @Test
    void aplicaV3ExatamenteNoCorteEmBrasilia() {
        assertThat(politica.classificar(LocalDateTime.parse("2026-11-01T00:00:00")))
                .isEqualTo(VersaoPolitica.V3);
    }

    @Test
    void aplicaV3DepoisDoCorteEmBrasilia() {
        assertThat(politica.classificar(LocalDateTime.parse("2026-11-02T10:00:00")))
                .isEqualTo(VersaoPolitica.V3);
    }

    @Test
    void converteInstanteUtcAnteriorAoCorteParaBrasilia() {
        assertThat(politica.classificar(Instant.parse("2026-11-01T02:59:59.999999999Z")))
                .isEqualTo(VersaoPolitica.V2);
    }

    @Test
    void converteInstanteUtcExatamenteNoCorteParaBrasilia() {
        assertThat(politica.classificar(Instant.parse("2026-11-01T03:00:00Z")))
                .isEqualTo(VersaoPolitica.V3);
    }

    @Test
    void reutilizaUmaUnicaCapturaParaVigenciaPrazoERegistroMesmoSeRelogioCruzarOCorte() {
        Clock clock = mock(Clock.class);
        when(clock.instant()).thenReturn(
                Instant.parse("2026-11-01T02:59:59.999999999Z"),
                Instant.parse("2026-11-01T03:00:00Z"));
        PoliticaDePrazo prazo = new PoliticaDePrazo(clock);

        Instant capturado = clock.instant();
        LocalDateTime submetidoEm = LocalDateTime.ofInstant(capturado, ZoneId.of("America/Sao_Paulo"));

        assertThat(submetidoEm).isEqualTo(LocalDateTime.parse("2026-10-31T23:59:59.999999999"));
        assertThat(politica.classificar(capturado)).isEqualTo(VersaoPolitica.V2);
        assertThat(politica.classificar(submetidoEm)).isEqualTo(VersaoPolitica.V2);
        assertThatCode(() -> prazo.validar(LocalDate.of(2026, 9, 2), submetidoEm.toLocalDate()))
                .doesNotThrowAnyException();
        verify(clock).instant();
        verifyNoMoreInteractions(clock);
    }
}

package com.orbe.reembolso.servico;

import java.time.Instant;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.orbe.reembolso.config.ClockConfig;
import com.orbe.reembolso.dominio.VersaoPolitica;

/**
 * Define o regime pelo momento imutável da submissão, nunca pelo da decisão.
 */
@Component
public class PoliticaDeVigencia {

    private static final LocalDateTime INICIO_V3 = LocalDateTime.of(2026, 11, 1, 0, 0);

    public VersaoPolitica classificar(Instant submetidoEm) {
        return classificar(LocalDateTime.ofInstant(submetidoEm, ClockConfig.FUSO_PADRAO));
    }

    /** Recebe o momento da submissão registrado no horário de Brasília. */
    public VersaoPolitica classificar(LocalDateTime submetidoEm) {
        return submetidoEm.isBefore(INICIO_V3) ? VersaoPolitica.V2 : VersaoPolitica.V3;
    }
}

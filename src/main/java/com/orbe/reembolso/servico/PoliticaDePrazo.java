package com.orbe.reembolso.servico;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

import com.orbe.reembolso.config.ClockConfig;
import com.orbe.reembolso.dominio.RegraDeNegocioException;

/**
 * Política de Despesas, seção 2: prazo de submissão.
 */
@Component
public class PoliticaDePrazo {

    static final int PRAZO_EM_DIAS = 60;

    private final Clock clock;

    public PoliticaDePrazo(Clock clock) {
        this.clock = clock;
    }

    public void validar(LocalDate dataDespesa) {
        validar(dataDespesa, LocalDate.now(clock.withZone(ClockConfig.FUSO_PADRAO)));
    }

    /** A referência é o dia em Brasília derivado da captura temporal do comando. */
    public void validar(LocalDate dataDespesa, LocalDate dataReferencia) {
        if (dataDespesa.isAfter(dataReferencia)) {
            throw new RegraDeNegocioException("A data da despesa não pode estar no futuro.");
        }
        long dias = ChronoUnit.DAYS.between(dataDespesa, dataReferencia);
        if (dias >= PRAZO_EM_DIAS) {
            throw new RegraDeNegocioException(
                    "Prazo de submissão expirado: a política permite até %d dias após a despesa."
                            .formatted(PRAZO_EM_DIAS));
        }
    }
}

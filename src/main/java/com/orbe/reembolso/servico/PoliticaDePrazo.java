package com.orbe.reembolso.servico;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Component;

import com.orbe.reembolso.dominio.RegraDeNegocioException;

/**
 * Política de Despesas, seção 2: prazo de submissão. "Até 60 dias" inclui o 60º
 * (o dia da despesa é o dia zero). Ver specs/003-prazo-60-dias.
 */
@Component
public class PoliticaDePrazo {

    static final int PRAZO_EM_DIAS = 60;

    private final Clock clock;

    public PoliticaDePrazo(Clock clock) {
        this.clock = clock;
    }

    public void validar(LocalDate dataDespesa) {
        LocalDate hoje = LocalDate.now(clock);
        if (dataDespesa.isAfter(hoje)) {
            throw new RegraDeNegocioException("A data da despesa não pode estar no futuro.");
        }
        long dias = ChronoUnit.DAYS.between(dataDespesa, hoje);
        if (dias > PRAZO_EM_DIAS) {
            throw new RegraDeNegocioException(
                    "Prazo de submissão expirado: a política permite até %d dias após a despesa."
                            .formatted(PRAZO_EM_DIAS));
        }
    }
}

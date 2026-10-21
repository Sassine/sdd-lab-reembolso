package com.orbe.reembolso.servico;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.orbe.reembolso.dominio.RegraDeNegocioException;

/** Política de Despesas v3: teto diário de refeições. */
@Component
public class PoliticaDeRefeicao {

    private static final BigDecimal TETO_DIARIO = new BigDecimal("120.00");

    public void validarSubmissao(BigDecimal totalPersistido, BigDecimal valorProposto) {
        validarTotal(totalPersistido.add(valorProposto));
    }

    /** O total persistido já inclui a reserva do pedido em aprovação. */
    public void validarAprovacao(BigDecimal totalPersistido) {
        validarTotal(totalPersistido);
    }

    private void validarTotal(BigDecimal total) {
        if (total.compareTo(TETO_DIARIO) > 0) {
            throw new RegraDeNegocioException(
                    "O limite diário de refeições é de R$ 120,00.");
        }
    }
}

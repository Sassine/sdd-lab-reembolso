package com.orbe.reembolso.servico;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.orbe.reembolso.dominio.RegraDeNegocioException;

/**
 * Política de Despesas v3, seção 5 (spec 002, R1, R2, R3).
 * "Até" inclui o limite: R$ 500,00 é só gestor; R$ 5.000,00 ainda é reembolso.
 */
@Component
public class PoliticaDeAlcada {

    static final BigDecimal LIMITE_SO_GESTOR = new BigDecimal("500.00");
    static final BigDecimal LIMITE_REEMBOLSO = new BigDecimal("5000.00");

    public void validarSubmissao(BigDecimal valor) {
        if (valor.compareTo(LIMITE_REEMBOLSO) > 0) {
            throw new RegraDeNegocioException(
                    "Valores acima de R$ 5.000,00 não são reembolso: solicite adiantamento pelo processo de Viagens.");
        }
    }

    public boolean exigeFinanceiro(BigDecimal valor) {
        return valor.compareTo(LIMITE_SO_GESTOR) > 0;
    }
}

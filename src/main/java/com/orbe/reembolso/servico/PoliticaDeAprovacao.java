package com.orbe.reembolso.servico;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.orbe.reembolso.dominio.Reembolso;
import com.orbe.reembolso.dominio.RegraDeNegocioException;

/** Limites de submissão e alçadas de aprovação destinados às operações v3. */
@Component
public class PoliticaDeAprovacao {

    private static final BigDecimal LIMITE_SO_GESTOR = new BigDecimal("500.00");
    private static final BigDecimal LIMITE_REEMBOLSO = new BigDecimal("5000.00");

    private final PoliticaDeAutorizacao autorizacao;

    public PoliticaDeAprovacao(PoliticaDeAutorizacao autorizacao) {
        this.autorizacao = autorizacao;
    }

    public void validarSubmissao(BigDecimal valor) {
        if (valor.compareTo(LIMITE_REEMBOLSO) > 0) {
            throw new RegraDeNegocioException(
                    "Acima de R$ 5.000,00, o colaborador deve solicitar adiantamento pelo processo de Viagens.");
        }
    }

    /** Valida sem mutar e retorna a exigência de Financeiro para uso pelo agregado. */
    public boolean validar(Reembolso reembolso, DecidirReembolso decisao) {
        autorizacao.validar(reembolso, decisao);
        return reembolso.getValor().compareTo(LIMITE_SO_GESTOR) > 0;
    }
}

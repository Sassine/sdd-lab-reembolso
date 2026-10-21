package com.orbe.reembolso.servico;

import org.springframework.stereotype.Component;

import com.orbe.reembolso.dominio.Reembolso;
import com.orbe.reembolso.dominio.RegraDeNegocioException;

/** Autorização por etapa e motivo obrigatório para a rejeição v3. */
@Component
public class PoliticaDeRejeicao {

    private final PoliticaDeAutorizacao autorizacao;

    public PoliticaDeRejeicao(PoliticaDeAutorizacao autorizacao) {
        this.autorizacao = autorizacao;
    }

    /** Apenas valida; a transição e o registro da decisão cabem ao agregado. */
    public void validar(Reembolso reembolso, DecidirReembolso decisao) {
        autorizacao.validar(reembolso, decisao);
        if (decisao.motivo() == null || decisao.motivo().isBlank()) {
            throw new RegraDeNegocioException("O motivo da rejeição deve conter texto não branco.");
        }
    }
}

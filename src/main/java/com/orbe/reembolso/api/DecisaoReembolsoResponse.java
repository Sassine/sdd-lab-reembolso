package com.orbe.reembolso.api;

import java.time.LocalDateTime;

import com.orbe.reembolso.dominio.DecisaoReembolso;
import com.orbe.reembolso.dominio.EtapaAprovacao;
import com.orbe.reembolso.dominio.StatusReembolso;

public record DecisaoReembolsoResponse(
        EtapaAprovacao etapa,
        StatusReembolso resultado,
        String responsavel,
        LocalDateTime quando,
        String motivo) {

    static DecisaoReembolsoResponse de(DecisaoReembolso decisao) {
        return new DecisaoReembolsoResponse(decisao.getEtapa(), decisao.getResultado(),
                decisao.getResponsavel(), decisao.getQuando(), decisao.getMotivo());
    }
}

package com.orbe.reembolso.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.orbe.reembolso.dominio.Categoria;
import com.orbe.reembolso.dominio.EtapaAprovacao;
import com.orbe.reembolso.dominio.Reembolso;
import com.orbe.reembolso.dominio.StatusReembolso;
import com.orbe.reembolso.dominio.VersaoPolitica;

public record ReembolsoResponse(
        Long id,
        String colaborador,
        Categoria categoria,
        BigDecimal valor,
        LocalDate dataDespesa,
        String cnpjFornecedor,
        String descricao,
        StatusReembolso status,
        LocalDateTime submetidoEm,
        String decididoPor,
        String motivoDecisao,
        LocalDateTime decididoEm,
        VersaoPolitica politica,
        EtapaAprovacao etapaPendente,
        List<DecisaoReembolsoResponse> historico) {

    static ReembolsoResponse de(Reembolso r, VersaoPolitica politica) {
        EtapaAprovacao etapaPendente = politica == VersaoPolitica.V2 ? null : switch (r.getStatus()) {
            case SUBMETIDA -> EtapaAprovacao.GESTOR;
            case AGUARDANDO_FINANCEIRO -> EtapaAprovacao.FINANCEIRO;
            default -> null;
        };
        return new ReembolsoResponse(
                r.getId(), r.getColaborador(), r.getCategoria(), r.getValor(), r.getDataDespesa(),
                r.getCnpjFornecedor(), r.getDescricao(), r.getStatus(), r.getSubmetidoEm(),
                r.getDecididoPor(), r.getMotivoDecisao(), r.getDecididoEm(),
                politica, etapaPendente, r.getHistorico().stream().map(DecisaoReembolsoResponse::de).toList());
    }
}

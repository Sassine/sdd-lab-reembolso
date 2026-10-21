package com.orbe.reembolso.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.orbe.reembolso.dominio.Categoria;
import com.orbe.reembolso.dominio.Reembolso;
import com.orbe.reembolso.dominio.StatusReembolso;

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
        String aprovadorGestor,
        String aprovadorFinanceiro) {

    static ReembolsoResponse de(Reembolso r) {
        return new ReembolsoResponse(
                r.getId(), r.getColaborador(), r.getCategoria(), r.getValor(), r.getDataDespesa(),
                r.getCnpjFornecedor(), r.getDescricao(), r.getStatus(), r.getSubmetidoEm(),
                r.getDecididoPor(), r.getMotivoDecisao(), r.getDecididoEm(),
                r.getAprovadorGestor(), r.getAprovadorFinanceiro());
    }
}

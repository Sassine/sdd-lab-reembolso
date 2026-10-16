package com.orbe.reembolso.servico;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.orbe.reembolso.dominio.Categoria;

/** Comando de submissão, já validado na borda (formato). Regras de negócio ficam no serviço. */
public record NovoReembolso(
        String colaborador,
        Categoria categoria,
        BigDecimal valor,
        LocalDate dataDespesa,
        String cnpjFornecedor,
        String descricao) {
}

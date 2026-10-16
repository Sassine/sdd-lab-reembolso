package com.orbe.reembolso.api;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import com.orbe.reembolso.dominio.Categoria;
import com.orbe.reembolso.servico.NovoReembolso;

public record NovoReembolsoRequest(
        @NotBlank @Size(max = 80) String colaborador,
        @NotNull Categoria categoria,
        @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal valor,
        @NotNull LocalDate dataDespesa,
        @NotBlank @Pattern(regexp = "\\d{14}", message = "deve conter exatamente 14 dígitos") String cnpjFornecedor,
        @Size(max = 500) String descricao) {

    NovoReembolso paraComando() {
        return new NovoReembolso(colaborador.trim(), categoria, valor, dataDespesa, cnpjFornecedor, descricao);
    }
}

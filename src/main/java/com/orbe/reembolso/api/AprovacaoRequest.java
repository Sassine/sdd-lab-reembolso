package com.orbe.reembolso.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.orbe.reembolso.dominio.PapelAprovador;

/** Spec 002: o papel de quem aprova passa a ser obrigatório. */
public record AprovacaoRequest(
        @NotBlank @Size(max = 80) String responsavel,
        @NotNull PapelAprovador papel) {
}

package com.orbe.reembolso.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.orbe.reembolso.dominio.EtapaAprovacao;
import com.orbe.reembolso.dominio.PapelDeclarado;
import com.orbe.reembolso.servico.DecidirReembolso;

public record DecisaoRequest(
        @NotBlank @Size(max = 80) String responsavel,
        @Size(max = 500) String motivo,
        PapelDeclarado papel,
        String colaboradorVinculado,
        String gestorImediato,
        EtapaAprovacao etapa) {

    DecidirReembolso paraComando() {
        return new DecidirReembolso(responsavel.trim(), papel, trim(colaboradorVinculado),
                trim(gestorImediato), etapa, motivo);
    }

    private static String trim(String identificador) {
        return identificador == null ? null : identificador.trim();
    }
}

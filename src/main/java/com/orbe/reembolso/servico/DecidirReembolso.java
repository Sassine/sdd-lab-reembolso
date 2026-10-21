package com.orbe.reembolso.servico;

import com.orbe.reembolso.dominio.EtapaAprovacao;
import com.orbe.reembolso.dominio.PapelDeclarado;

/** Declarações da decisão; autorização e regras de negócio ficam nas políticas do serviço. */
public record DecidirReembolso(
        String responsavel,
        PapelDeclarado papel,
        String colaboradorVinculado,
        String gestorImediato,
        EtapaAprovacao etapa,
        String motivo) {
}

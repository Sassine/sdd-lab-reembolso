package com.orbe.reembolso.dominio;

import java.time.LocalDateTime;

/** Decisão aceita, imutável na aplicação e criada pelo agregado. */
public final class DecisaoReembolso {

    private final int ordem;
    private final EtapaAprovacao etapa;
    private final StatusReembolso resultado;
    private final String responsavel;
    private final LocalDateTime quando;
    private final String motivo;

    DecisaoReembolso(int ordem, EtapaAprovacao etapa, StatusReembolso resultado,
                     String responsavel, LocalDateTime quando, String motivo) {
        this.ordem = ordem;
        this.etapa = etapa;
        this.resultado = resultado;
        this.responsavel = responsavel;
        this.quando = quando;
        this.motivo = motivo;
    }

    public int getOrdem() { return ordem; }
    public EtapaAprovacao getEtapa() { return etapa; }
    public StatusReembolso getResultado() { return resultado; }
    public String getResponsavel() { return responsavel; }
    public LocalDateTime getQuando() { return quando; }
    public String getMotivo() { return motivo; }
}

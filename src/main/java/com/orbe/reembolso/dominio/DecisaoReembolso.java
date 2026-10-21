package com.orbe.reembolso.dominio;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/** Decisão aceita, imutável na aplicação e criada pelo agregado. */
@Entity
@Table(name = "decisao_reembolso", uniqueConstraints = @UniqueConstraint(
        name = "uk_decisao_reembolso_ordem", columnNames = {"reembolso_id", "ordem"}))
public class DecisaoReembolso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reembolso_id", nullable = false, updatable = false)
    private Reembolso reembolso;

    @Column(nullable = false, updatable = false)
    private int ordem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private EtapaAprovacao etapa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, updatable = false)
    private StatusReembolso resultado;

    @Column(nullable = false, length = 80, updatable = false)
    private String responsavel;

    @Column(nullable = false, updatable = false)
    private LocalDateTime quando;

    @Column(length = 500, updatable = false)
    private String motivo;

    protected DecisaoReembolso() {
        // exigido pelo JPA
    }

    DecisaoReembolso(Reembolso reembolso, int ordem, EtapaAprovacao etapa, StatusReembolso resultado,
                     String responsavel, LocalDateTime quando, String motivo) {
        this.reembolso = reembolso;
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

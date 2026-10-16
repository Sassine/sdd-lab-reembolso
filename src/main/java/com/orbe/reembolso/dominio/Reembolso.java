package com.orbe.reembolso.dominio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reembolso")
public class Reembolso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String colaborador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Categoria categoria;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDate dataDespesa;

    @Column(nullable = false, length = 14)
    private String cnpjFornecedor;

    @Column(length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusReembolso status;

    @Column(nullable = false)
    private LocalDateTime submetidoEm;

    @Column(length = 80)
    private String decididoPor;

    @Column(length = 500)
    private String motivoDecisao;

    private LocalDateTime decididoEm;

    protected Reembolso() {
        // exigido pelo JPA
    }

    public Reembolso(String colaborador, Categoria categoria, BigDecimal valor, LocalDate dataDespesa,
                     String cnpjFornecedor, String descricao, LocalDateTime submetidoEm) {
        this.colaborador = colaborador;
        this.categoria = categoria;
        this.valor = valor;
        this.dataDespesa = dataDespesa;
        this.cnpjFornecedor = cnpjFornecedor;
        this.descricao = descricao;
        this.submetidoEm = submetidoEm;
        this.status = StatusReembolso.SUBMETIDA;
    }

    public void aprovar(String responsavel, LocalDateTime quando) {
        exigirStatus(StatusReembolso.SUBMETIDA, "aprovar");
        this.status = StatusReembolso.APROVADA;
        registrarDecisao(responsavel, null, quando);
    }

    public void rejeitar(String responsavel, String motivo, LocalDateTime quando) {
        exigirStatus(StatusReembolso.SUBMETIDA, "rejeitar");
        this.status = StatusReembolso.REJEITADA;
        registrarDecisao(responsavel, motivo, quando);
    }

    private void exigirStatus(StatusReembolso esperado, String operacao) {
        if (status != esperado) {
            throw new TransicaoDeStatusInvalidaException(status, operacao);
        }
    }

    private void registrarDecisao(String responsavel, String motivo, LocalDateTime quando) {
        this.decididoPor = responsavel;
        this.motivoDecisao = motivo;
        this.decididoEm = quando;
    }

    public Long getId() { return id; }
    public String getColaborador() { return colaborador; }
    public Categoria getCategoria() { return categoria; }
    public BigDecimal getValor() { return valor; }
    public LocalDate getDataDespesa() { return dataDespesa; }
    public String getCnpjFornecedor() { return cnpjFornecedor; }
    public String getDescricao() { return descricao; }
    public StatusReembolso getStatus() { return status; }
    public LocalDateTime getSubmetidoEm() { return submetidoEm; }
    public String getDecididoPor() { return decididoPor; }
    public String getMotivoDecisao() { return motivoDecisao; }
    public LocalDateTime getDecididoEm() { return decididoEm; }
}

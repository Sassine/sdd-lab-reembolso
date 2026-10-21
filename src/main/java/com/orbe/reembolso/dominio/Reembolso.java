package com.orbe.reembolso.dominio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "reembolso", indexes = @Index(name = "idx_reembolso_colaborador_data_categoria",
        columnList = "colaborador, data_despesa, categoria"))
public class Reembolso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(nullable = false)
    private Long versao;

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

    @Column(nullable = false, updatable = false)
    private LocalDateTime submetidoEm;

    @Column(length = 80)
    private String decididoPor;

    @Column(length = 500)
    private String motivoDecisao;

    private LocalDateTime decididoEm;

    @OneToMany(mappedBy = "reembolso", cascade = CascadeType.ALL)
    @OrderBy("ordem ASC")
    private List<DecisaoReembolso> historico = new ArrayList<>();

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

    /**
     * Aceita uma aprovação já autorizada pelas políticas.
     * A política seleciona se o gestor exige Financeiro; a etapa financeira sempre conclui.
     */
    public void aprovar(EtapaAprovacao etapa, String responsavel, LocalDateTime quando,
                        boolean exigeFinanceiro) {
        aprovar(etapa, responsavel, quando, exigeFinanceiro, () -> {});
    }

    /** Permite validar a escrita do estado antes de acrescentar o evento, na mesma unidade de trabalho. */
    public void aprovar(EtapaAprovacao etapa, String responsavel, LocalDateTime quando,
                        boolean exigeFinanceiro, Runnable antesDoHistorico) {
        exigirEtapa(etapa, "aprovar na etapa " + etapa);
        this.status = etapa == EtapaAprovacao.GESTOR && exigeFinanceiro
                ? StatusReembolso.AGUARDANDO_FINANCEIRO : StatusReembolso.APROVADA;
        registrarDecisao(responsavel, null, quando);
        antesDoHistorico.run();
        adicionarAoHistorico(etapa, StatusReembolso.APROVADA, responsavel, null, quando);
    }

    /** Aceita uma rejeição cuja autorização e motivo já foram validados pelas políticas. */
    public void rejeitar(EtapaAprovacao etapa, String responsavel, String motivo, LocalDateTime quando) {
        rejeitar(etapa, responsavel, motivo, quando, () -> {});
    }

    public void rejeitar(EtapaAprovacao etapa, String responsavel, String motivo, LocalDateTime quando,
                         Runnable antesDoHistorico) {
        exigirEtapa(etapa, "rejeitar na etapa " + etapa);
        this.status = StatusReembolso.REJEITADA;
        registrarDecisao(responsavel, motivo, quando);
        antesDoHistorico.run();
        adicionarAoHistorico(etapa, StatusReembolso.REJEITADA, responsavel, motivo, quando);
    }

    public void exigirEtapa(EtapaAprovacao etapa, String operacao) {
        boolean etapaAtual = (etapa == EtapaAprovacao.GESTOR && status == StatusReembolso.SUBMETIDA)
                || (etapa == EtapaAprovacao.FINANCEIRO && status == StatusReembolso.AGUARDANDO_FINANCEIRO);
        if (!etapaAtual) {
            throw new TransicaoDeStatusInvalidaException(status, operacao);
        }
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

    private void adicionarAoHistorico(EtapaAprovacao etapa, StatusReembolso resultado, String responsavel,
                                     String motivo, LocalDateTime quando) {
        historico.add(new DecisaoReembolso(this, historico.size() + 1, etapa, resultado,
                responsavel, quando, motivo));
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
    public List<DecisaoReembolso> getHistorico() { return List.copyOf(historico); }
}

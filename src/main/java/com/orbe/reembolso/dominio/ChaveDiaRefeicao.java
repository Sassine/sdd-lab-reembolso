package com.orbe.reembolso.dominio;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ChaveDiaRefeicao implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "colaborador", nullable = false, length = 80)
    private String colaborador;

    @Column(name = "data_despesa", nullable = false)
    private LocalDate dataDespesa;

    protected ChaveDiaRefeicao() {
    }

    public ChaveDiaRefeicao(String colaborador, LocalDate dataDespesa) {
        this.colaborador = Objects.requireNonNull(colaborador, "colaborador");
        this.dataDespesa = Objects.requireNonNull(dataDespesa, "dataDespesa");
        if (colaborador.length() > 80) {
            throw new IllegalArgumentException("Colaborador deve ter no maximo 80 caracteres");
        }
    }

    public String getColaborador() {
        return colaborador;
    }

    public LocalDate getDataDespesa() {
        return dataDespesa;
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }
        return outro instanceof ChaveDiaRefeicao chave
                && Objects.equals(colaborador, chave.colaborador)
                && Objects.equals(dataDespesa, chave.dataDespesa);
    }

    @Override
    public int hashCode() {
        return Objects.hash(colaborador, dataDespesa);
    }
}

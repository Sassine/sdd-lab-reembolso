package com.orbe.reembolso.dominio;

import java.util.Objects;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** Ancora de sincronizacao persistente; nao armazena saldo nem reserva. */
@Entity
@Table(name = "dia_refeicao")
public class DiaRefeicao {

    @EmbeddedId
    private ChaveDiaRefeicao chave;

    protected DiaRefeicao() {
    }

    public DiaRefeicao(ChaveDiaRefeicao chave) {
        this.chave = Objects.requireNonNull(chave, "chave");
    }

    public ChaveDiaRefeicao getChave() {
        return chave;
    }
}

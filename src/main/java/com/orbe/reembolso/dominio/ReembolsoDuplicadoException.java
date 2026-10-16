package com.orbe.reembolso.dominio;

/** Mesmo colaborador, fornecedor, valor e data já submetidos. A API responde 409. */
public class ReembolsoDuplicadoException extends RuntimeException {

    public ReembolsoDuplicadoException() {
        super("Já existe um reembolso com o mesmo colaborador, fornecedor, valor e data.");
    }
}

package com.orbe.reembolso.dominio;

/** A operação não vale para o status atual do reembolso. A API responde 409. */
public class TransicaoDeStatusInvalidaException extends RuntimeException {

    public TransicaoDeStatusInvalidaException(StatusReembolso atual, String operacao) {
        super("Não é possível %s um reembolso com status %s.".formatted(operacao, atual));
    }
}

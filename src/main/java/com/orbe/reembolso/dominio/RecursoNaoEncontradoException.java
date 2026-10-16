package com.orbe.reembolso.dominio;

/** A API responde 404. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(Long id) {
        super("Reembolso %d não encontrado.".formatted(id));
    }
}

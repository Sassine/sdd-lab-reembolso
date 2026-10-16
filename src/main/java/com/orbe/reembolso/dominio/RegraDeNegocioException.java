package com.orbe.reembolso.dominio;

/** Violação de uma regra da Política de Despesas. A API responde 422. */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}

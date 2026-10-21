package com.orbe.reembolso.servico;

import java.util.Locale;

import org.springframework.stereotype.Component;

import com.orbe.reembolso.dominio.PapelAprovador;
import com.orbe.reembolso.dominio.Reembolso;
import com.orbe.reembolso.dominio.RegraDeNegocioException;

/**
 * Política de Despesas v3, seção 5, último parágrafo (spec 002, R5).
 * Nomes são comparados sem diferenciar maiúsculas e espaços nas pontas.
 */
@Component
public class PoliticaDeSegregacao {

    public void validar(Reembolso reembolso, String responsavel, PapelAprovador papel) {
        if (mesmaPessoa(responsavel, reembolso.getColaborador())) {
            throw new RegraDeNegocioException("Quem solicitou o reembolso não pode aprová-lo.");
        }
        if (papel == PapelAprovador.FINANCEIRO && mesmaPessoa(responsavel, reembolso.getAprovadorGestor())) {
            throw new RegraDeNegocioException(
                    "A aprovação do Financeiro precisa ser feita por outra pessoa, não por quem aprovou como gestor.");
        }
    }

    private static boolean mesmaPessoa(String a, String b) {
        return a != null && b != null && normalizar(a).equals(normalizar(b));
    }

    private static String normalizar(String nome) {
        return nome.trim().toLowerCase(Locale.ROOT);
    }
}

package com.orbe.reembolso.servico;

import org.springframework.stereotype.Component;

import com.orbe.reembolso.dominio.EtapaAprovacao;
import com.orbe.reembolso.dominio.PapelDeclarado;
import com.orbe.reembolso.dominio.Reembolso;
import com.orbe.reembolso.dominio.RegraDeNegocioException;
import com.orbe.reembolso.dominio.StatusReembolso;
import com.orbe.reembolso.dominio.TransicaoDeStatusInvalidaException;

/** Autorização declarada do laboratório, comum à aprovação e à rejeição v3. */
@Component
public class PoliticaDeAutorizacao {

    /** Apenas valida declarações; não registra decisões nem altera o pedido ou o comando. */
    public void validar(Reembolso reembolso, DecidirReembolso decisao) {
        EtapaAprovacao etapa = decisao.etapa();
        if (etapa == null) {
            throw new RegraDeNegocioException("A etapa da decisão deve ser declarada.");
        }
        boolean etapaAtual = (etapa == EtapaAprovacao.GESTOR
                && reembolso.getStatus() == StatusReembolso.SUBMETIDA)
                || (etapa == EtapaAprovacao.FINANCEIRO
                && reembolso.getStatus() == StatusReembolso.AGUARDANDO_FINANCEIRO);
        if (!etapaAtual) {
            throw new TransicaoDeStatusInvalidaException(reembolso.getStatus(),
                    "decidir na etapa " + etapa);
        }

        String responsavel = identificador(decisao.responsavel());
        if (responsavel.isBlank()) {
            throw new RegraDeNegocioException("A identidade do responsável deve ser declarada.");
        }
        if (responsavel.length() > 80) {
            throw new RegraDeNegocioException("A identidade do responsável deve ter no máximo 80 caracteres.");
        }
        PapelDeclarado papelEsperado = etapa == EtapaAprovacao.GESTOR
                ? PapelDeclarado.GESTOR : PapelDeclarado.FINANCEIRO;
        if (decisao.papel() != papelEsperado) {
            throw new RegraDeNegocioException("O papel declarado deve ser " + papelEsperado
                    + " para decidir nesta etapa.");
        }
        String vinculo = identificador(decisao.colaboradorVinculado());
        if (vinculo.isBlank() || !vinculo.equals(reembolso.getColaborador())) {
            throw new RegraDeNegocioException("O vínculo declarado deve corresponder ao solicitante do pedido.");
        }
        if (responsavel.equals(reembolso.getColaborador())) {
            throw new RegraDeNegocioException("O solicitante não pode decidir sobre o próprio pedido.");
        }
        if (etapa == EtapaAprovacao.GESTOR) {
            String gestor = identificador(decisao.gestorImediato());
            if (gestor.isBlank() || !gestor.equals(responsavel)) {
                throw new RegraDeNegocioException("O responsável deve ser o gestor imediato declarado.");
            }
        } else if (reembolso.getHistorico().stream().anyMatch(evento ->
                evento.getEtapa() == EtapaAprovacao.GESTOR
                        && evento.getResultado() == StatusReembolso.APROVADA
                        && responsavel.equals(evento.getResponsavel()))) {
            throw new RegraDeNegocioException(
                    "O Financeiro deve ser pessoa distinta de quem aprovou como gestor.");
        }
    }

    private static String identificador(String valor) {
        return valor == null ? "" : valor.trim();
    }
}

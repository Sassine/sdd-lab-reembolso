package com.orbe.reembolso.servico;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.orbe.reembolso.dominio.Categoria;
import com.orbe.reembolso.dominio.RegraDeNegocioException;
import com.orbe.reembolso.dominio.StatusReembolso;

/**
 * Política de Despesas v3, seção 4.1 (spec 002, R6).
 * Teto por colaborador e por data da despesa; pedidos rejeitados não contam.
 */
@Component
public class PoliticaTetoRefeicao {

    static final BigDecimal TETO_DIARIO = new BigDecimal("120.00");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ReembolsoRepository repositorio;

    public PoliticaTetoRefeicao(ReembolsoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void validar(String colaborador, LocalDate dataDespesa, BigDecimal valor) {
        BigDecimal existente = repositorio.somarValorPorDia(
                colaborador, Categoria.REFEICAO, dataDespesa, StatusReembolso.REJEITADA);
        if (existente == null) {
            existente = BigDecimal.ZERO;
        }
        BigDecimal total = existente.add(valor);
        if (total.compareTo(TETO_DIARIO) > 0) {
            throw new RegraDeNegocioException(
                    "Teto diário de refeição excedido em %s: já existem %s em refeições; com este pedido seriam %s (teto %s)."
                            .formatted(DATA.format(dataDespesa), reais(existente), reais(total), reais(TETO_DIARIO)));
        }
    }

    private static String reais(BigDecimal valor) {
        return "R$ " + valor.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }
}

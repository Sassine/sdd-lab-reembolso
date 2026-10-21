package com.orbe.reembolso.servico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.orbe.reembolso.dominio.Categoria;
import com.orbe.reembolso.dominio.EtapaAprovacao;
import com.orbe.reembolso.dominio.PapelDeclarado;
import com.orbe.reembolso.dominio.Reembolso;
import com.orbe.reembolso.dominio.RegraDeNegocioException;
import com.orbe.reembolso.dominio.TransicaoDeStatusInvalidaException;

class PoliticaDeAprovacaoTest {

    private static final LocalDateTime QUANDO = LocalDateTime.of(2026, 11, 3, 10, 0);
    private final PoliticaDeAprovacao politica =
            new PoliticaDeAprovacao(new PoliticaDeAutorizacao());

    @ParameterizedTest
    @ValueSource(strings = {"100.00", "500.00", "500.01", "5000.00"})
    void aceitaSubmissaoAteCincoMilSemAlterarValor(String valor) {
        BigDecimal proposto = new BigDecimal(valor);

        assertThatCode(() -> politica.validarSubmissao(proposto)).doesNotThrowAnyException();

        assertThat(proposto).isEqualTo(new BigDecimal(valor));
        assertThat(proposto.scale()).isEqualTo(2);
    }

    @Test
    void recusaSubmissaoAcimaDeCincoMilOrientandoAdiantamentoPorViagens() {
        BigDecimal proposto = new BigDecimal("5000.01");

        assertThatThrownBy(() -> politica.validarSubmissao(proposto))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("solicitar adiantamento")
                .hasMessageContaining("processo de Viagens");

        assertThat(proposto).isEqualTo(new BigDecimal("5000.01"));
    }

    @ParameterizedTest
    @MethodSource("alcadas")
    void selecionaAlcadaDoGestorSemAlterarPedidoOuHistorico(String valor, boolean exigeFinanceiro) {
        Reembolso pedido = pedido(valor, false);

        assertThat(politica.validar(pedido, gestor("B"))).isEqualTo(exigeFinanceiro);

        assertPreservado(pedido, valor, false);
    }

    static Stream<Arguments> alcadas() {
        return Stream.of(Arguments.of("100.00", false), Arguments.of("500.00", false),
                Arguments.of("500.01", true), Arguments.of("5000.00", true));
    }

    @ParameterizedTest
    @ValueSource(strings = {"500.01", "5000.00"})
    void autorizaFinanceiroDistintoSemConcluirOuRegravarDecisaoDoGestor(String valor) {
        Reembolso pedido = pedido(valor, true);

        assertThat(politica.validar(pedido, financeiro("C"))).isTrue();

        assertPreservado(pedido, valor, true);
    }

    @Test
    void recusaAutoaprovacaoPelaAutorizacaoRealSemAlterarPedido() {
        Reembolso pedido = pedido("500.00", false);

        assertThatThrownBy(() -> politica.validar(pedido, gestor("A")))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("próprio pedido");

        assertPreservado(pedido, "500.00", false);
    }

    @Test
    void recusaGestorComoFinanceiroPreservandoSuaAprovacaoAnterior() {
        Reembolso pedido = pedido("500.01", true);

        assertThatThrownBy(() -> politica.validar(pedido, financeiro(" B ")))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("pessoa distinta");

        assertPreservado(pedido, "500.01", true);
    }

    @Test
    void recusaFinanceiroAntecipadoAntesDeAvaliarDeclaracoesInvalidas() {
        Reembolso pedido = pedido("5000.00", false);
        DecidirReembolso decisao = new DecidirReembolso(
                null, null, null, null, EtapaAprovacao.FINANCEIRO, null);

        assertThatThrownBy(() -> politica.validar(pedido, decisao))
                .isInstanceOf(TransicaoDeStatusInvalidaException.class);

        assertPreservado(pedido, "5000.00", false);
    }

    @Test
    void delegaPedidoEComandoOriginaisEPropagaRecusaDaAutorizacaoInjetada() {
        PoliticaDeAutorizacao autorizacao = mock(PoliticaDeAutorizacao.class);
        PoliticaDeAprovacao politicaComColaborador = new PoliticaDeAprovacao(autorizacao);
        Reembolso pedido = pedido("500.00", false);
        DecidirReembolso decisao = gestor("B");
        RegraDeNegocioException recusa = new RegraDeNegocioException("Decisão não autorizada.");
        doThrow(recusa).when(autorizacao).validar(pedido, decisao);

        assertThatThrownBy(() -> politicaComColaborador.validar(pedido, decisao)).isSameAs(recusa);

        verify(autorizacao).validar(pedido, decisao);
        assertPreservado(pedido, "500.00", false);
    }

    private static DecidirReembolso gestor(String responsavel) {
        return new DecidirReembolso(responsavel, PapelDeclarado.GESTOR, "A", responsavel,
                EtapaAprovacao.GESTOR, null);
    }

    private static DecidirReembolso financeiro(String responsavel) {
        return new DecidirReembolso(responsavel, PapelDeclarado.FINANCEIRO, "A", null,
                EtapaAprovacao.FINANCEIRO, null);
    }

    private static Reembolso pedido(String valor, boolean aguardandoFinanceiro) {
        Reembolso pedido = new Reembolso("A", Categoria.HOSPEDAGEM, new BigDecimal(valor),
                LocalDate.of(2026, 11, 2), "11222333000181", "Viagem fictícia", QUANDO.minusDays(1));
        if (aguardandoFinanceiro) {
            pedido.aprovar(EtapaAprovacao.GESTOR, "B", QUANDO, true);
        }
        return pedido;
    }

    private static void assertPreservado(Reembolso pedido, String valor, boolean aguardandoFinanceiro) {
        assertThat(pedido).usingRecursiveComparison().isEqualTo(pedido(valor, aguardandoFinanceiro));
    }
}

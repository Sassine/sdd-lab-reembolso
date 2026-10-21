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
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import com.orbe.reembolso.dominio.Categoria;
import com.orbe.reembolso.dominio.EtapaAprovacao;
import com.orbe.reembolso.dominio.PapelDeclarado;
import com.orbe.reembolso.dominio.Reembolso;
import com.orbe.reembolso.dominio.RegraDeNegocioException;
import com.orbe.reembolso.dominio.TransicaoDeStatusInvalidaException;

class PoliticaDeRejeicaoTest {

    private static final LocalDateTime QUANDO = LocalDateTime.of(2026, 11, 3, 10, 0);
    private final PoliticaDeRejeicao politica =
            new PoliticaDeRejeicao(new PoliticaDeAutorizacao());

    @ParameterizedTest
    @EnumSource(EtapaAprovacao.class)
    void aceitaResponsavelDaEtapaComMotivoSemRejeitarOuAlterarHistorico(EtapaAprovacao etapa) {
        Reembolso pedido = pedido(etapa);
        DecidirReembolso decisao = decisao(etapa, "  Despesa particular  ");

        assertThatCode(() -> politica.validar(pedido, decisao)).doesNotThrowAnyException();

        assertPreservado(pedido, etapa);
        assertThat(decisao.motivo()).isEqualTo("  Despesa particular  ");
    }

    @ParameterizedTest
    @MethodSource("motivosSemConteudo")
    void exigeMotivoComConteudoSemAlterarPedidoOuHistorico(EtapaAprovacao etapa, String motivo) {
        Reembolso pedido = pedido(etapa);

        assertThatThrownBy(() -> politica.validar(pedido, decisao(etapa, motivo)))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("motivo");

        assertPreservado(pedido, etapa);
    }

    static Stream<Arguments> motivosSemConteudo() {
        return Stream.of(EtapaAprovacao.values()).flatMap(etapa ->
                Stream.of(null, "", "   ", " \t\r\n ", "\u2003")
                        .map(motivo -> Arguments.of(etapa, motivo)));
    }

    @ParameterizedTest
    @EnumSource(EtapaAprovacao.class)
    void delegaPedidoEComandoOriginaisEPropagaRecusaDaAutorizacaoInjetada(EtapaAprovacao etapa) {
        PoliticaDeAutorizacao autorizacao = mock(PoliticaDeAutorizacao.class);
        PoliticaDeRejeicao politicaComColaborador = new PoliticaDeRejeicao(autorizacao);
        Reembolso pedido = pedido(etapa);
        DecidirReembolso decisao = decisao(etapa, "Despesa particular");
        RegraDeNegocioException recusa = new RegraDeNegocioException("Decisão não autorizada.");
        doThrow(recusa).when(autorizacao).validar(pedido, decisao);

        assertThatThrownBy(() -> politicaComColaborador.validar(pedido, decisao)).isSameAs(recusa);

        verify(autorizacao).validar(pedido, decisao);
        assertPreservado(pedido, etapa);
    }

    @ParameterizedTest
    @EnumSource(EtapaAprovacao.class)
    void recusaEtapaDiferenteDaAtualSemAlterarPedido(EtapaAprovacao pendencia) {
        Reembolso pedido = pedido(pendencia);
        EtapaAprovacao outra = pendencia == EtapaAprovacao.GESTOR
                ? EtapaAprovacao.FINANCEIRO : EtapaAprovacao.GESTOR;

        assertThatThrownBy(() -> politica.validar(pedido, decisao(outra, "Despesa particular")))
                .isInstanceOf(TransicaoDeStatusInvalidaException.class);

        assertPreservado(pedido, pendencia);
    }

    @Test
    void recusaEtapaAusenteSemInferirDoPapel() {
        Reembolso pedido = pedido(EtapaAprovacao.GESTOR);
        DecidirReembolso decisao = new DecidirReembolso(
                "B", PapelDeclarado.GESTOR, "A", "B", null, "Despesa particular");

        assertThatThrownBy(() -> politica.validar(pedido, decisao))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("etapa");

        assertPreservado(pedido, EtapaAprovacao.GESTOR);
    }

    @Test
    void recusaGestorHistoricoComoFinanceiroPreservandoAprovacaoAnterior() {
        Reembolso pedido = pedido(EtapaAprovacao.FINANCEIRO);
        DecidirReembolso decisao = new DecidirReembolso(
                "B", PapelDeclarado.FINANCEIRO, "A", "D",
                EtapaAprovacao.FINANCEIRO, "Despesa particular");

        assertThatThrownBy(() -> politica.validar(pedido, decisao))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("pessoa distinta");

        assertPreservado(pedido, EtapaAprovacao.FINANCEIRO);
    }

    private static DecidirReembolso decisao(EtapaAprovacao etapa, String motivo) {
        return etapa == EtapaAprovacao.GESTOR
                ? new DecidirReembolso("B", PapelDeclarado.GESTOR, "A", "B", etapa, motivo)
                : new DecidirReembolso("C", PapelDeclarado.FINANCEIRO, "A", null, etapa, motivo);
    }

    private static Reembolso pedido(EtapaAprovacao etapa) {
        Reembolso pedido = new Reembolso("A", Categoria.HOSPEDAGEM, new BigDecimal("800.00"),
                LocalDate.of(2026, 11, 2), "11222333000181", "Viagem fictícia", QUANDO.minusDays(1));
        if (etapa == EtapaAprovacao.FINANCEIRO) {
            pedido.aprovar(EtapaAprovacao.GESTOR, "B", QUANDO, true);
        }
        return pedido;
    }

    private static void assertPreservado(Reembolso pedido, EtapaAprovacao etapa) {
        assertThat(pedido).usingRecursiveComparison().isEqualTo(pedido(etapa));
    }
}

package com.orbe.reembolso.servico;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.orbe.reembolso.dominio.Categoria;
import com.orbe.reembolso.dominio.EtapaAprovacao;
import com.orbe.reembolso.dominio.PapelDeclarado;
import com.orbe.reembolso.dominio.Reembolso;
import com.orbe.reembolso.dominio.RegraDeNegocioException;
import com.orbe.reembolso.dominio.TransicaoDeStatusInvalidaException;

class PoliticaDeAutorizacaoTest {

    private static final LocalDateTime QUANDO = LocalDateTime.of(2026, 11, 3, 10, 0);
    private final PoliticaDeAutorizacao politica = new PoliticaDeAutorizacao();

    @ParameterizedTest
    @EnumSource(EtapaAprovacao.class)
    void autorizaResponsavelDaEtapaSemAlterarPedidoOuHistorico(EtapaAprovacao etapa) {
        Reembolso pedido = pedido(etapa);
        DecidirReembolso decisao = etapa == EtapaAprovacao.GESTOR
                ? decisao("B", PapelDeclarado.GESTOR, "A", "B", etapa, null)
                : decisao("C", PapelDeclarado.FINANCEIRO, "A", null, etapa, null);

        assertThatCode(() -> politica.validar(pedido, decisao)).doesNotThrowAnyException();

        assertPreservado(pedido, etapa);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"Despesa particular"})
    void autorizaFinanceiroComNovoGestorSemReatribuirAprovacaoAnterior(String motivo) {
        Reembolso pedido = pedido(EtapaAprovacao.FINANCEIRO);

        assertThatCode(() -> politica.validar(pedido, decisao(
                "C", PapelDeclarado.FINANCEIRO, "A", "D", EtapaAprovacao.FINANCEIRO, motivo)))
                .doesNotThrowAnyException();

        assertPreservado(pedido, EtapaAprovacao.FINANCEIRO);
        assertThat(pedido.getHistorico()).singleElement()
                .extracting(evento -> evento.getResponsavel()).isEqualTo("B");
    }

    @ParameterizedTest
    @MethodSource("declaracoesInvalidas")
    void recusaDeclaracaoInvalidaComExplicacaoSemAlterarPedido(
            DecidirReembolso decisao, String explicacao) {
        Reembolso pedido = pedido(decisao.etapa());

        assertThatThrownBy(() -> politica.validar(pedido, decisao))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining(explicacao);

        assertPreservado(pedido, decisao.etapa());
    }

    static Stream<Arguments> declaracoesInvalidas() {
        return Stream.of(
                invalida(null, PapelDeclarado.GESTOR, "A", "B", EtapaAprovacao.GESTOR, "responsável"),
                invalida(" \t ", PapelDeclarado.GESTOR, "A", "B", EtapaAprovacao.GESTOR, "responsável"),
                invalida("B".repeat(81), PapelDeclarado.GESTOR, "A", "B".repeat(81),
                        EtapaAprovacao.GESTOR, "80"),
                invalida("B", null, "A", "B", EtapaAprovacao.GESTOR, "papel"),
                invalida("C", null, "A", null, EtapaAprovacao.FINANCEIRO, "papel"),
                invalida("B", PapelDeclarado.GESTOR, null, "B", EtapaAprovacao.GESTOR, "vínculo"),
                invalida("C", PapelDeclarado.FINANCEIRO, "  ", null,
                        EtapaAprovacao.FINANCEIRO, "vínculo"),
                invalida("B", PapelDeclarado.GESTOR, "D", "B", EtapaAprovacao.GESTOR, "vínculo"),
                invalida("C", PapelDeclarado.FINANCEIRO, "a", null,
                        EtapaAprovacao.FINANCEIRO, "vínculo"),
                invalida("B", PapelDeclarado.GESTOR, "A", null, EtapaAprovacao.GESTOR, "gestor"),
                invalida("B", PapelDeclarado.GESTOR, "A", "  ", EtapaAprovacao.GESTOR, "gestor"),
                invalida("C", PapelDeclarado.GESTOR, "A", "B", EtapaAprovacao.GESTOR, "gestor"),
                invalida("b", PapelDeclarado.GESTOR, "A", "B", EtapaAprovacao.GESTOR, "gestor"),
                invalida("B", PapelDeclarado.FINANCEIRO, "A", "B", EtapaAprovacao.GESTOR, "papel"),
                invalida("C", PapelDeclarado.GESTOR, "A", "C", EtapaAprovacao.FINANCEIRO, "papel"));
    }

    @ParameterizedTest
    @MethodSource("autodecisoes")
    void recusaAutoaprovacaoEAutorrejeicao(EtapaAprovacao etapa, String motivo) {
        Reembolso pedido = pedido(etapa);
        PapelDeclarado papel = etapa == EtapaAprovacao.GESTOR
                ? PapelDeclarado.GESTOR : PapelDeclarado.FINANCEIRO;

        assertThatThrownBy(() -> politica.validar(pedido,
                decisao(" A ", papel, "A", "A", etapa, motivo)))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("próprio pedido");

        assertPreservado(pedido, etapa);
    }

    static Stream<Arguments> autodecisoes() {
        return Stream.of(Arguments.of(EtapaAprovacao.GESTOR, null),
                Arguments.of(EtapaAprovacao.GESTOR, "Despesa particular"),
                Arguments.of(EtapaAprovacao.FINANCEIRO, null),
                Arguments.of(EtapaAprovacao.FINANCEIRO, "Despesa particular"));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"Despesa particular"})
    void recusaGestorHistoricoComoFinanceiroMesmoDeclarandoNovoGestor(String motivo) {
        Reembolso pedido = pedido(EtapaAprovacao.FINANCEIRO);

        assertThatThrownBy(() -> politica.validar(pedido, decisao(
                " B ", PapelDeclarado.FINANCEIRO, "A", "D", EtapaAprovacao.FINANCEIRO, motivo)))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("pessoa distinta");

        assertPreservado(pedido, EtapaAprovacao.FINANCEIRO);
    }

    @ParameterizedTest
    @EnumSource(EtapaAprovacao.class)
    void comparaIdentificadoresComTrimSemAlterarDeclaracoes(EtapaAprovacao etapa) {
        Reembolso pedido = pedido(etapa);
        DecidirReembolso decisao = etapa == EtapaAprovacao.GESTOR
                ? decisao(" B ", PapelDeclarado.GESTOR, " A ", " B ", etapa, null)
                : decisao(" b ", PapelDeclarado.FINANCEIRO, " A ", null, etapa, null);

        assertThatCode(() -> politica.validar(pedido, decisao)).doesNotThrowAnyException();

        assertPreservado(pedido, etapa);
        assertThat(decisao.responsavel()).startsWith(" ").endsWith(" ");
    }

    @ParameterizedTest
    @EnumSource(EtapaAprovacao.class)
    void exigeEtapaDeclaradaSemInferirDoPapel(EtapaAprovacao pendencia) {
        Reembolso pedido = pedido(pendencia);
        PapelDeclarado papel = pendencia == EtapaAprovacao.GESTOR
                ? PapelDeclarado.GESTOR : PapelDeclarado.FINANCEIRO;

        assertThatThrownBy(() -> politica.validar(pedido, decisao("B", papel, "A", "B", null, null)))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("etapa");

        assertPreservado(pedido, pendencia);
    }

    @ParameterizedTest
    @EnumSource(EtapaAprovacao.class)
    void recusaEtapaForaDeOrdemAntesDeAvaliarAutorizacao(EtapaAprovacao pendencia) {
        Reembolso pedido = pedido(pendencia);
        EtapaAprovacao outra = pendencia == EtapaAprovacao.GESTOR
                ? EtapaAprovacao.FINANCEIRO : EtapaAprovacao.GESTOR;

        assertThatThrownBy(() -> politica.validar(pedido, decisao(null, null, null, null, outra, null)))
                .isInstanceOf(TransicaoDeStatusInvalidaException.class)
                .hasMessageContaining(pedido.getStatus().name());

        assertPreservado(pedido, pendencia);
    }

    @ParameterizedTest
    @MethodSource("pedidosEncerrados")
    void recusaDecisaoEmPedidoEncerrado(Reembolso pedido, Reembolso esperado, EtapaAprovacao etapa) {
        assertThatThrownBy(() -> politica.validar(pedido, decisao(null, null, null, null, etapa, null)))
                .isInstanceOf(TransicaoDeStatusInvalidaException.class)
                .hasMessageContaining(pedido.getStatus().name());

        assertThat(pedido).usingRecursiveComparison().isEqualTo(esperado);
    }

    static Stream<Arguments> pedidosEncerrados() {
        return Stream.of(false, true).flatMap(rejeitado -> Stream.of(EtapaAprovacao.values())
                .map(etapa -> Arguments.of(encerrado(rejeitado), encerrado(rejeitado), etapa)));
    }

    private static Reembolso encerrado(boolean rejeitado) {
        Reembolso pedido = pedido(EtapaAprovacao.FINANCEIRO);
        if (rejeitado) {
            pedido.rejeitar(EtapaAprovacao.FINANCEIRO, "C", "Despesa particular", QUANDO.plusHours(1));
        } else {
            pedido.aprovar(EtapaAprovacao.FINANCEIRO, "C", QUANDO.plusHours(1), false);
        }
        return pedido;
    }

    private static Arguments invalida(String responsavel, PapelDeclarado papel, String vinculo,
                                     String gestor, EtapaAprovacao etapa, String explicacao) {
        return Arguments.of(decisao(responsavel, papel, vinculo, gestor, etapa, null), explicacao);
    }

    private static DecidirReembolso decisao(String responsavel, PapelDeclarado papel, String vinculo,
                                           String gestor, EtapaAprovacao etapa, String motivo) {
        return new DecidirReembolso(responsavel, papel, vinculo, gestor, etapa, motivo);
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

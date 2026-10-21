package com.orbe.reembolso.servico;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.orbe.reembolso.dominio.Categoria;
import com.orbe.reembolso.dominio.Reembolso;
import com.orbe.reembolso.dominio.RegraDeNegocioException;

/** Spec 002: CA10, CA11, CA12, CA13. */
@DataJpaTest
class PoliticaTetoRefeicaoTest {

    private static final LocalDate DIA = LocalDate.of(2026, 10, 15);
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 20, 10, 0);

    @Autowired
    ReembolsoRepository repositorio;

    PoliticaTetoRefeicao politica;

    @BeforeEach
    void preparar() {
        politica = new PoliticaTetoRefeicao(repositorio);
    }

    private Reembolso refeicao(String colaborador, String valor, String cnpj) {
        return repositorio.save(new Reembolso(colaborador, Categoria.REFEICAO, new BigDecimal(valor), DIA,
                cnpj, null, AGORA));
    }

    @Test
    void aceitaQuandoOTotalFicaExatamenteNoTeto() {
        refeicao("ana", "100.00", "11222333000181");

        assertThatCode(() -> politica.validar("ana", DIA, new BigDecimal("20.00"))).doesNotThrowAnyException();
    }

    @Test
    void recusaUmCentavoAcimaInformandoExistenteETotal() {
        refeicao("ana", "100.00", "11222333000181");

        assertThatThrownBy(() -> politica.validar("ana", DIA, new BigDecimal("20.01")))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessageContaining("R$ 100,00")
                .hasMessageContaining("R$ 120,01")
                .hasMessageContaining("15/10/2026");
    }

    @Test
    void pedidosRejeitadosNaoContam() {
        Reembolso rejeitado = refeicao("ana", "100.00", "11222333000181");
        rejeitado.rejeitar("bruno", "nota ilegível", AGORA);
        repositorio.save(rejeitado);

        assertThatCode(() -> politica.validar("ana", DIA, new BigDecimal("100.00"))).doesNotThrowAnyException();
    }

    @Test
    void oTetoEPorColaborador() {
        refeicao("bia", "100.00", "11222333000181");

        assertThatCode(() -> politica.validar("ana", DIA, new BigDecimal("100.00"))).doesNotThrowAnyException();
    }

    @Test
    void outrasCategoriasNaoContam() {
        repositorio.save(new Reembolso("ana", Categoria.TRANSPORTE, new BigDecimal("300.00"), DIA,
                "11222333000181", null, AGORA));

        assertThatCode(() -> politica.validar("ana", DIA, new BigDecimal("120.00"))).doesNotThrowAnyException();
    }
}

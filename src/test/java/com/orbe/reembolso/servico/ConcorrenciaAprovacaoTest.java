package com.orbe.reembolso.servico;

import static com.orbe.reembolso.suporte.RelogioDeTeste.HOJE;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;

import com.orbe.reembolso.dominio.Categoria;
import com.orbe.reembolso.dominio.PapelAprovador;
import com.orbe.reembolso.dominio.Reembolso;
import com.orbe.reembolso.dominio.StatusReembolso;
import com.orbe.reembolso.dominio.TransicaoDeStatusInvalidaException;
import com.orbe.reembolso.suporte.RelogioDeTeste;

/**
 * Spec 002, CA14 (R7). Bruno e Davi aprovam o mesmo pedido ao mesmo tempo.
 * Não importa quem chega primeiro: exatamente uma aprovação vale, a outra é recusada
 * como conflito, e o pedido registra um único aprovador.
 */
@SpringBootTest
@Import(RelogioDeTeste.class)
class ConcorrenciaAprovacaoTest {

    @Autowired
    ReembolsoService servico;

    @Autowired
    ReembolsoRepository repositorio;

    @AfterEach
    void limpar() {
        repositorio.deleteAll();
    }

    @RepeatedTest(5)
    void duasAprovacoesSimultaneasSoUmaVale() throws Exception {
        Reembolso pedido = servico.submeter(new NovoReembolso("ana", Categoria.OUTROS, new BigDecimal("300.00"),
                HOJE.minusDays(1), "11222333000181", "concorrência"));

        CountDownLatch largada = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        List<Future<String>> resultados = new ArrayList<>();
        for (String gestor : List.of("bruno", "davi")) {
            Callable<String> aprovacao = () -> {
                largada.await();
                try {
                    servico.aprovar(pedido.getId(), gestor, PapelAprovador.GESTOR);
                    return "ok:" + gestor;
                } catch (OptimisticLockingFailureException | TransicaoDeStatusInvalidaException e) {
                    return "conflito";
                }
            };
            resultados.add(pool.submit(aprovacao));
        }
        largada.countDown();

        List<String> saidas = new ArrayList<>();
        for (Future<String> f : resultados) {
            saidas.add(f.get(10, TimeUnit.SECONDS));
        }
        pool.shutdown();

        assertThat(saidas).filteredOn(s -> s.startsWith("ok:")).hasSize(1);
        assertThat(saidas).filteredOn("conflito"::equals).hasSize(1);

        String vencedor = saidas.stream().filter(s -> s.startsWith("ok:")).findFirst().orElseThrow().substring(3);
        Reembolso gravado = servico.buscar(pedido.getId());
        assertThat(gravado.getStatus()).isEqualTo(StatusReembolso.APROVADA);
        assertThat(gravado.getAprovadorGestor()).isEqualTo(vencedor);
        assertThat(gravado.getDecididoPor()).isEqualTo(vencedor);
    }
}

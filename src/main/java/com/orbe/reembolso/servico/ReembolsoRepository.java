package com.orbe.reembolso.servico;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.orbe.reembolso.dominio.Categoria;
import com.orbe.reembolso.dominio.Reembolso;
import com.orbe.reembolso.dominio.StatusReembolso;

public interface ReembolsoRepository extends JpaRepository<Reembolso, Long> {

    boolean existsByColaboradorAndCnpjFornecedorAndValorAndDataDespesa(
            String colaborador, String cnpjFornecedor, BigDecimal valor, LocalDate dataDespesa);

    List<Reembolso> findByColaboradorOrderByDataDespesaDescIdDesc(String colaborador);

    /** Soma do dia por colaborador e categoria, ignorando um status. Retorna null se não houver pedidos. */
    @Query("""
            select sum(r.valor) from Reembolso r
            where r.colaborador = :colaborador
              and r.categoria = :categoria
              and r.dataDespesa = :data
              and r.status <> :ignorado
            """)
    BigDecimal somarValorPorDia(@Param("colaborador") String colaborador,
                                @Param("categoria") Categoria categoria,
                                @Param("data") LocalDate data,
                                @Param("ignorado") StatusReembolso ignorado);
}

package com.orbe.reembolso.servico;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.orbe.reembolso.dominio.Reembolso;
import com.orbe.reembolso.dominio.StatusReembolso;

public interface ReembolsoRepository extends JpaRepository<Reembolso, Long> {

    @Override
    @EntityGraph(attributePaths = "historico")
    Optional<Reembolso> findById(Long id);

    boolean existsByColaboradorAndCnpjFornecedorAndValorAndDataDespesa(
            String colaborador, String cnpjFornecedor, BigDecimal valor, LocalDate dataDespesa);

    @Query("""
            select coalesce(sum(r.valor), 0) from Reembolso r
            where r.colaborador = :colaborador and r.dataDespesa = :dataDespesa
              and r.categoria = com.orbe.reembolso.dominio.Categoria.REFEICAO
              and r.submetidoEm >= :inicioV3 and r.status in :estados
            """)
    BigDecimal somarRefeicoesReservadas(
            @Param("colaborador") String colaborador, @Param("dataDespesa") LocalDate dataDespesa,
            @Param("inicioV3") LocalDateTime inicioV3, @Param("estados") Collection<StatusReembolso> estados);

    @EntityGraph(attributePaths = "historico")
    @Transactional(readOnly = true)
    List<Reembolso> findByColaboradorOrderByDataDespesaDescIdDesc(String colaborador);
}

package com.orbe.reembolso.servico;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.orbe.reembolso.dominio.Reembolso;

public interface ReembolsoRepository extends JpaRepository<Reembolso, Long> {

    @Override
    @EntityGraph(attributePaths = "historico")
    Optional<Reembolso> findById(Long id);

    boolean existsByColaboradorAndCnpjFornecedorAndValorAndDataDespesa(
            String colaborador, String cnpjFornecedor, BigDecimal valor, LocalDate dataDespesa);

    @EntityGraph(attributePaths = "historico")
    @Transactional(readOnly = true)
    List<Reembolso> findByColaboradorOrderByDataDespesaDescIdDesc(String colaborador);
}

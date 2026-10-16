package com.orbe.reembolso.servico;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.orbe.reembolso.dominio.Reembolso;

public interface ReembolsoRepository extends JpaRepository<Reembolso, Long> {

    boolean existsByColaboradorAndCnpjFornecedorAndValorAndDataDespesa(
            String colaborador, String cnpjFornecedor, BigDecimal valor, LocalDate dataDespesa);

    List<Reembolso> findByColaboradorOrderByDataDespesaDescIdDesc(String colaborador);
}

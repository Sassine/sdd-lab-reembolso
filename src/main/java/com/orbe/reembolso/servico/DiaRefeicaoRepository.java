package com.orbe.reembolso.servico;

import java.time.LocalDate;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.orbe.reembolso.dominio.ChaveDiaRefeicao;
import com.orbe.reembolso.dominio.DiaRefeicao;

public interface DiaRefeicaoRepository extends JpaRepository<DiaRefeicao, ChaveDiaRefeicao> {

    // INSERT explicito: save de um ID atribuido pode fazer merge em vez de disputar a PK.
    @Modifying
    @Transactional(propagation = Propagation.MANDATORY)
    @Query(value = """
            insert into dia_refeicao (colaborador, data_despesa)
            values (:colaborador, :dataDespesa)
            """, nativeQuery = true)
    void inserir(@Param("colaborador") String colaborador, @Param("dataDespesa") LocalDate dataDespesa);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Transactional(propagation = Propagation.MANDATORY)
    @Query("select dia from DiaRefeicao dia where dia.chave = :chave")
    Optional<DiaRefeicao> buscarParaAtualizacao(@Param("chave") ChaveDiaRefeicao chave);
}

package com.orbe.reembolso.servico;

import java.sql.SQLException;
import java.util.Objects;
import java.util.regex.Pattern;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.orbe.reembolso.dominio.ChaveDiaRefeicao;

@Component
public class ControleDeTetoDiario {

    private static final Pattern PK_H2 = Pattern.compile(
            "\\AUnique index or primary key violation: \"PUBLIC\\.PRIMARY_KEY_[0-9A-Z]+"
                    + " ON PUBLIC\\.DIA_REFEICAO\\(DATA_DESPESA, COLABORADOR\\) VALUES \\(");

    private final DiaRefeicaoRepository repositorio;
    private final TransactionTemplate independente;

    public ControleDeTetoDiario(DiaRefeicaoRepository repositorio, PlatformTransactionManager gerenciador) {
        this.repositorio = repositorio;
        this.independente = new TransactionTemplate(gerenciador);
        independente.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        independente.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
    }

    /**
     * Deve preceder qualquer escrita ou lock do comando externo. A ancora vazia
     * permanece confirmada mesmo se o comando for posteriormente recusado.
     */
    public void garantirExistencia(ChaveDiaRefeicao chave) {
        Objects.requireNonNull(chave, "chave");
        try {
            independente.executeWithoutResult(status -> {
                if (!repositorio.existsById(chave)) {
                    repositorio.inserir(chave.getColaborador(), chave.getDataDespesa());
                    repositorio.flush();
                }
            });
        } catch (DataIntegrityViolationException falha) {
            // execute ja reverteu a transacao: nunca consultar usando a transacao abortada.
            if (!colisaoDaChavePrimaria(falha)
                    || !Boolean.TRUE.equals(independente.execute(status -> repositorio.existsById(chave)))) {
                throw falha;
            }
        }
    }

    /**
     * Usar apos garantirExistencia, na transacao READ_COMMITTED do comando.
     * MANDATORY conserva o lock ate o commit/rollback do negocio, nunca em REQUIRES_NEW.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void bloquear(ChaveDiaRefeicao chave) {
        repositorio.buscarParaAtualizacao(Objects.requireNonNull(chave, "chave"))
                .orElseThrow(() -> new IllegalStateException("Ancora diaria ainda nao foi garantida"));
    }

    private boolean colisaoDaChavePrimaria(DataIntegrityViolationException falha) {
        // Formato comprovado no H2 do projeto, incluindo tabela e colunas da PK.
        // Ancora no inicio do diagnostico: dados/SQL citados depois nao podem
        // simular uma colisao. 23505 sozinho tambem identifica outras UNIQUEs.
        for (Throwable causa = falha; causa != null; causa = causa.getCause()) {
            if (causa instanceof SQLException sql
                    && "23505".equals(sql.getSQLState()) && sql.getErrorCode() == 23505
                    && sql.getMessage() != null
                    && PK_H2.matcher(sql.getMessage()).find()) {
                return true;
            }
        }
        return false;
    }
}

package com.orbe.reembolso.api;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.orbe.reembolso.dominio.RecursoNaoEncontradoException;
import com.orbe.reembolso.dominio.ReembolsoDuplicadoException;
import com.orbe.reembolso.dominio.RegraDeNegocioException;
import com.orbe.reembolso.dominio.TransicaoDeStatusInvalidaException;

/** Todo erro sai como Problem Details (RFC 9457). Constitution, art. 4. */
@RestControllerAdvice
public class TratadorDeErros {

    private static final HttpStatusCode UNPROCESSABLE = HttpStatusCode.valueOf(422);

    @ExceptionHandler(RegraDeNegocioException.class)
    ProblemDetail regraDeNegocio(RegraDeNegocioException e) {
        return problema(UNPROCESSABLE, "Regra de negócio violada", e.getMessage());
    }

    @ExceptionHandler(ReembolsoDuplicadoException.class)
    ProblemDetail duplicado(ReembolsoDuplicadoException e) {
        return problema(HttpStatus.CONFLICT, "Reembolso duplicado", e.getMessage());
    }

    @ExceptionHandler(TransicaoDeStatusInvalidaException.class)
    ProblemDetail transicao(TransicaoDeStatusInvalidaException e) {
        return problema(HttpStatus.CONFLICT, "Transição de status inválida", e.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ProblemDetail concorrencia(OptimisticLockingFailureException e) {
        return problema(HttpStatus.CONFLICT, "Conflito de concorrência",
                "O reembolso foi alterado por outra operação ao mesmo tempo. Consulte o estado atual e tente de novo.");
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ProblemDetail naoEncontrado(RecursoNaoEncontradoException e) {
        return problema(HttpStatus.NOT_FOUND, "Não encontrado", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail entradaInvalida(MethodArgumentNotValidException e) {
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Entrada inválida", "Um ou mais campos são inválidos.");
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));
        problema.setProperty("campos", campos);
        return problema;
    }

    private static ProblemDetail problema(HttpStatusCode status, String titulo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        return problema;
    }
}

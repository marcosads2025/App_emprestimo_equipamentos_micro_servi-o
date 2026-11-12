package br.com.emprestimo.msemprestimo.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tratamento centralizado de exceções. As respostas seguem o padrão
 * Problem Details (RFC 9457): { type, title, status, detail, instance, ... }.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ProblemDetail naoEncontrado(RecursoNaoEncontradoException ex) {
        return problema(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex.getMessage());
    }

    @ExceptionHandler(ConflitoException.class)
    ProblemDetail conflito(ConflitoException ex) {
        return problema(HttpStatus.CONFLICT, "Conflito", ex.getMessage());
    }

    /** Duas requisições simultâneas passaram pela checagem: a constraint UNIQUE do banco barra a segunda. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail integridade(DataIntegrityViolationException ex) {
        return problema(HttpStatus.CONFLICT, "Conflito", "Registro viola uma restrição de unicidade");
    }

    /** Duas devoluções simultâneas do mesmo empréstimo: o @Version deixa só a primeira gravar. */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ProblemDetail concorrencia(ObjectOptimisticLockingFailureException ex) {
        return problema(HttpStatus.CONFLICT, "Conflito", "O empréstimo foi alterado por outra requisição. Consulte e tente novamente.");
    }

    /** msusuario ou msequipamento fora do ar, sem instância no Eureka, timeout ou erro inesperado. */
    @ExceptionHandler(ServicoIndisponivelException.class)
    ProblemDetail indisponivel(ServicoIndisponivelException ex) {
        return problema(HttpStatus.SERVICE_UNAVAILABLE, "Serviço indisponível", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validacao(MethodArgumentNotValidException ex) {
        ProblemDetail problem = problema(HttpStatus.BAD_REQUEST, "Dados inválidos", "Um ou mais campos são inválidos");
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> campos.putIfAbsent(e.getField(), e.getDefaultMessage()));
        problem.setProperty("campos", campos);
        return problem;
    }

    /** JSON malformado ou UUID inválido no corpo. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ProblemDetail corpoInvalido(HttpMessageNotReadableException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Requisição inválida", "Corpo da requisição ausente ou malformado");
    }

    /** UUID inválido na URL, ex.: /api/v1/usuarios/abc */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ProblemDetail parametroInvalido(MethodArgumentTypeMismatchException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Parâmetro inválido",
                "Valor inválido para '" + ex.getName() + "': " + ex.getValue());
    }

    private ProblemDetail problema(HttpStatus status, String titulo, String detalhe) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detalhe);
        problem.setTitle(titulo);
        return problem;
    }
}

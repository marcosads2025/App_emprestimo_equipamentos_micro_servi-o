package br.com.emprestimo.msemprestimo.exception;

/** Vira HTTP 503: falha de comunicação com outro microsserviço. */
public class ServicoIndisponivelException extends RuntimeException {

    public ServicoIndisponivelException(String message, Throwable cause) {
        super(message, cause);
    }
}

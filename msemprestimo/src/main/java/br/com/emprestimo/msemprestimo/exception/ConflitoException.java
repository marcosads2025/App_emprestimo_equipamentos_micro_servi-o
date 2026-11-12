package br.com.emprestimo.msemprestimo.exception;

/** Vira HTTP 409: violação de unicidade ou estado incompatível. */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String message) {
        super(message);
    }
}

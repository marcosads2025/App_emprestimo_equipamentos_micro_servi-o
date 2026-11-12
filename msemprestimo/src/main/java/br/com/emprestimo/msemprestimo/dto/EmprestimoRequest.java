package br.com.emprestimo.msemprestimo.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EmprestimoRequest(
        @NotNull(message = "usuarioId é obrigatório") UUID usuarioId,
        @NotNull(message = "equipamentoId é obrigatório") UUID equipamentoId) {
}

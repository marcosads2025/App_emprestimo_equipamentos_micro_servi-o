package br.com.emprestimo.msemprestimo.dto;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para registro de devolução de equipamento.
 * Inclui a condição do equipamento ao ser devolvido (BOM ou AVARIADO).
 */
public record DevolucaoRequest(
        @NotNull(message = "condicao é obrigatória") String condicao) {
}

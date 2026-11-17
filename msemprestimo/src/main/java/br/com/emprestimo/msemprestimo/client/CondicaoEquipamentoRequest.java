package br.com.emprestimo.msemprestimo.client;

import jakarta.validation.constraints.NotNull;

/**
 * DTO para atualização da condição de um equipamento.
 * Aceita valores: BOM ou AVARIADO
 */
public record CondicaoEquipamentoRequest(
        @NotNull(message = "condicao é obrigatória") String condicao) {
}

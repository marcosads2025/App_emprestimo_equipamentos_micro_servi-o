package br.com.msequipamento.msequipamento.dto;

import br.com.msequipamento.msequipamento.model.CondicaoEquipamento;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para atualização da condição de um equipamento.
 * Aceita valores: BOM ou AVARIADO
 */
public record CondicaoEquipamentoRequest(
        @NotNull(message = "condicao é obrigatória") CondicaoEquipamento condicao) {
}

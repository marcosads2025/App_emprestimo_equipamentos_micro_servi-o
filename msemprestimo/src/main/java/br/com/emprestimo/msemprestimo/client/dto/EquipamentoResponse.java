package br.com.emprestimo.msemprestimo.client.dto;

import java.util.UUID;

public record EquipamentoResponse(
        UUID id,
        String nome,
        String tipo,
        String patrimonio,
        Boolean disponivel,
        UUID emprestimoReservaId) {
}

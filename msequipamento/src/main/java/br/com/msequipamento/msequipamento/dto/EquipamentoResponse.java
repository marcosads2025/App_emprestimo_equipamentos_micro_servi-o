package br.com.msequipamento.msequipamento.dto;

import br.com.msequipamento.msequipamento.model.Equipamento;

import java.util.UUID;

public record EquipamentoResponse(UUID id, String nome, String tipo, String patrimonio, Boolean disponivel) {

    public static EquipamentoResponse de(Equipamento equipamento) {
        return new EquipamentoResponse(equipamento.getId(), equipamento.getNome(), equipamento.getTipo(),
                equipamento.getPatrimonio(), equipamento.getDisponivel());
    }
}

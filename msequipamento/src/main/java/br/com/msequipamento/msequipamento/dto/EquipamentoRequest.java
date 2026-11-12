package br.com.msequipamento.msequipamento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EquipamentoRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Size(max = 60) String tipo,
        @NotBlank @Size(max = 30) String patrimonio) {
}

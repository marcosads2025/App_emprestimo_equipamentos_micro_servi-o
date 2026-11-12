package br.com.emprestimo.msemprestimo.dto;

import br.com.emprestimo.msemprestimo.model.Emprestimo;
import br.com.emprestimo.msemprestimo.model.StatusEmprestimo;

import java.time.LocalDateTime;
import java.util.UUID;

public record EmprestimoResponse(
        UUID id,
        UUID usuarioId,
        UUID equipamentoId,
        LocalDateTime dataEmprestimo,
        LocalDateTime dataDevolucao,
        StatusEmprestimo status) {

    public static EmprestimoResponse de(Emprestimo e) {
        return new EmprestimoResponse(e.getId(), e.getUsuarioId(), e.getEquipamentoId(),
                e.getDataEmprestimo(), e.getDataDevolucao(), e.getStatus());
    }
}

package br.com.emprestimo.msemprestimo.repository;

import br.com.emprestimo.msemprestimo.model.Emprestimo;
import br.com.emprestimo.msemprestimo.model.StatusEmprestimo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, UUID> {

    List<Emprestimo> findByUsuarioIdOrderByDataEmprestimoDesc(UUID usuarioId);

    boolean existsByEquipamentoIdAndStatus(UUID equipamentoId, StatusEmprestimo status);
}

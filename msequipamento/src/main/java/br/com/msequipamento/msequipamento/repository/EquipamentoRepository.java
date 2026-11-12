package br.com.msequipamento.msequipamento.repository;

import br.com.msequipamento.msequipamento.model.Equipamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EquipamentoRepository extends JpaRepository<Equipamento, UUID> {

    boolean existsByPatrimonio(String patrimonio);

    List<Equipamento> findByDisponivelTrue();

    @Modifying
    @Query("""
            UPDATE Equipamento e
               SET e.disponivel = false, e.emprestimoId = :emprestimoId
             WHERE e.id = :id AND e.disponivel = true
            """)
    int reservar(@Param("id") UUID id, @Param("emprestimoId") UUID emprestimoId);

    @Modifying
    @Query("""
            UPDATE Equipamento e
               SET e.disponivel = true, e.emprestimoId = null
             WHERE e.id = :id AND e.emprestimoId = :emprestimoId
            """)
    int liberar(@Param("id") UUID id, @Param("emprestimoId") UUID emprestimoId);
}

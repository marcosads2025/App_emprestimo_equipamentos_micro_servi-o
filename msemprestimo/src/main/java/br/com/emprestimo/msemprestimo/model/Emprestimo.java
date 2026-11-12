package br.com.emprestimo.msemprestimo.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Guarda só os IDs externos de usuário e equipamento — sem @ManyToOne,
 * porque esses dados pertencem a outros microsserviços e a outros bancos.
 *
 * Sem setters: o estado só muda pelos métodos de negócio (devolver, cancelar).
 */
@Entity
@Table(name = "tb_emprestimos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // exigido pelo JPA
public class Emprestimo {

    /**
     * Atribuído pelo próprio serviço ANTES de chamar o msequipamento,
     * porque é a chave da reserva idempotente (PUT .../reservas/{emprestimoId}).
     */
    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36, nullable = false, updatable = false)
    private UUID id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "usuario_id", length = 36, nullable = false, updatable = false)
    private UUID usuarioId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "equipamento_id", length = 36, nullable = false, updatable = false)
    private UUID equipamentoId;

    @Column(name = "data_emprestimo", nullable = false, updatable = false)
    private LocalDateTime dataEmprestimo;

    @Column(name = "data_devolucao")
    private LocalDateTime dataDevolucao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusEmprestimo status;

    /**
     * Controle de concorrência otimista: se duas devoluções do mesmo empréstimo
     * chegarem juntas, só a primeira grava; a segunda recebe 409.
     * Bônus: com versao == null o Spring Data sabe que a entidade é nova
     * e faz INSERT direto, mesmo com o id já preenchido.
     */
    @Version
    private Long versao;

    public static Emprestimo novoAtivo(UUID id, UUID usuarioId, UUID equipamentoId, LocalDateTime agora) {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.id = id;
        emprestimo.usuarioId = usuarioId;
        emprestimo.equipamentoId = equipamentoId;
        emprestimo.dataEmprestimo = agora;
        emprestimo.status = StatusEmprestimo.ATIVO;
        return emprestimo;
    }

    public void devolver(LocalDateTime agora) {
        this.status = StatusEmprestimo.DEVOLVIDO;
        this.dataDevolucao = agora;
    }

    public void cancelar() {
        this.status = StatusEmprestimo.CANCELADO;
    }

    public boolean isAtivo() {
        return status == StatusEmprestimo.ATIVO;
    }
}

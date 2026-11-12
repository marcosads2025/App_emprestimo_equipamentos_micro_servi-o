package br.com.msequipamento.msequipamento.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "tb_equipamentos")
@Getter
@Setter
@NoArgsConstructor
public class Equipamento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 60)
    private String tipo;

    @Column(nullable = false, unique = true, length = 30)
    private String patrimonio;

    @Column(nullable = false)
    private Boolean disponivel;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "emprestimo_id", length = 36)
    private UUID emprestimoId;
}

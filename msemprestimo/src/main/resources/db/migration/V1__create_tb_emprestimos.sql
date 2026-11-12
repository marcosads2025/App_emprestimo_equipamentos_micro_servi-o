-- Apenas os IDs externos de usuário e equipamento: sem chave estrangeira para outros bancos.
CREATE TABLE tb_emprestimos (
     id                  CHAR(36)        NOT NULL,
     usuario_id          CHAR(36)        NOT NULL,
     equipamento_id      CHAR(36)        NOT NULL,
     data_emprestimo     DATE            NOT NULL,
     data_devolucao      DATE            NULL,
     status              VARCHAR(20)     NOT NULL,
     versao              BIGINT          NOT NULL DEFAULT 0,

    -- Coluna calculada: só preenchida quando o empréstimo está ATIVO
    -- Índice UNIQUE ignora valores NULL → garante 1 empréstimo ativo por equipamento
     equipamento_ativo   CHAR(36) GENERATED ALWAYS AS (
     CASE WHEN status = 'ATIVO' THEN equipamento_id ELSE NULL END
     ) STORED,

   CONSTRAINT pk_emprestimos PRIMARY KEY (id),
   CONSTRAINT ck_emprestimos_status CHECK (status IN ('ATIVO', 'DEVOLVIDO', 'CANCELADO')),
   CONSTRAINT uk_emprestimos_equipamento_ativo UNIQUE (equipamento_ativo)
   );

-- Índice separado (sintaxe correta do PostgreSQL)
  CREATE INDEX idx_emprestimos_usuario
    ON tb_emprestimos (usuario_id);

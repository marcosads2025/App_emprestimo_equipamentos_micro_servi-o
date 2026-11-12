CREATE TABLE tb_equipamentos (
        id            CHAR(36)     NOT NULL PRIMARY KEY,
        nome          VARCHAR(120) NOT NULL,
        tipo          VARCHAR(60)  NOT NULL,
        patrimonio    VARCHAR(30)  NOT NULL,
        disponivel    BOOLEAN      NOT NULL,
        emprestimo_id CHAR(36),
        CONSTRAINT uk_equipamentos_patrimonio UNIQUE (patrimonio)
);
CREATE TABLE tb_usuarios (
     id        CHAR(36)     NOT NULL PRIMARY KEY,
     nome      VARCHAR(120) NOT NULL,
     email     VARCHAR(160) NOT NULL,
     matricula VARCHAR(30)  NOT NULL,
     CONSTRAINT uk_usuarios_email UNIQUE (email),
     CONSTRAINT uk_usuarios_matricula UNIQUE (matricula)
);
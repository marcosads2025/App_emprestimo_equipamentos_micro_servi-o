# Microsserviço de Usuário

## Descrição
O **msusuario** é o microsserviço responsável pelo gerenciamento de usuários do Sistema de Empréstimo de Equipamentos. Ele gerencia o cadastro, consulta e autenticação de usuários que podem solicitar empréstimos de equipamentos.

## Funcionalidades
- **Cadastro de Usuários**: Criação e registro de novos usuários no sistema
- **Consulta de Usuários**: Busca de usuários por ID ou listagem completa
- **Validação de Dados**: Validação de informações do usuário usando Bean Validation
- **Gerenciamento de Perfil**: Mantém informações como nome, email, matrícula

## Tecnologias
- **Spring Boot 3.5.6**
- **Spring Data JPA**
- **Spring Web**
- **Spring Cloud Netflix Eureka Client**
- **Flyway** (migrações de banco de dados)
- **PostgreSQL**
- **Lombok**
- **Bean Validation**
- **Java 17**

## Como Funciona
1. O serviço se registra no Eureka Server ao iniciar
2. Mantém um banco de dados PostgreSQL com a tabela de usuários
3. Fornece endpoints REST para CRUD de usuários
4. O serviço `msemprestimo` chama este serviço via OpenFeign para validar usuários antes de permitir empréstimos

## Endpoints Principais

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/v1/usuarios` | Criar novo usuário |
| `GET` | `/api/v1/usuarios/{id}` | Buscar usuário por ID |
| `GET` | `/api/v1/usuarios` | Listar todos os usuários |
| `PUT` | `/api/v1/usuarios/{id}` | Atualizar usuário |
| `DELETE` | `/api/v1/usuarios/{id}` | Deletar usuário |

## Contrato com msemprestimo
O serviço `msemprestimo` depende do seguinte comportamento:

- **GET /api/v1/usuarios/{id}**: Retorna `200` com dados do usuário (id, nome, email, matricula) ou `404` se o usuário não existir

É importante retornar `404` quando o usuário não existe, pois o `msemprestimo` usa essa resposta para impedir empréstimos de usuários inválidos.

## Banco de Dados
- **Banco**: PostgreSQL
- **Tabela**: `tb_usuarios` (criada via Flyway)
- **Campos principais**: id, nome, email, matricula

## Configuração
- Registra-se automaticamente no Eureka Server
- Configurações de banco em `application.properties`
- Migrações de banco em `src/main/resources/db/migration/`

## Porta
O serviço roda na porta configurada em `application.properties`.

## Execução
```bash
mvn clean install
mvn spring-boot:run
```

## Dependências
- Requer o PostgreSQL rodando
- Requer o Discovery Server (Eureka) rodando para registro

## Ordem de Inicialização
Na arquitetura completa, a ordem recomendada é:
1. discovery-server (Eureka)
2. msusuario
3. msequipamento
4. msemprestimo
5. apigateway

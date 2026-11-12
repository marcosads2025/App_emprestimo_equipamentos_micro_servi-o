# Microsserviço de Equipamento

## Descrição
O **msequipamento** é o microsserviço responsável pelo gerenciamento de equipamentos do Sistema de Empréstimo de Equipamentos. Ele gerencia o cadastro, consulta e disponibilidade de equipamentos, além de controlar as reservas feitas pelo serviço de empréstimos.

## Funcionalidades
- **Cadastro de Equipamentos**: Criação e registro de novos equipamentos no sistema
- **Consulta de Equipamentos**: Busca de equipamentos por ID ou listagem completa
- **Controle de Disponibilidade**: Gerencia o status de disponibilidade de cada equipamento
- **Reserva de Equipamentos**: Permite reservar equipamentos para empréstimos específicos
- **Liberação de Equipamentos**: Libera equipamentos quando o empréstimo é devolvido ou cancelado
- **Validação**: Validação de dados usando Bean Validation

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
2. Mantém um banco de dados PostgreSQL com a tabela de equipamentos
3. Fornece endpoints REST para CRUD de equipamentos
4. O serviço `msemprestimo` chama este serviço via OpenFeign para:
   - Consultar disponibilidade de equipamentos
   - Reservar equipamentos para empréstimos
   - Liberar equipamentos após devolução

## Endpoints Principais

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/v1/equipamentos` | Criar novo equipamento |
| `GET` | `/api/v1/equipamentos/{id}` | Buscar equipamento por ID |
| `GET` | `/api/v1/equipamentos` | Listar todos os equipamentos |
| `PUT` | `/api/v1/equipamentos/{id}` | Atualizar equipamento |
| `DELETE` | `/api/v1/equipamentos/{id}` | Deletar equipamento |
| `PUT` | `/api/v1/equipamentos/{id}/reservas/{emprestimoId}` | Reservar equipamento |
| `DELETE` | `/api/v1/equipamentos/{id}/reservas/{emprestimoId}` | Liberar reserva |

## Contrato com msemprestimo
O serviço `msemprestimo` depende dos seguintes comportamentos:

- **GET /api/v1/equipamentos/{id}**: Retorna `200` com dados do equipamento (incluindo campo `disponivel`) ou `404` se não existir
- **PUT /api/v1/equipamentos/{id}/reservas/{emprestimoId}**: Reserva o equipamento para um empréstimo. Retorna `200` se reservado com sucesso, `404` se equipamento não existe, ou `409` se equipamento já está ocupado
- **DELETE /api/v1/equipamentos/{id}/reservas/{emprestimoId}**: Libera a reserva do equipamento. Retorna `204` (inclusive se já liberado) ou `404` se equipamento não existe

## Banco de Dados
- **Banco**: PostgreSQL
- **Tabela**: `tb_equipamentos` (criada via Flyway)
- **Campos principais**: id, nome, patrimonio, tipo, disponivel, emprestimoId

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

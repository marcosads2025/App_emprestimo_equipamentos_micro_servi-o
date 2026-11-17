# Etapa 2 — OpenFeign + Circuit Breaker

## Resumo das Alterações

Esta implementação adiciona funcionalidade de rastreamento de condição de equipamentos e integração com Circuit Breaker para garantir resiliência na comunicação entre microsserviços.

### Alterações no msequipamento

1. **Enum CondicaoEquipamento** (`model/CondicaoEquipamento.java`)
   - Valores: `BOM` e `AVARIADO`
   - Representa o estado físico do equipamento

2. **Entidade Equipamento** (`model/Equipamento.java`)
   - Adicionado campo `condicao` com valor padrão `BOM`
   - Armazenado como VARCHAR(20) no banco

3. **Migração Flyway** (`V2__adicionar_coluna_condicao.sql`)
   - Adiciona coluna `condicao` na tabela `tb_equipamentos`
   - Valor padrão 'BOM' para registros existentes

4. **DTO CondicaoEquipamentoRequest** (`dto/CondicaoEquipamentoRequest.java`)
   - DTO para atualização de condição via API

5. **DTO EquipamentoResponse** (`dto/EquipamentoResponse.java`)
   - Atualizado para incluir campo `condicao` nas respostas

6. **Service** (`service/EquipamentoService.java`)
   - Método `alterarCondicao(UUID id, CondicaoEquipamento condicao)`
   - Atualiza a condição do equipamento com validação de existência

7. **Controller** (`controller/EquipamentoController.java`)
   - Endpoint `PUT /api/v1/equipamentos/{id}/condicao`
   - Retorna 200 OK com equipamento atualizado
   - Retorna 404 Not Found se equipamento não existe

### Alterações no msemprestimo

1. **Dependências** (`pom.xml`)
   - `resilience4j-spring-boot3`
   - `resilience4j-circuitbreaker`
   - `resilience4j-feign`

2. **DTO CondicaoEquipamentoRequest** (`client/CondicaoEquipamentoRequest.java`)
   - DTO para enviar condição ao msequipamento

3. **DTO DevolucaoRequest** (`dto/DevolucaoRequest.java`)
   - DTO para endpoint de devolução com campo `condicao`

4. **Feign Client** (`client/EquipamentoClient.java`)
   - Método `alterarCondicao` com anotação `@CircuitBreaker`
   - Fallback `fallbackAlterarCondicao` que lança `ServicoIndisponivelException`
   - Configuração: `equipamentoCondicao`

5. **Configuração Circuit Breaker** (`application.yml`)
   ```yaml
   spring.cloud.circuitbreaker:
     configs:
       default:
         sliding-window-size: 5
         minimum-number-of-calls: 3
         failure-rate-threshold: 50%
         wait-duration-in-open-state: 30s
         permitted-number-of-calls-in-half-open-state: 2
         ignore-exceptions:
           - feign.FeignException$NotFound
     instances:
       equipamentoCondicao:
         base-config: default
   ```

6. **Service** (`service/EmprestimoService.java`)
   - Método `devolver(UUID id, String condicao)` atualizado
   - Chama `alterarCondicaoEquipamento` antes de liberar o equipamento
   - Se a alteração de condição falhar, a devolução não é concluída
   - Método privado `alterarCondicaoEquipamento` com tratamento de exceções

7. **Controller** (`controller/EmprestimoController.java`)
   - Endpoint `PATCH /api/v1/emprestimos/{id}/devolucao` atualizado
   - Agora aceita corpo JSON com campo `condicao`

8. **EquipamentoResponse** (`client/dto/EquipamentoResponse.java`)
   - Adicionado campo `condicao` para compatibilidade com msequipamento

## Exemplos de Requisição JSON (Postman)

### 1. Alterar condição de equipamento (msequipamento)

**Endpoint:** `PUT http://localhost:8082/api/v1/equipamentos/{id}/condicao`

**Body:**
```json
{
  "condicao": "BOM"
}
```

Ou para equipamento avariado:
```json
{
  "condicao": "AVARIADO"
}
```

**Respostas:**
- `200 OK`: Equipamento atualizado com sucesso
- `404 Not Found`: Equipamento não encontrado
- `400 Bad Request`: Campo condicao inválido ou ausente

---

### 2. Registrar devolução com condição (msemprestimo)

**Endpoint:** `PATCH http://localhost:8083/api/v1/emprestimos/{id}/devolucao`

**Body:**
```json
{
  "condicao": "BOM"
}
```

Ou para equipamento devolvido avariado:
```json
{
  "condicao": "AVARIADO"
}
```

**Respostas:**
- `200 OK`: Devolução registrada com sucesso, condição do equipamento atualizada
- `404 Not Found`: Empréstimo não encontrado
- `409 Conflict`: Empréstimo já devolvido ou cancelado
- `503 Service Unavailable`: msequipamento indisponível, devolução não concluída

---

## Cenários de Teste

| Cenário | Resultado esperado |
|---------|-------------------|
| Devolução com condição BOM | Condição atualizada + empréstimo DEVOLVIDO ✅ |
| Devolução com condição AVARIADO | Condição atualizada + empréstimo DEVOLVIDO ✅ |
| Equipamento não existe | Retorna 404; circuito NÃO abre ❌ |
| msequipamento desligado | Retorna 503; devolução NÃO é concluída ⚠️ |
| 3 de 5 chamadas falham | Circuito vai para OPEN; chamadas subsequentes bloqueadas ⚡ |
| Após 30s de recuperação | Circuito vai para HALF_OPEN; volta para CLOSED com sucesso ✅ |

## Comportamento do Circuit Breaker

- **Estados:** CLOSED → OPEN → HALF_OPEN → CLOSED
- **Ignora erros 404:** Erros de negócio não abrem o circuito
- **Fallback:** Retorna 503 quando o serviço está indisponível
- **Configuração:**
  - Janela de 5 chamadas
  - Mínimo de 3 chamadas para avaliação
  - 50% de falhas para abrir o circuito
  - 30 segundos em estado OPEN
  - 2 chamadas permitidas em HALF_OPEN

## Regras Importantes

- A devolução só é concluída se a atualização da condição for bem-sucedida
- Erros de negócio (404) não contabilizam como falhas técnicas
- O fallback lança `ServicoIndisponivelException` que é convertida em HTTP 503
- O Circuit Breaker é aplicado APENAS na chamada de alteração de condição

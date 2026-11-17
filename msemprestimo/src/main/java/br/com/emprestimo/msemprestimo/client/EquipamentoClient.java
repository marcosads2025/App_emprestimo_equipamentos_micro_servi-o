package br.com.emprestimo.msemprestimo.client;

import br.com.emprestimo.msemprestimo.client.dto.EquipamentoResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "msequipamento", path = "/api/v1/equipamentos")
public interface EquipamentoClient {

    @GetMapping("/{id}")
    EquipamentoResponse buscarPorId(@PathVariable("id") UUID id);

    /** Reserva idempotente: 200 reservado · 404 não existe · 409 ocupado por outro empréstimo. */
    @PutMapping("/{id}/reservas/{emprestimoId}")
    EquipamentoResponse reservar(@PathVariable("id") UUID id, @PathVariable("emprestimoId") UUID emprestimoId);

    /** Liberação idempotente: 204 mesmo se já estiver liberado · 404 não existe. */
    @DeleteMapping("/{id}/reservas/{emprestimoId}")
    void liberar(@PathVariable("id") UUID id, @PathVariable("emprestimoId") UUID emprestimoId);

    /**
     * RF11 — Altera a condição do equipamento (BOM ou AVARIADO).
     * Aplica Circuit Breaker com fallback para indisponibilidade.
     * Erros de negócio (404) não abrem o circuito, apenas falhas técnicas.
     */
    @PutMapping("/{id}/condicao")
    @CircuitBreaker(name = "equipamentoCondicao", fallbackMethod = "fallbackAlterarCondicao")
    EquipamentoResponse alterarCondicao(@PathVariable("id") UUID id, @RequestBody CondicaoEquipamentoRequest request);

    /**
     * Fallback para alteração de condição quando o serviço está indisponível.
     * Lança exceção que será convertida em HTTP 503 SERVICE_UNAVAILABLE.
     */
    default EquipamentoResponse fallbackAlterarCondicao(UUID id, CondicaoEquipamentoRequest request, Throwable throwable) {
        throw new br.com.emprestimo.msemprestimo.exception.ServicoIndisponivelException(
                "Serviço msequipamento indisponível para alterar condição do equipamento", throwable);
    }
}

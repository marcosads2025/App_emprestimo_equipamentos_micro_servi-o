package br.com.emprestimo.msemprestimo.client;

import br.com.emprestimo.msemprestimo.client.dto.EquipamentoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

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
}

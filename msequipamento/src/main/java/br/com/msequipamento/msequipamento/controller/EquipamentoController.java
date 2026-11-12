package br.com.msequipamento.msequipamento.controller;

import br.com.msequipamento.msequipamento.dto.EquipamentoRequest;
import br.com.msequipamento.msequipamento.dto.EquipamentoResponse;
import br.com.msequipamento.msequipamento.service.EquipamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/equipamentos")
@RequiredArgsConstructor
public class EquipamentoController {

    private final EquipamentoService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquipamentoResponse cadastrar(@RequestBody @Valid EquipamentoRequest request) {
        return service.cadastrar(request);
    }

    @GetMapping("/disponiveis")
    public List<EquipamentoResponse> listarDisponiveis() {
        return service.listarDisponiveis();
    }

    @GetMapping("/{id}")
    public EquipamentoResponse buscarPorId(@PathVariable UUID id) {
        return service.buscarPorId(id);
    }

    @PutMapping("/{id}/reservas/{emprestimoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reservar(@PathVariable UUID id, @PathVariable UUID emprestimoId) {
        service.reservar(id, emprestimoId);
    }

    @DeleteMapping("/{id}/reservas/{emprestimoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void liberar(@PathVariable UUID id, @PathVariable UUID emprestimoId) {
        service.liberar(id, emprestimoId);
    }
}

package br.com.emprestimo.msemprestimo.controller;

import br.com.emprestimo.msemprestimo.dto.EmprestimoRequest;
import br.com.emprestimo.msemprestimo.dto.EmprestimoResponse;
import br.com.emprestimo.msemprestimo.service.EmprestimoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/emprestimos")
@RequiredArgsConstructor
public class EmprestimoController {

    private final EmprestimoService service;

    /** RF03 — solicitar empréstimo. 201 com o ID · 400 · 404 usuário/equipamento · 409 ocupado · 503. */
    @PostMapping
    public ResponseEntity<EmprestimoResponse> solicitar(@RequestBody @Valid EmprestimoRequest request) {
        EmprestimoResponse criado = service.solicitar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(location).body(criado);
    }

    /** RF06 — consultar por ID. */
    @GetMapping("/{id}")
    public EmprestimoResponse buscarPorId(@PathVariable UUID id) {
        return service.buscarPorId(id);
    }

    /** RF06 — consultar por usuário (mais recentes primeiro). Lista vazia se não houver nenhum. */
    @GetMapping("/usuarios/{usuarioId}")
    public List<EmprestimoResponse> listarPorUsuario(@PathVariable UUID usuarioId) {
        return service.listarPorUsuario(usuarioId);
    }

    /** RF07 — registrar devolução. 200 · 404 · 409 já devolvido/cancelado · 503. */
    @PatchMapping("/{id}/devolucao")
    public EmprestimoResponse devolver(@PathVariable UUID id) {
        return service.devolver(id);
    }

    /** Extra: cancelar empréstimo ativo. */
    @PatchMapping("/{id}/cancelamento")
    public EmprestimoResponse cancelar(@PathVariable UUID id) {
        return service.cancelar(id);
    }
}

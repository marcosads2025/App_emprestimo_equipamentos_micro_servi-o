package br.com.msusuario.msusuario;

import br.com.msusuario.msusuario.dto.UsuarioRequest;
import br.com.msusuario.msusuario.dto.UsuarioResponse;
import br.com.msusuario.msusuario.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse criar(@RequestBody @Valid UsuarioRequest request) {
        return service.criar(request);
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscarPorId(@PathVariable UUID id) {
        return service.buscarPorId(id);
    }
}

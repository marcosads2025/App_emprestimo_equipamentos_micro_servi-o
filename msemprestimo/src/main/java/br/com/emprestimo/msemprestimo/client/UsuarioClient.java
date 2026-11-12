package br.com.emprestimo.msemprestimo.client;

import br.com.emprestimo.msemprestimo.client.dto.UsuarioResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * "msusuario" é o nome registrado no Eureka — sem URL fixa.
 * O Spring Cloud LoadBalancer escolhe uma das instâncias disponíveis a cada chamada.
 */
@FeignClient(name = "msusuario", path = "/api/v1/usuarios")
public interface UsuarioClient {

    @GetMapping("/{id}")
    UsuarioResponse buscarPorId(@PathVariable("id") UUID id);
}

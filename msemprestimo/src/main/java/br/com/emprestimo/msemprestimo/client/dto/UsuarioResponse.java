package br.com.emprestimo.msemprestimo.client.dto;

import java.util.UUID;

/**
 * "Visão" do usuário que este serviço precisa. Não é uma cópia do código do msusuario:
 * cada serviço declara só os campos que usa (campos extras na resposta são ignorados).
 */
public record UsuarioResponse(UUID id, String nome, String email, String matricula) {
}

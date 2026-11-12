package br.com.msusuario.msusuario.dto;

import br.com.msusuario.msusuario.model.Usuario;

import java.util.UUID;

public record UsuarioResponse(UUID id, String nome, String email, String matricula) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getMatricula());
    }
}

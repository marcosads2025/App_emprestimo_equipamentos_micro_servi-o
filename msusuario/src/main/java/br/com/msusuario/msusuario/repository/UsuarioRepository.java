package br.com.msusuario.msusuario.repository;

import br.com.msusuario.msusuario.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    boolean existsByEmail(String email);

    boolean existsByMatricula(String matricula);
}

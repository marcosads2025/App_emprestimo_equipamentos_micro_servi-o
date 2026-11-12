package br.com.msusuario.msusuario.service;

import br.com.msusuario.msusuario.dto.UsuarioRequest;
import br.com.msusuario.msusuario.dto.UsuarioResponse;
import br.com.msusuario.msusuario.exception.ConflitoException;
import br.com.msusuario.msusuario.exception.RecursoNaoEncontradoException;
import br.com.msusuario.msusuario.model.Usuario;
import br.com.msusuario.msusuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository repository;

    @Transactional
    public UsuarioResponse criar(UsuarioRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new ConflitoException("E-mail já cadastrado");
        }
        if (repository.existsByMatricula(request.matricula())) {
            throw new ConflitoException("Matrícula já cadastrada");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setMatricula(request.matricula());

        return UsuarioResponse.de(repository.save(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(UUID id) {
        return repository.findById(id)
                .map(UsuarioResponse::de)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }
}

package br.com.msequipamento.msequipamento.service;

import br.com.msequipamento.msequipamento.model.Equipamento;
import br.com.msequipamento.msequipamento.dto.EquipamentoRequest;
import br.com.msequipamento.msequipamento.dto.EquipamentoResponse;
import br.com.msequipamento.msequipamento.exception.ConflitoException;
import br.com.msequipamento.msequipamento.exception.RecursoNaoEncontradoException;
import br.com.msequipamento.msequipamento.repository.EquipamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EquipamentoService {

    private final EquipamentoRepository repository;

    @Transactional
    public EquipamentoResponse cadastrar(EquipamentoRequest request) {
        if (repository.existsByPatrimonio(request.patrimonio())) {
            throw new ConflitoException("Patrimônio já cadastrado");
        }

        Equipamento equipamento = new Equipamento();
        equipamento.setNome(request.nome());
        equipamento.setTipo(request.tipo());
        equipamento.setPatrimonio(request.patrimonio());
        equipamento.setDisponivel(true);

        return EquipamentoResponse.de(repository.save(equipamento));
    }

    @Transactional(readOnly = true)
    public EquipamentoResponse buscarPorId(UUID id) {
        return EquipamentoResponse.de(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<EquipamentoResponse> listarDisponiveis() {
        return repository.findByDisponivelTrue().stream()
                .map(EquipamentoResponse::de)
                .toList();
    }

    @Transactional
    public void reservar(UUID id, UUID emprestimoId) {
        if (repository.reservar(id, emprestimoId) == 1) {
            return;
        }

        Equipamento equipamento = buscarEntidade(id);
        if (!emprestimoId.equals(equipamento.getEmprestimoId())) {
            throw new ConflitoException("Equipamento indisponível");
        }
    }

    @Transactional
    public void liberar(UUID id, UUID emprestimoId) {
        if (repository.liberar(id, emprestimoId) == 0) {
            buscarEntidade(id);
        }
    }

    private Equipamento buscarEntidade(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Equipamento não encontrado"));
    }
}

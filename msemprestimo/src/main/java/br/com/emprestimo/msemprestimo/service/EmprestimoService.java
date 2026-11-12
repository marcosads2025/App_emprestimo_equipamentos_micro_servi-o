package br.com.emprestimo.msemprestimo.service;

import br.com.emprestimo.msemprestimo.client.EquipamentoClient;
import br.com.emprestimo.msemprestimo.client.UsuarioClient;
import br.com.emprestimo.msemprestimo.client.dto.EquipamentoResponse;
import br.com.emprestimo.msemprestimo.dto.EmprestimoRequest;
import br.com.emprestimo.msemprestimo.dto.EmprestimoResponse;
import br.com.emprestimo.msemprestimo.exception.ConflitoException;
import br.com.emprestimo.msemprestimo.exception.RecursoNaoEncontradoException;
import br.com.emprestimo.msemprestimo.exception.ServicoIndisponivelException;
import br.com.emprestimo.msemprestimo.model.Emprestimo;
import br.com.emprestimo.msemprestimo.model.StatusEmprestimo;
import br.com.emprestimo.msemprestimo.repository.EmprestimoRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Coordena o empréstimo entre três bancos independentes.
 *
 * Os métodos que chamam outros serviços NÃO são @Transactional de propósito:
 * manter uma transação do banco aberta durante chamadas HTTP prende conexões e,
 * mesmo assim, não desfaria nada nos outros serviços. Cada gravação local é
 * curta (saveAndFlush) e as falhas entre serviços são tratadas com compensação.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmprestimoService {

    private final EmprestimoRepository repository;
    private final UsuarioClient usuarioClient;
    private final EquipamentoClient equipamentoClient;
    private final Clock clock;

    /** RF03, RF04, RF05 — fluxo obrigatório do item 7 do enunciado. */
    public EmprestimoResponse solicitar(EmprestimoRequest request) {
        UUID usuarioId = request.usuarioId();
        UUID equipamentoId = request.equipamentoId();

        // O ID nasce antes de tudo: é a chave estável da reserva idempotente no msequipamento.
        UUID emprestimoId = UUID.randomUUID();

        // Passo 3 — o usuário existe? (404 se não)
        validarUsuario(usuarioId);

        // Passo 4 — o equipamento existe e está livre? (404 / 409)
        EquipamentoResponse equipamento = consultarEquipamento(equipamentoId);
        if (!Boolean.TRUE.equals(equipamento.disponivel())) {
            throw new ConflitoException("Equipamento " + equipamento.patrimonio() + " está indisponível");
        }
        if (repository.existsByEquipamentoIdAndStatus(equipamentoId, StatusEmprestimo.ATIVO)) {
            throw new ConflitoException("Equipamento " + equipamento.patrimonio() + " já possui empréstimo ativo");
        }

        // Passo 4 — reserva atômica. A consulta acima pode ficar velha; quem decide de fato é esta chamada.
        reservarEquipamento(equipamentoId, emprestimoId);

        // Passo 5 — só depois da reserva o empréstimo é gravado como ATIVO
        Emprestimo emprestimo = Emprestimo.novoAtivo(emprestimoId, usuarioId, equipamentoId, LocalDateTime.now(clock));
        try {
            Emprestimo salvo = repository.saveAndFlush(emprestimo);
            log.info("Empréstimo {} registrado: usuário {} / equipamento {}", emprestimoId, usuarioId, equipamentoId);
            return EmprestimoResponse.de(salvo);
        } catch (RuntimeException e) {
            // Reservou mas não gravou: desfaz a reserva para o equipamento não ficar preso.
            log.error("Falha ao gravar o empréstimo {}; desfazendo a reserva do equipamento {}", emprestimoId, equipamentoId, e);
            liberarComoCompensacao(equipamentoId, emprestimoId);
            if (e instanceof DataIntegrityViolationException) {
                throw new ConflitoException("Equipamento já possui empréstimo ativo");
            }
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public EmprestimoResponse buscarPorId(UUID id) {
        return EmprestimoResponse.de(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<EmprestimoResponse> listarPorUsuario(UUID usuarioId) {
        return repository.findByUsuarioIdOrderByDataEmprestimoDesc(usuarioId).stream()
                .map(EmprestimoResponse::de)
                .toList();
    }

    /**
     * RF07 — devolução. Ordem: primeiro libera, depois grava DEVOLVIDO.
     * - Se a liberação falhar, o empréstimo continua ATIVO e a devolução pode ser repetida.
     * - Se a liberação der certo e a gravação falhar, repetir é seguro: a liberação é idempotente.
     * - Um empréstimo já DEVOLVIDO é recusado antes de qualquer chamada: nunca libera duas vezes.
     */
    public EmprestimoResponse devolver(UUID id) {
        Emprestimo emprestimo = buscarAtivo(id, "devolvido");
        liberarEquipamento(emprestimo);
        emprestimo.devolver(LocalDateTime.now(clock));
        Emprestimo salvo = repository.saveAndFlush(emprestimo);
        log.info("Empréstimo {} devolvido", id);
        return EmprestimoResponse.de(salvo);
    }

    /** Extra (fora do contrato mínimo): cancela um empréstimo ativo e libera o equipamento. */
    public EmprestimoResponse cancelar(UUID id) {
        Emprestimo emprestimo = buscarAtivo(id, "cancelado");
        liberarEquipamento(emprestimo);
        emprestimo.cancelar();
        Emprestimo salvo = repository.saveAndFlush(emprestimo);
        log.info("Empréstimo {} cancelado", id);
        return EmprestimoResponse.de(salvo);
    }

    // ------------------------------------------------------------------ apoio

    private Emprestimo buscar(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empréstimo " + id + " não encontrado"));
    }

    private Emprestimo buscarAtivo(UUID id, String acao) {
        Emprestimo emprestimo = buscar(id);
        if (!emprestimo.isAtivo()) {
            throw new ConflitoException("Empréstimo " + id + " não pode ser " + acao
                    + ": status atual é " + emprestimo.getStatus());
        }
        return emprestimo;
    }

    private void validarUsuario(UUID usuarioId) {
        try {
            usuarioClient.buscarPorId(usuarioId);
        } catch (FeignException.NotFound e) {
            throw new RecursoNaoEncontradoException("Usuário " + usuarioId + " não encontrado");
        } catch (FeignException e) {
            throw new ServicoIndisponivelException("Não foi possível consultar o msusuario", e);
        }
    }

    private EquipamentoResponse consultarEquipamento(UUID equipamentoId) {
        try {
            return equipamentoClient.buscarPorId(equipamentoId);
        } catch (FeignException.NotFound e) {
            throw new RecursoNaoEncontradoException("Equipamento " + equipamentoId + " não encontrado");
        } catch (FeignException e) {
            throw new ServicoIndisponivelException("Não foi possível consultar o msequipamento", e);
        }
    }

    private void reservarEquipamento(UUID equipamentoId, UUID emprestimoId) {
        try {
            equipamentoClient.reservar(equipamentoId, emprestimoId);
        } catch (FeignException.NotFound e) {
            throw new RecursoNaoEncontradoException("Equipamento " + equipamentoId + " não encontrado");
        } catch (FeignException.Conflict e) {
            // Outro empréstimo reservou entre a consulta e a reserva (corrida): o msequipamento barrou.
            throw new ConflitoException("Equipamento " + equipamentoId + " foi reservado por outro empréstimo");
        } catch (FeignException e) {
            // Timeout ou erro: não dá para saber se a reserva aconteceu. Como a liberação é
            // idempotente e usa o mesmo emprestimoId, desfazer "por garantia" é seguro.
            liberarComoCompensacao(equipamentoId, emprestimoId);
            throw new ServicoIndisponivelException("Não foi possível reservar o equipamento no msequipamento", e);
        }
    }

    private void liberarEquipamento(Emprestimo emprestimo) {
        try {
            equipamentoClient.liberar(emprestimo.getEquipamentoId(), emprestimo.getId());
        } catch (FeignException.NotFound e) {
            // O equipamento não existe mais no msequipamento: não há o que liberar; segue a devolução.
            log.warn("Equipamento {} não encontrado ao liberar o empréstimo {}", emprestimo.getEquipamentoId(), emprestimo.getId());
        } catch (FeignException e) {
            throw new ServicoIndisponivelException(
                    "Não foi possível liberar o equipamento no msequipamento. O empréstimo continua ATIVO; tente novamente.", e);
        }
    }

    private void liberarComoCompensacao(UUID equipamentoId, UUID emprestimoId) {
        try {
            equipamentoClient.liberar(equipamentoId, emprestimoId);
            log.info("Compensação: reserva do equipamento {} para o empréstimo {} desfeita", equipamentoId, emprestimoId);
        } catch (FeignException e) {
            // Limite da consistência síncrona: sem mensageria, resta registrar para correção manual.
            log.error("COMPENSAÇÃO FALHOU: o equipamento {} pode ter ficado reservado para o empréstimo {}, que não existe. "
                    + "Corrigir com DELETE /api/v1/equipamentos/{}/reservas/{}",
                    equipamentoId, emprestimoId, equipamentoId, emprestimoId, e);
        }
    }
}

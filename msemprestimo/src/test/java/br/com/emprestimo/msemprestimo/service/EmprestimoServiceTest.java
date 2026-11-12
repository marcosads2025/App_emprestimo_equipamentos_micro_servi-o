package br.com.emprestimo.msemprestimo.service;

import br.com.emprestimo.msemprestimo.client.EquipamentoClient;
import br.com.emprestimo.msemprestimo.client.UsuarioClient;
import br.com.emprestimo.msemprestimo.client.dto.EquipamentoResponse;
import br.com.emprestimo.msemprestimo.client.dto.UsuarioResponse;
import br.com.emprestimo.msemprestimo.dto.EmprestimoRequest;
import br.com.emprestimo.msemprestimo.dto.EmprestimoResponse;
import br.com.emprestimo.msemprestimo.exception.ConflitoException;
import br.com.emprestimo.msemprestimo.exception.RecursoNaoEncontradoException;
import br.com.emprestimo.msemprestimo.exception.ServicoIndisponivelException;
import br.com.emprestimo.msemprestimo.model.Emprestimo;
import br.com.emprestimo.msemprestimo.model.StatusEmprestimo;
import br.com.emprestimo.msemprestimo.repository.EmprestimoRepository;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Testes unitários do fluxo do item 7: clientes Feign e repositório simulados com Mockito. */
@ExtendWith(MockitoExtension.class)
class EmprestimoServiceTest {

    private static final UUID USUARIO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID EQUIPAMENTO_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID EMPRESTIMO_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final Instant AGORA = Instant.parse("2026-11-10T13:00:00Z");
    private static final LocalDateTime AGORA_LOCAL = LocalDateTime.of(2026, 11, 10, 13, 0);

    @Mock
    private EmprestimoRepository repository;
    @Mock
    private UsuarioClient usuarioClient;
    @Mock
    private EquipamentoClient equipamentoClient;

    private EmprestimoService service;

    @BeforeEach
    void setUp() {
        Clock relogioFixo = Clock.fixed(AGORA, ZoneOffset.UTC);
        service = new EmprestimoService(repository, usuarioClient, equipamentoClient, relogioFixo);
    }

    // ------------------------------------------------------------ fixtures

    private static final EmprestimoRequest REQUEST = new EmprestimoRequest(USUARIO_ID, EQUIPAMENTO_ID);

    private UsuarioResponse usuario() {
        return new UsuarioResponse(USUARIO_ID, "Ana Souza", "ana@escola.edu.br", "MAT-001");
    }

    private EquipamentoResponse equipamento(boolean disponivel) {
        return new EquipamentoResponse(EQUIPAMENTO_ID, "Notebook Dell", "NOTEBOOK", "PAT-0001", disponivel, null);
    }

    private Emprestimo emprestimoAtivo() {
        return Emprestimo.novoAtivo(EMPRESTIMO_ID, USUARIO_ID, EQUIPAMENTO_ID, AGORA_LOCAL.minusDays(2));
    }

    /** Cria a mesma FeignException que o Feign lançaria ao receber esse status HTTP. */
    private static FeignException erroHttp(int status) {
        Request request = Request.create(Request.HttpMethod.GET, "http://servico/teste", Map.of(), null,
                StandardCharsets.UTF_8, null);
        Response response = Response.builder()
                .status(status)
                .reason("erro simulado")
                .request(request)
                .headers(Map.of())
                .build();
        return FeignException.errorStatus("teste", response);
    }

    /** Usuário existe, equipamento disponível e sem empréstimo ativo local. */
    private void cenarioValidoAteAReserva() {
        when(usuarioClient.buscarPorId(USUARIO_ID)).thenReturn(usuario());
        when(equipamentoClient.buscarPorId(EQUIPAMENTO_ID)).thenReturn(equipamento(true));
        when(repository.existsByEquipamentoIdAndStatus(EQUIPAMENTO_ID, StatusEmprestimo.ATIVO)).thenReturn(false);
    }

    // ------------------------------------------------------------ solicitar

    @Test
    void solicitarComSucessoReservaComOMesmoIdQueGrava() {
        cenarioValidoAteAReserva();
        when(equipamentoClient.reservar(eq(EQUIPAMENTO_ID), any(UUID.class))).thenReturn(equipamento(false));
        when(repository.saveAndFlush(any(Emprestimo.class))).thenAnswer(inv -> inv.getArgument(0));

        EmprestimoResponse resposta = service.solicitar(REQUEST);

        assertEquals(StatusEmprestimo.ATIVO, resposta.status());
        assertEquals(USUARIO_ID, resposta.usuarioId());
        assertEquals(EQUIPAMENTO_ID, resposta.equipamentoId());
        assertEquals(LocalDateTime.of(2026, 11, 10, 13, 0), resposta.dataEmprestimo());
        assertNull(resposta.dataDevolucao());

        // O emprestimoId usado na reserva é exatamente o id do empréstimo gravado
        ArgumentCaptor<UUID> idReserva = ArgumentCaptor.forClass(UUID.class);
        verify(equipamentoClient).reservar(eq(EQUIPAMENTO_ID), idReserva.capture());
        assertEquals(resposta.id(), idReserva.getValue());
    }

    @Test
    void solicitarComUsuarioInexistenteRetorna404SemTocarNoEquipamento() {
        when(usuarioClient.buscarPorId(USUARIO_ID)).thenThrow(erroHttp(404));

        assertThrows(RecursoNaoEncontradoException.class, () -> service.solicitar(REQUEST));
        verifyNoInteractions(equipamentoClient);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void solicitarComMsUsuarioForaDoArRetorna503() {
        when(usuarioClient.buscarPorId(USUARIO_ID)).thenThrow(erroHttp(503));

        assertThrows(ServicoIndisponivelException.class, () -> service.solicitar(REQUEST));
        verifyNoInteractions(equipamentoClient);
    }

    @Test
    void solicitarComEquipamentoInexistenteRetorna404() {
        when(usuarioClient.buscarPorId(USUARIO_ID)).thenReturn(usuario());
        when(equipamentoClient.buscarPorId(EQUIPAMENTO_ID)).thenThrow(erroHttp(404));

        assertThrows(RecursoNaoEncontradoException.class, () -> service.solicitar(REQUEST));
        verify(equipamentoClient, never()).reservar(any(), any());
    }

    @Test
    void solicitarComEquipamentoIndisponivelRetorna409SemReservar() {
        when(usuarioClient.buscarPorId(USUARIO_ID)).thenReturn(usuario());
        when(equipamentoClient.buscarPorId(EQUIPAMENTO_ID)).thenReturn(equipamento(false));

        assertThrows(ConflitoException.class, () -> service.solicitar(REQUEST));
        verify(equipamentoClient, never()).reservar(any(), any());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void solicitarQuandoOutroEmprestimoReservaNaFrenteRetorna409() {
        // A consulta disse "disponível", mas outra requisição reservou antes: o msequipamento responde 409.
        cenarioValidoAteAReserva();
        when(equipamentoClient.reservar(eq(EQUIPAMENTO_ID), any(UUID.class))).thenThrow(erroHttp(409));

        assertThrows(ConflitoException.class, () -> service.solicitar(REQUEST));
        verify(repository, never()).saveAndFlush(any());
        verify(equipamentoClient, never()).liberar(any(), any()); // a reserva é do outro: não pode liberar
    }

    @Test
    void solicitarComFalhaNaReservaRetorna503EDesfazPorGarantia() {
        cenarioValidoAteAReserva();
        when(equipamentoClient.reservar(eq(EQUIPAMENTO_ID), any(UUID.class))).thenThrow(erroHttp(503));

        assertThrows(ServicoIndisponivelException.class, () -> service.solicitar(REQUEST));

        ArgumentCaptor<UUID> idReserva = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<UUID> idLiberacao = ArgumentCaptor.forClass(UUID.class);
        verify(equipamentoClient).reservar(eq(EQUIPAMENTO_ID), idReserva.capture());
        verify(equipamentoClient).liberar(eq(EQUIPAMENTO_ID), idLiberacao.capture());
        assertEquals(idReserva.getValue(), idLiberacao.getValue());
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void solicitarComFalhaAoGravarLiberaAReserva() {
        cenarioValidoAteAReserva();
        when(equipamentoClient.reservar(eq(EQUIPAMENTO_ID), any(UUID.class))).thenReturn(equipamento(false));
        when(repository.saveAndFlush(any(Emprestimo.class)))
                .thenThrow(new DataIntegrityViolationException("uk_emprestimos_equipamento_ativo"));

        assertThrows(ConflitoException.class, () -> service.solicitar(REQUEST));

        ArgumentCaptor<UUID> idReserva = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<UUID> idLiberacao = ArgumentCaptor.forClass(UUID.class);
        verify(equipamentoClient).reservar(eq(EQUIPAMENTO_ID), idReserva.capture());
        verify(equipamentoClient).liberar(eq(EQUIPAMENTO_ID), idLiberacao.capture());
        assertEquals(idReserva.getValue(), idLiberacao.getValue());
    }

    // ------------------------------------------------------------ consultas

    @Test
    void buscarEmprestimoInexistenteRetorna404() {
        when(repository.findById(EMPRESTIMO_ID)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscarPorId(EMPRESTIMO_ID));
    }

    // ------------------------------------------------------------ devolução

    @Test
    void devolverLiberaOEquipamentoEMarcaDevolvido() {
        when(repository.findById(EMPRESTIMO_ID)).thenReturn(Optional.of(emprestimoAtivo()));
        when(repository.saveAndFlush(any(Emprestimo.class))).thenAnswer(inv -> inv.getArgument(0));

        EmprestimoResponse resposta = service.devolver(EMPRESTIMO_ID);

        assertEquals(StatusEmprestimo.DEVOLVIDO, resposta.status());
        assertEquals(AGORA_LOCAL, resposta.dataDevolucao());
        verify(equipamentoClient).liberar(EQUIPAMENTO_ID, EMPRESTIMO_ID);
    }

    @Test
    void devolverDuasVezesRetorna409ENaoLiberaDeNovo() {
        Emprestimo devolvido = emprestimoAtivo();
        devolvido.devolver(AGORA_LOCAL.minusHours(1));
        when(repository.findById(EMPRESTIMO_ID)).thenReturn(Optional.of(devolvido));

        assertThrows(ConflitoException.class, () -> service.devolver(EMPRESTIMO_ID));
        verifyNoInteractions(equipamentoClient);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void devolverComMsEquipamentoForaDoArMantemAtivo() {
        Emprestimo ativo = emprestimoAtivo();
        when(repository.findById(EMPRESTIMO_ID)).thenReturn(Optional.of(ativo));
        doThrow(erroHttp(503)).when(equipamentoClient).liberar(EQUIPAMENTO_ID, EMPRESTIMO_ID);

        assertThrows(ServicoIndisponivelException.class, () -> service.devolver(EMPRESTIMO_ID));
        assertEquals(StatusEmprestimo.ATIVO, ativo.getStatus());
        verify(repository, never()).saveAndFlush(any());
    }

    // ------------------------------------------------------------ cancelamento

    @Test
    void cancelarLiberaOEquipamentoEMarcaCancelado() {
        when(repository.findById(EMPRESTIMO_ID)).thenReturn(Optional.of(emprestimoAtivo()));
        when(repository.saveAndFlush(any(Emprestimo.class))).thenAnswer(inv -> inv.getArgument(0));

        EmprestimoResponse resposta = service.cancelar(EMPRESTIMO_ID);

        assertEquals(StatusEmprestimo.CANCELADO, resposta.status());
        verify(equipamentoClient).liberar(EQUIPAMENTO_ID, EMPRESTIMO_ID);
    }
}

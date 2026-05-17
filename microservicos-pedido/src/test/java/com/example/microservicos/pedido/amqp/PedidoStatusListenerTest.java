package com.example.microservicos.pedido.amqp;

import com.example.microservicos.pedido.model.Pedido;
import com.example.microservicos.pedido.model.PedidoStatusDto;
import com.example.microservicos.pedido.model.Status;
import com.example.microservicos.pedido.repository.PedidoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PedidoStatusListenerTest {

    @InjectMocks
    private PedidoStatusListener pedidoStatusListener;

    @Mock
    private PedidoRepository pedidoRepository;

    @Captor
    private ArgumentCaptor<Pedido> captor;

    @DisplayName("Deve atualizar status do pedido de PENDENTE para APROVADO")
    @Test
    void deveAtualizarStatusParaAprovado() {

        Pedido pedido = new Pedido(1L, 1L, 2, Status.PENDENTE);

        PedidoStatusDto dto = new PedidoStatusDto(1L, Status.APROVADO);

        Mockito.when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        Assertions.assertEquals(Status.PENDENTE, pedido.getStatus());

        pedidoStatusListener.atualizaStatus(dto);

        Mockito.verify(pedidoRepository).save(captor.capture());

        Pedido pedidoSalvo = captor.getValue();

        Assertions.assertEquals(Status.APROVADO, pedidoSalvo.getStatus());
    }

    @DisplayName("Deve falhar ao atualizar o status do pedido, dado um pedidoId inexistente")
    @Test
    void deveFalharQuandoPedidoNaoExistir() {

        PedidoStatusDto dto = new PedidoStatusDto(1L, Status.APROVADO);

        Mockito.when(pedidoRepository.findById(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(RuntimeException.class, () -> pedidoStatusListener.atualizaStatus(dto));

        Mockito.verify(pedidoRepository, Mockito.never()).save(Mockito.any());
    }
}

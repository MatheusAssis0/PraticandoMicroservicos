package com.example.microservicos.pedido.service;

import com.example.microservicos.pedido.model.Pedido;
import com.example.microservicos.pedido.model.PedidoRequestDto;
import com.example.microservicos.pedido.model.Status;
import com.example.microservicos.pedido.repository.PedidoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @InjectMocks
    private PedidoService pedidoService;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Captor
    private ArgumentCaptor<Pedido> captor;

    @DisplayName("Quando buscar um pedido por ID")
    @Nested
    class BuscaPorId {
        @DisplayName("Então deve buscar com sucesso")
        @Nested
        class Sucesso {
            @DisplayName("Dado um ID válido")
            @Test
            void buscaPorIdSucesso() {

                Long id = 1L;
                Pedido pedido = new Pedido(id, id, 5, Status.PENDENTE);
                when(pedidoRepository.findById(id)).thenReturn(Optional.of(pedido));

                var pedidoDtoResponse = pedidoService.getPedido(id);

                Assertions.assertEquals(pedido.getId(), pedidoDtoResponse.id());
                Assertions.assertEquals(pedido.getProdutoId(), pedidoDtoResponse.produtoId());
                Assertions.assertEquals(pedido.getQuantidade(), pedidoDtoResponse.quantidade());
                Assertions.assertEquals(pedido.getStatus(), pedidoDtoResponse.status());
            }
        }
        @DisplayName("Então deve falhar ao buscar")
        @Nested
        class Falha {
            @DisplayName("Dado um ID inválido")
            @Test
            void buscaPorIdFalha() {

                Long id = 1L;
                when(pedidoRepository.findById(id)).thenReturn(Optional.empty());

                var exception = Assertions.assertThrows(ResponseStatusException.class, () -> pedidoService.getPedido(id));

                Assertions.assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
                Assertions.assertEquals("Pedido não encontrado", exception.getReason());
            }
        }
    }

    @DisplayName("Quando criar um pedido")
    @Nested
    class CriarPedido {
        @DisplayName("Então deve criar com sucesso")
        @Nested
        class Sucesso {
            @DisplayName("Dado um JSON preenchido corretamente")
            @Test
            void criarPedidoSucesso() {

                PedidoRequestDto pedidoRequestDto = new PedidoRequestDto(1L, 1L, 2);
                Pedido pedido = new Pedido(null, 1L, 2, Status.PENDENTE);

                when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

                pedidoService.criarPedido(pedidoRequestDto);

                then(pedidoRepository).should().save(captor.capture());
                Pedido pedidoSalvo = captor.getValue();

                Assertions.assertEquals(pedido.getProdutoId(), pedidoSalvo.getProdutoId());
                Assertions.assertEquals(pedido.getQuantidade(), pedidoSalvo.getQuantidade());
                Assertions.assertEquals(Status.PENDENTE, pedidoSalvo.getStatus());
                then(rabbitTemplate).should().convertAndSend(anyString(), any(PedidoRequestDto.class));
            }
        }
    }
}
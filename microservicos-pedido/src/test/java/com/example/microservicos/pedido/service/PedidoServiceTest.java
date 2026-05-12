package com.example.microservicos.pedido.service;

import com.example.microservicos.pedido.model.DiminuirEstoqueDto;
import com.example.microservicos.pedido.model.Pedido;
import com.example.microservicos.pedido.model.PedidoRequestDto;
import com.example.microservicos.pedido.model.ProdutoResponseDto;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @InjectMocks
    private PedidoService pedidoService;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProdutoServiceClient produtoServiceClient;

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
                Pedido pedido = new Pedido(id, id, 5, BigDecimal.valueOf(5));
                when(pedidoRepository.findById(id)).thenReturn(Optional.of(pedido));

                var pedidoDtoResponse = pedidoService.getPedido(id);

                Assertions.assertEquals(pedido.getId(), pedidoDtoResponse.id());
                Assertions.assertEquals(pedido.getProdutoId(), pedidoDtoResponse.produtoId());
                Assertions.assertEquals(pedido.getQuantidade(), pedidoDtoResponse.quantidade());
                Assertions.assertEquals(pedido.getValorTotal(), pedidoDtoResponse.valorTotal());
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

                PedidoRequestDto pedidoRequestDto = new PedidoRequestDto(1L, 2);
                ProdutoResponseDto produtoResponse = new ProdutoResponseDto(1L, "Lapis", BigDecimal.valueOf(2.50), 200);
                var valorTotal = produtoResponse.preco().multiply(BigDecimal.valueOf(pedidoRequestDto.quantidade()));
                Pedido pedido = new Pedido(null, 1L, 2, valorTotal);

                when(produtoServiceClient.getProduto(1L)).thenReturn(produtoResponse);
                when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

                pedidoService.criarPedido(pedidoRequestDto);

                then(pedidoRepository).should().save(captor.capture());
                then(produtoServiceClient).should().decreaseStock(1L, new DiminuirEstoqueDto(2));
                Pedido pedidoSalvo = captor.getValue();

                Assertions.assertEquals(pedido.getProdutoId(), pedidoSalvo.getProdutoId());
                Assertions.assertEquals(pedido.getQuantidade(), pedidoSalvo.getQuantidade());
                Assertions.assertEquals(0, pedido.getValorTotal().compareTo(pedidoSalvo.getValorTotal()));
            }
        }

        @DisplayName("Então deve falhar ao criar")
        @Nested
        class Falha {
            @DisplayName("Dado um ID de produto inválido")
            @Test
            void criarPedidoFalhaIdProduto() {

                Long id = 1L;
                PedidoRequestDto pedidoRequestDto = new PedidoRequestDto(id, 2);

                when(produtoServiceClient.getProduto(1L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));

                var exception = Assertions.assertThrows(ResponseStatusException.class, () -> pedidoService.criarPedido(pedidoRequestDto));

                Assertions.assertEquals("Produto não encontrado", exception.getReason());
                Assertions.assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
                then(pedidoRepository).should(never()).save(any());
            }

            @DisplayName("Dado uma quantidade maior que o estoque")
            @Test
            void criarPedidoFalhaEstoqueInsuficiente () {

                Long id = 1L;
                PedidoRequestDto pedidoRequestDto = new PedidoRequestDto(id, 2);
                ProdutoResponseDto produtoResponse = new ProdutoResponseDto(1L, "Lapis", BigDecimal.valueOf(2.50), 1);

                when(produtoServiceClient.getProduto(1L)).thenReturn(produtoResponse);

                var exception = Assertions.assertThrows(ResponseStatusException.class, () -> pedidoService.criarPedido(pedidoRequestDto));
                Assertions.assertEquals("Estoque insuficiente", exception.getReason());
                Assertions.assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
                then(pedidoRepository).should(never()).save(any());
                then(produtoServiceClient).should(never()).decreaseStock(anyLong(), any());
            }
        }
    }
}

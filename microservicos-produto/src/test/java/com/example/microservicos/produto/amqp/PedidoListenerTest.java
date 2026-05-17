package com.example.microservicos.produto.amqp;

import com.example.microservicos.produto.model.PedidoRequestDto;
import com.example.microservicos.produto.model.PedidoStatusDto;
import com.example.microservicos.produto.model.Produto;
import com.example.microservicos.produto.model.Status;
import com.example.microservicos.produto.repository.ProdutoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoListenerTest {

    @InjectMocks
    private PedidoListener pedidoListener;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @DisplayName("Deve aprovar pedido, dado um pedido com produtoId válido e estoque suficiente")
    @Test
    void deveAprovarPedido() {

        PedidoRequestDto dto = new PedidoRequestDto(1L, 1L, 1);

        Produto produto = new Produto(1L, "Notebook", BigDecimal.valueOf(1000.0), 10);

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        pedidoListener.recebeMensagem(dto);

        assertEquals(9, produto.getEstoque());

        verify(produtoRepository).save(produto);

        verify(rabbitTemplate).convertAndSend(eq("pedido.status"), eq(new PedidoStatusDto(1L, Status.APROVADO)));
    }

    @DisplayName("Deve rejeitar pedido, dado um pedido com quantidade > estoque")
    @Test
    void deveRejeitarPedidoSemEstoque() {

        PedidoRequestDto dto = new PedidoRequestDto(1L, 1L, 20);

        Produto produto = new Produto(1L, "Notebook", BigDecimal.valueOf(1000.0), 10);

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        pedidoListener.recebeMensagem(dto);

        verify(produtoRepository, never()).save(any());

        verify(rabbitTemplate).convertAndSend(eq("pedido.status"), eq(new PedidoStatusDto(1L, Status.REJEITADO)));
    }
}
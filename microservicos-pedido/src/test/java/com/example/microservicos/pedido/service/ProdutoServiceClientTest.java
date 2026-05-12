package com.example.microservicos.pedido.service;

import com.example.microservicos.pedido.client.ProdutoClient;
import com.example.microservicos.pedido.model.DiminuirEstoqueDto;
import com.example.microservicos.pedido.model.ProdutoResponseDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceClientTest {

    @InjectMocks
    private ProdutoServiceClient produtoServiceClient;

    @Mock
    private ProdutoClient produtoClient;

    @Test
    void deveBuscarProdutoComSucesso() {

        ProdutoResponseDto response = new ProdutoResponseDto(1L, "Lapis", BigDecimal.valueOf(10.0), 100);

        when(produtoClient.getProductById(1L)).thenReturn(response);

        ProdutoResponseDto result = produtoServiceClient.getProduto(1L);

        Assertions.assertEquals(response, result);

        verify(produtoClient).getProductById(1L);
    }

    @Test
    void deveDiminuirEstoque() {

        produtoServiceClient.decreaseStock(1L, new DiminuirEstoqueDto(2));

        verify(produtoClient).decreaseStock(eq(1L), any(DiminuirEstoqueDto.class));
    }

    @Test
    void deveExecutarFallback() {

        ResponseStatusException exception = Assertions.assertThrows(ResponseStatusException.class,
                () -> produtoServiceClient.fallbackGetProduto(1L, new RuntimeException()));

        Assertions.assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    }
}
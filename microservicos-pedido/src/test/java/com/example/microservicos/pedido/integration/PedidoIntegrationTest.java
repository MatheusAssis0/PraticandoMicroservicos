package com.example.microservicos.pedido.integration;

import com.example.microservicos.pedido.ClasseAbstracaoTeste;
import com.example.microservicos.pedido.client.ProdutoClient;
import com.example.microservicos.pedido.model.Pedido;
import com.example.microservicos.pedido.model.ProdutoResponseDto;
import com.example.microservicos.pedido.repository.PedidoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PedidoIntegrationTest extends ClasseAbstracaoTeste {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PedidoRepository pedidoRepository;

    @MockBean
    private ProdutoClient produtoClient;

    @BeforeEach
    void limparDatabase() {
        pedidoRepository.deleteAll();
    }

    @DisplayName("Deve listar o pedido com sucesso dado um ID válido")
    @Test
    void deveListarPedidoComSucesso() throws Exception {

        Pedido pedidoSalvo = pedidoRepository.save(new Pedido(null, 1L, 1, BigDecimal.valueOf(5.0)));

        mockMvc.perform(get("/pedidos/{id}", pedidoSalvo.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produtoId").value(1))
                .andExpect(jsonPath("$.quantidade").value(1))
                .andExpect(jsonPath("$.valorTotal").value(5.0));
    }

    @DisplayName("Deve falhar ao listar pedido com ID inválido")
    @Test
    void deveFalharAoListarPedidoComIdInvalido() throws Exception {

        mockMvc.perform(get("/pedidos/{id}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Pedido não encontrado"));
    }

    @DisplayName("Deve criar o pedido com sucesso dado um JSON válido")
    @Test
    void deveCriarPedidoComSucesso() throws Exception {

        var json = """
                {
                    "produtoId": 1,
                    "quantidade": 1
                }
                """;

        Mockito.when(produtoClient.getProductById(1L))
                .thenReturn(new ProdutoResponseDto(1L, "Lapis", BigDecimal.valueOf(5.0), 100));

        Mockito.doNothing().when(produtoClient).decreaseStock(Mockito.anyLong(), Mockito.any());

        mockMvc.perform(post("/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.produtoId").value(1))
                .andExpect(jsonPath("$.quantidade").value(1));

        var pedidos = pedidoRepository.findAll();

        Assertions.assertEquals(1, pedidos.size());
        Assertions.assertEquals(1, pedidos.get(0).getQuantidade());
        Assertions.assertEquals(1L, pedidos.get(0).getProdutoId());
        Assertions.assertEquals(0, pedidos.get(0).getValorTotal().compareTo(BigDecimal.valueOf(5)));
    }

    @DisplayName("Deve falhar ao criar pedido dado um produtoID inexistente")
    @Test
    void deveFalharCriarPedidoComProdutoInexistente() throws Exception {

        var json = """
                {
                    "produtoId": 1,
                    "quantidade": 1
                }
                """;

        Mockito.when(produtoClient.getProductById(1L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));

        mockMvc.perform(post("/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Produto não encontrado"));

        Assertions.assertEquals(0, pedidoRepository.count());
    }

    @DisplayName("Deve falhar ao criar pedido dado uma quantidade > estoque")
    @Test
    void deveFalharCriarPedidoComEstoqueInsuficiente() throws Exception {

        var json = """
                {
                    "produtoId": 1,
                    "quantidade": 1
                }
                """;

        Mockito.when(produtoClient.getProductById(1L))
                .thenReturn(new ProdutoResponseDto(1L, "Lapis", BigDecimal.valueOf(5.0), 0));

        mockMvc.perform(post("/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(status().reason("Estoque insuficiente"));

        Assertions.assertEquals(0, pedidoRepository.count());
    }
}
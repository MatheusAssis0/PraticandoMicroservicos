package com.example.microservicos.pedido.controller;

import com.example.microservicos.pedido.model.Pedido;
import com.example.microservicos.pedido.model.PedidoResponseDto;
import com.example.microservicos.pedido.model.Status;
import com.example.microservicos.pedido.service.PedidoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PedidoController.class)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PedidoService pedidoService;

    @DisplayName("Quando buscar um pedido")
    @Nested
    class BuscarPedido {
        @DisplayName("Então deve buscar com sucesso")
        @Nested
        class Sucesso {
            @DisplayName("Dado um ID válido")
            @Test
            void buscarIdPedido() throws Exception {

                Long id = 1L;
                Pedido pedido = new Pedido(id, id, 1, Status.PENDENTE);

                Mockito.when(pedidoService.getPedido(id)).thenReturn(new PedidoResponseDto(pedido));

                mockMvc.perform(get("/pedidos/{id}", id))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(id))
                        .andExpect(jsonPath("$.produtoId").value(id))
                        .andExpect(jsonPath("$.quantidade").value(1))
                        .andExpect(jsonPath("$.status").value("PENDENTE"));
            }
        }

        @DisplayName("Então deve falhar ao buscar")
        @Nested
        class Falha {
            @DisplayName("Dado um ID inválido")
            @Test
            void buscarPedidoIdInvalido() throws Exception {

                Long id = 1L;

                Mockito.when(pedidoService.getPedido(id)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado"));

                mockMvc.perform(get("/pedidos/{id}", id))
                        .andExpect(status().isNotFound())
                        .andExpect(status().reason("Pedido não encontrado"));
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
            void criarPedidoSucesso() throws Exception {

                var json = """
                        {
                          "produtoId": 1,
                          "quantidade": 1
                        }
                        """;

                PedidoResponseDto pedidoResponseDto = new PedidoResponseDto(1L, 1L, 1, Status.PENDENTE);

                Mockito.when(pedidoService.criarPedido(Mockito.any())).thenReturn(pedidoResponseDto);

                mockMvc.perform(post("/pedidos")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.produtoId").value(1))
                        .andExpect(jsonPath("$.quantidade").value(1))
                        .andExpect(jsonPath("$.status").value("PENDENTE"));
            }
        }

        @DisplayName("Então deve falhar ao criar")
        @Nested
        class Falha {
            @DisplayName("Dado um pedido sem produtoId")
            @Test
            void criarPedidoSemProdutoId() throws Exception {

                var json = """
                        {
                            "quantidade": 1
                        }
                        """;

                mockMvc.perform(post("/pedidos")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest());
            }

            @DisplayName("Dado um pedido com quantidade zerada")
            @Test
            void criarPedidoQuantidadeZero() throws Exception {

                var json = """
                        {
                            "produtoId": 1,
                            "quantidade": 0
                        }
                        """;

                mockMvc.perform(post("/pedidos")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest());
            }
        }
    }
}
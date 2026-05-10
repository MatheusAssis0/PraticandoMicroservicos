package com.example.microservicos.produto.controller;

import com.example.microservicos.produto.model.Produto;
import com.example.microservicos.produto.model.ProdutoDtoResponse;
import com.example.microservicos.produto.service.ProdutoService;
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

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProdutoController.class)
class ProdutoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProdutoService produtoService;

    @DisplayName("Quando buscar um produto")
    @Nested
    class BuscarProduto {
        @DisplayName("Então deve buscar com sucesso")
        @Nested
        class Sucesso {
            @DisplayName("Dado um ID válido")
            @Test
            void buscarProduto() throws Exception {

                Long id = 1L;
                Produto produto = new Produto(id, "Lapis", BigDecimal.valueOf(1.00), 10);

                Mockito.when(produtoService.buscarProdutoPorId(id)).thenReturn(new ProdutoDtoResponse(produto));

                mockMvc.perform(get("/produtos/{id}", id))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.id").value(1))
                        .andExpect(jsonPath("$.nome").value("Lapis"))
                        .andExpect(jsonPath("$.preco").value(BigDecimal.valueOf(1.00)))
                        .andExpect(jsonPath("$.estoque").value(10));
            }
        }
        @DisplayName("Então deve falhar ao buscar")
        @Nested
        class Falhar {
            @DisplayName("Dado um ID inválido")
            @Test
            void buscarProdutoIdInvalido() throws Exception {

                Long id = 1L;

                Mockito.when(produtoService.buscarProdutoPorId(id)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));

                mockMvc.perform(get("/produtos/{id}", id))
                        .andExpect(status().isNotFound())
                        .andExpect(status().reason("Produto não encontrado"));
            }
        }
    }

    @DisplayName("Quando criar um produto")
    @Nested
    class CriarProduto {
        @DisplayName("Então deve criar com sucesso")
        @Nested
        class Sucesso {
            @DisplayName("Dado um JSON preenchido corretamente")
            @Test
            void criarProduto() throws Exception {

                var json = """
                        {
                          "nome": "Notebook",
                          "preco": 2000,
                          "estoque": 12
                        }
                        """;

                ProdutoDtoResponse produto = new ProdutoDtoResponse(1L, "Notebook", BigDecimal.valueOf(2000.00), 12);

                Mockito.when(produtoService.criarProduto(Mockito.any())).thenReturn(produto);

                mockMvc.perform(post("/produtos")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.nome").value("Notebook"))
                        .andExpect(jsonPath("$.preco").value(2000.00))
                        .andExpect(jsonPath("$.estoque").value(12));
            }
        }
        @DisplayName("Então deve falhar ao criar")
        @Nested
        class Falhar {
            @DisplayName("Dado produto sem nome")
            @Test
            void criarProdutoSemNome() throws Exception {

                var json = """
                        {
                          "nome": "",
                          "preco": 2000,
                          "estoque": 12
                        }
                        """;

                mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andExpect(status().isBadRequest());
            }

            @DisplayName("Dado um produto com preco negativo")
            @Test
            void criarProdutoComPrecoNegativo() throws Exception {

                var json = """
                        {
                          "nome": "Notebook",
                          "preco": -10,
                          "estoque": 12
                        }
                        """;

                mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andExpect(status().isBadRequest());
            }

            @DisplayName("Dado um produto com estoque negativo")
            @Test
            void criarProdutoComEstoqueNegativo() throws Exception {

                var json = """
                        {
                          "nome": "Notebook",
                          "preco": 10,
                          "estoque": -12
                        }
                        """;

                mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                        .andExpect(status().isBadRequest());
            }
        }
    }

    @DisplayName("Quando diminuir o estoque")
    @Nested
    class DiminuirEstoque {
        @DisplayName("Então deve diminuir com sucesso")
        @Nested
        class Sucesso {
            @DisplayName("Dado um ID e JSON válidos e estoque suficiente")
            @Test
            void diminuirEstoqueComSucesso() throws Exception {

                Long id = 1L;

                var json = """
                        {
                           "quantidade": 2
                        }
                        """;

                ProdutoDtoResponse produto = new ProdutoDtoResponse(id, "Notebook", BigDecimal.valueOf(2000.00), 12);

                Mockito.when(produtoService.diminuirEstoque(Mockito.anyLong(), Mockito.any())).thenReturn(produto);

                mockMvc.perform(patch("/produtos/{id}", id)
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.estoque").value(12));
            }
        }

        @DisplayName("Então deve falhar ao diminuir")
        @Nested
        class Falha {
            @DisplayName("Dado um ID inválido")
            @Test
            void diminuirEstoqueFalhaID() throws Exception {

                Long id = 1L;

                var json = """
                        {
                           "quantidade": 2
                        }
                        """;

                Mockito.when(produtoService.diminuirEstoque(Mockito.anyLong(), Mockito.any()))
                        .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));

                mockMvc.perform(patch("/produtos/{id}", id)
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isNotFound())
                        .andExpect(status().reason("Produto não encontrado"));
            }

            @DisplayName("Dado uma quantidade zerada")
            @Test
            void diminuirEstoqueQuantidadeZero() throws Exception {

                Long id = 1L;

                var json = """
                        {
                           "quantidade": 0
                        }
                        """;

                mockMvc.perform(patch("/produtos/{id}", id)
                                .content(json)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isBadRequest());
            }
        }
    }
}
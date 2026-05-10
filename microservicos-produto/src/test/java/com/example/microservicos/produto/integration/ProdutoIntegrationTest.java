package com.example.microservicos.produto.integration;

import com.example.microservicos.produto.ClasseAbstracaoTeste;
import com.example.microservicos.produto.model.Produto;
import com.example.microservicos.produto.repository.ProdutoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

public class ProdutoIntegrationTest extends ClasseAbstracaoTeste {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProdutoRepository repository;

    @BeforeEach
    void limparDatabase() {
        repository.deleteAll();
    }

    @DisplayName("Deve listar o produto com sucesso dado um ID válido")
    @Test
    void deveListarProdutoComSucesso() throws Exception {

        Produto produtoSalvo = repository.save(new Produto(null, "Lapis" , BigDecimal.valueOf(1.00), 100));

        mockMvc.perform(get("/produtos/{id}", produtoSalvo.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Lapis"))
                .andExpect(jsonPath("$.preco").value(1.00))
                .andExpect(jsonPath("$.estoque").value(100));
    }

    @DisplayName("Deve falhar ao listar produto com ID inválido")
    @Test
    void deveFalharAoListarProdutoComIdInvalido() throws Exception {

        mockMvc.perform(get("/produtos/{id}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Produto não encontrado"));
    }

    @DisplayName("Deve criar o produto com sucesso dado um JSON válido")
    @Test
    void deveCriarProdutoComSucesso() throws Exception {

        var json = """
                {
                  "nome": "Notebook",
                  "preco": 2000,
                  "estoque": 12
                }
                """;

        mockMvc.perform(post("/produtos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Notebook"))
                .andExpect(jsonPath("$.preco").value(2000))
                .andExpect(jsonPath("$.estoque").value(12));

        var produtos = repository.findAll();

        Assertions.assertEquals(1, produtos.size());
        Assertions.assertEquals("Notebook", produtos.get(0).getNome());
        Assertions.assertEquals(0, produtos.get(0).getPreco().compareTo(BigDecimal.valueOf(2000)));
        Assertions.assertEquals(12, produtos.get(0).getEstoque());
    }

    @DisplayName("Deve diminuir o estoque com sucesso dado um JSON e ID válidos e estoque suficiente")
    @Test
    void deveDiminuirEstoqueComSucesso() throws Exception {

        var json = """
                {
                    "quantidade": 2
                }
                """;

        Produto produtoSalvo = repository.save(new Produto(null, "Lapis" , BigDecimal.valueOf(1.00), 100));

        mockMvc.perform(patch("/produtos/{id}", produtoSalvo.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Lapis"))
                .andExpect(jsonPath("$.preco").value(1.00))
                .andExpect(jsonPath("$.estoque").value(98));

        Produto atualizado = repository.findById(produtoSalvo.getId()).get();

        Assertions.assertEquals(98, atualizado.getEstoque());
    }
}

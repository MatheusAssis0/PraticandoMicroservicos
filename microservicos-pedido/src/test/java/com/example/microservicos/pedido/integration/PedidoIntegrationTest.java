package com.example.microservicos.pedido.integration;

import com.example.microservicos.pedido.ClasseAbstracaoTeste;
import com.example.microservicos.pedido.repository.PedidoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureWireMock(port = 8089)
@TestPropertySource(properties = {"produto-service.url=http://localhost:8089"})
public class PedidoIntegrationTest extends ClasseAbstracaoTeste {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PedidoRepository pedidoRepository;

    @BeforeEach
    void limparDatabase() {
        pedidoRepository.deleteAll();
    }

    @Test
    void deveCriarPedidoComFeignReal() throws Exception {

        var json = """
        {
            "produtoId": 1,
            "quantidade": 1
        }
        """;

        stubFor(get(urlEqualTo("/produtos/1"))
                .willReturn(okJson("""
                {
                    "id": 1,
                    "nome": "Lapis",
                    "preco": 5.0,
                    "estoque": 100
                }
                """)));

        stubFor(patch(urlEqualTo("/produtos/1"))
                .willReturn(aResponse().withStatus(200)));

        mockMvc.perform(post("/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.produtoId").value(1))
                .andExpect(jsonPath("$.quantidade").value(1))
                .andExpect(jsonPath("$.valorTotal").value(5.0));
    }

    @Test
    void deveRetornar404QuandoProdutoNaoExistir() throws Exception {

        var json = """
        {
            "produtoId": 1,
            "quantidade": 1
        }
        """;

        stubFor(get(urlEqualTo("/produtos/1")).willReturn(aResponse().withStatus(404)));

        mockMvc.perform(post("/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());

        Assertions.assertEquals(0, pedidoRepository.count());
    }

    @Test
    void deveExecutarFallbackQuandoProdutoServiceCair() throws Exception {

        var json = """
        {
            "produtoId": 1,
            "quantidade": 1
        }
        """;

        stubFor(get(urlEqualTo("/produtos/1")).willReturn(aResponse().withFixedDelay(1000).withStatus(500)));

        mockMvc.perform(post("/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isServiceUnavailable())
                .andExpect(status().reason("Serviço de produto indisponível"));

        Assertions.assertEquals(0, pedidoRepository.count());
    }
}

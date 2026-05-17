package com.example.microservicos.pedido.integration;

import com.example.microservicos.pedido.ClasseAbstracaoTeste;
import com.example.microservicos.pedido.model.Pedido;
import com.example.microservicos.pedido.model.PedidoRequestDto;
import com.example.microservicos.pedido.model.Status;
import com.example.microservicos.pedido.repository.PedidoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class PedidoIntegrationTest extends ClasseAbstracaoTeste {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PedidoRepository pedidoRepository;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void limparDatabase() {
        pedidoRepository.deleteAll();
    }

    @DisplayName("Deve buscar pedido com sucesso, dado um pedidoId válido")
    @Test
    void deveBuscarPedidoPorId() throws Exception {

        Pedido pedido = pedidoRepository.save(new Pedido(null, 1L, 2, Status.PENDENTE));

        mockMvc.perform(get("/pedidos/{id}", pedido.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pedido.getId()))
                .andExpect(jsonPath("$.produtoId").value(1))
                .andExpect(jsonPath("$.quantidade").value(2))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @DisplayName("Deve falhar ao buscar o pedido, dado um pedidoId inexistente")
    @Test
    void deveFalharBuscarPedidoInexistente() throws Exception {

        mockMvc.perform(get("/pedidos/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(status().reason("Pedido não encontrado"));
    }

    @DisplayName("Deve criar com sucesso o pedido com status de PENDENTE")
    @Test
    void deveCriarPedidoComStatusPendente() throws Exception {

        var json = """
    {
        "produtoId": 1,
        "quantidade": 2
    }
    """;

        mockMvc.perform(post("/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.produtoId").value(1))
                .andExpect(jsonPath("$.quantidade").value(2))
                .andExpect(jsonPath("$.status").value("PENDENTE"));

        List<Pedido> pedidos = pedidoRepository.findAll();

        Assertions.assertEquals(1, pedidos.size());

        Pedido pedido = pedidos.get(0);

        Assertions.assertEquals(Status.PENDENTE, pedido.getStatus());

        Mockito.verify(rabbitTemplate).convertAndSend(Mockito.anyString(), Mockito.any(PedidoRequestDto.class));
    }

    @DisplayName("Deve falhar ao criar pedido com JSON sem produtoId")
    @Test
    void deveFalharCriarPedidoSemProdutoId() throws Exception {

        var json = """
    {
        "quantidade": 1
    }
    """;

        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @DisplayName("Deve falhar ao criar pedido com quantidade zerada")
    @Test
    void deveFalharCriarPedidoQuantidadeZero() throws Exception {

        var json = """
    {
        "produtoId": 1,
        "quantidade": 0
    }
    """;

        mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}

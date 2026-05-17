package com.example.microservicos.pedido.service;

import com.example.microservicos.pedido.model.Pedido;
import com.example.microservicos.pedido.model.PedidoRequestDto;
import com.example.microservicos.pedido.model.PedidoResponseDto;
import com.example.microservicos.pedido.model.Status;
import com.example.microservicos.pedido.repository.PedidoRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;

    private final RabbitTemplate rabbitTemplate;


    public PedidoService(PedidoRepository pedidoRepository, RabbitTemplate rabbitTemplate) {
        this.pedidoRepository = pedidoRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public PedidoResponseDto getPedido(Long id){

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado"));

        return new PedidoResponseDto(pedido);
    }

    public PedidoResponseDto criarPedido(PedidoRequestDto dto){

        Pedido pedido = new Pedido();
        pedido.setProdutoId(dto.produtoId());
        pedido.setQuantidade(dto.quantidade());
        pedido.setStatus(Status.PENDENTE);

        pedidoRepository.save(pedido);

        PedidoRequestDto evento = new PedidoRequestDto(pedido);
        rabbitTemplate.convertAndSend("pedido.concluido", evento);

        return new PedidoResponseDto(pedido);
    }
}

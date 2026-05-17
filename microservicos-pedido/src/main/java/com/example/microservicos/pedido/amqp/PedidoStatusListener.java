package com.example.microservicos.pedido.amqp;

import com.example.microservicos.pedido.model.Pedido;
import com.example.microservicos.pedido.model.PedidoStatusDto;
import com.example.microservicos.pedido.repository.PedidoRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class PedidoStatusListener {

    private final PedidoRepository pedidoRepository;

    public PedidoStatusListener(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @RabbitListener(queues = "pedido.status")
    public void atualizaStatus(PedidoStatusDto dto){

        Pedido pedido = pedidoRepository.findById(dto.pedidoId()).orElseThrow();

        pedido.setStatus(dto.status());

        pedidoRepository.save(pedido);

        System.out.println("Pedido atualizado para " + dto.status());
    }
}

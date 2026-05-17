package com.example.microservicos.produto.amqp;

import com.example.microservicos.produto.model.PedidoRequestDto;
import com.example.microservicos.produto.model.PedidoStatusDto;
import com.example.microservicos.produto.model.Produto;
import com.example.microservicos.produto.model.Status;
import com.example.microservicos.produto.repository.ProdutoRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PedidoListener {

    private final ProdutoRepository produtoRepository;

    private final RabbitTemplate rabbitTemplate;

    public PedidoListener(ProdutoRepository produtoRepository, RabbitTemplate rabbitTemplate) {
        this.produtoRepository = produtoRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "pedido.concluido")
    public void recebeMensagem(@Payload PedidoRequestDto pedido) {

        Optional<Produto> produtoOptional = produtoRepository.findById(pedido.produtoId());

        if (produtoOptional.isEmpty()) {

            rabbitTemplate.convertAndSend("pedido.status",
                    new PedidoStatusDto(pedido.pedidoId(), Status.REJEITADO));

            return;
        }

        Produto produto = produtoOptional.get();

        if (produto.getEstoque() < pedido.quantidade()) {

            rabbitTemplate.convertAndSend("pedido.status",
                    new PedidoStatusDto(pedido.pedidoId(), Status.REJEITADO));

            return;
        }

        produto.setEstoque(produto.getEstoque() - pedido.quantidade());

        produtoRepository.save(produto);

        rabbitTemplate.convertAndSend("pedido.status",
                new PedidoStatusDto(pedido.pedidoId(), Status.APROVADO));
    }

    @RabbitListener(queues = "pedido.concluido.dlq")
    public void recebeMensagemDLQ(PedidoRequestDto pedido) {

        System.out.println("Mensagem enviada para DLQ");
    }
}
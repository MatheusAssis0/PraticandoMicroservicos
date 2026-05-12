package com.example.microservicos.pedido.service;

import com.example.microservicos.pedido.client.ProdutoClient;
import com.example.microservicos.pedido.model.*;
import com.example.microservicos.pedido.repository.PedidoRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;

    private final ProdutoServiceClient produtoServiceClient;

    public PedidoService(PedidoRepository pedidoRepository, ProdutoServiceClient produtoServiceClient) {
        this.pedidoRepository = pedidoRepository;
        this.produtoServiceClient = produtoServiceClient;
    }

    public PedidoResponseDto getPedido(Long id){

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado"));

        return new PedidoResponseDto(pedido);
    }

    public PedidoResponseDto criarPedido(PedidoRequestDto dto){

        ProdutoResponseDto produto = produtoServiceClient.getProduto(dto.produtoId());

        if(produto == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado");
        }

        if(produto.estoque() < dto.quantidade()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estoque insuficiente");
        }

        BigDecimal valorTotal = produto.preco().multiply(BigDecimal.valueOf(dto.quantidade()));

        produtoServiceClient.decreaseStock(dto.produtoId(), new DiminuirEstoqueDto(dto.quantidade()));

        Pedido pedido = new Pedido(null, produto.id(), dto.quantidade(),  valorTotal);

        pedidoRepository.save(pedido);

        return new PedidoResponseDto(pedido);
    }
}

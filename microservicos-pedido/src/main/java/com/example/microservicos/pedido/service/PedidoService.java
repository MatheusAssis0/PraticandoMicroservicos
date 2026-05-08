package com.example.microservicos.pedido.service;

import com.example.microservicos.pedido.client.ProdutoClient;
import com.example.microservicos.pedido.model.*;
import com.example.microservicos.pedido.repository.PedidoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;

    private final ProdutoClient produtoClient;

    public PedidoService(PedidoRepository pedidoRepository, ProdutoClient produtoClient) {
        this.pedidoRepository = pedidoRepository;
        this.produtoClient = produtoClient;
    }

    public PedidoResponseDto getPedido(Long id){

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado"));

        return new PedidoResponseDto(pedido);
    }

    public PedidoResponseDto criarPedido(PedidoRequestDto dto){

        ProdutoResponseDto produto = produtoClient.getProductById(dto.produtoId());

        if(produto == null){
            throw new RuntimeException("Produto não encontrado");
        }

        if(produto.estoque() < dto.quantidade()){
            throw new RuntimeException("Estoque insuficiente");
        }

        BigDecimal valorTotal = produto.preco().multiply(BigDecimal.valueOf(dto.quantidade()));

        produtoClient.decreaseStock(dto.produtoId(), new DiminuirEstoqueDto(dto.quantidade()));

        Pedido pedido = new Pedido(null, produto.id(), dto.quantidade(),  valorTotal);

        pedidoRepository.save(pedido);

        return new PedidoResponseDto(pedido);
    }
}

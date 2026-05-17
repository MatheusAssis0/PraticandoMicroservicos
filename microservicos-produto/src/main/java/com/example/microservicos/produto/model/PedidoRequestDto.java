package com.example.microservicos.produto.model;

public record PedidoRequestDto(Long pedidoId, Long produtoId, Integer quantidade) {
}

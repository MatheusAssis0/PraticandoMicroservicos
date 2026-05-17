package com.example.microservicos.pedido.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PedidoResponseDto(@NotNull Long id, @NotNull Long produtoId, @NotNull @Positive Integer quantidade,
                                @NotNull Status status) {
    public PedidoResponseDto(Pedido pedido){
        this(pedido.getId(), pedido.getProdutoId(), pedido.getQuantidade(), pedido.getStatus());
    }
}

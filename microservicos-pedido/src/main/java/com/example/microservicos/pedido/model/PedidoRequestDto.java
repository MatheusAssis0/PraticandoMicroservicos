package com.example.microservicos.pedido.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PedidoRequestDto(@NotNull Long produtoId, @NotNull @Positive Integer quantidade) {
    public PedidoRequestDto(Pedido pedido) {
        this(pedido.getProdutoId(), pedido.getQuantidade());
    }
}

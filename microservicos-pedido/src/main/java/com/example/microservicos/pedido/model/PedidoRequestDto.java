package com.example.microservicos.pedido.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PedidoRequestDto(Long pedidoId, @NotNull Long produtoId, @NotNull @Positive Integer quantidade) {
    public PedidoRequestDto(Pedido pedido) {
        this(pedido.getId(), pedido.getProdutoId(), pedido.getQuantidade());
    }
}

package com.example.microservicos.pedido.model;

import jakarta.validation.constraints.NotNull;

public record PedidoStatusDto(@NotNull Long pedidoId, Status status) {
}

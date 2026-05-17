package com.example.microservicos.produto.model;

import jakarta.validation.constraints.NotNull;

public record PedidoStatusDto(@NotNull Long pedidoId, Status status) {
}

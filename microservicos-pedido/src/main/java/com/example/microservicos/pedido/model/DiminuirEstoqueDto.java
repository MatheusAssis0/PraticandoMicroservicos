package com.example.microservicos.pedido.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DiminuirEstoqueDto(@Positive@NotNull Integer quantidade) {
}

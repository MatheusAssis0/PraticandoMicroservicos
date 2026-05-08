package com.example.microservicos.produto.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DiminuirEstoqueDto(@NotNull @Positive Integer quantidade) {
}

package com.example.microservicos.pedido.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ProdutoResponseDto(@NotNull Long id,
                                 @NotBlank String nome,
                                 @NotNull @Positive BigDecimal preco,
                                 @NotNull @PositiveOrZero Integer estoque) {
}

package com.example.microservicos.produto.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ProdutoDtoRequest(@NotBlank String nome, @Positive @NotNull BigDecimal preco, @PositiveOrZero @NotNull Integer estoque) {
    public ProdutoDtoRequest(Produto produto) {
        this(produto.getNome(), produto.getPreco(), produto.getEstoque());
    }
}

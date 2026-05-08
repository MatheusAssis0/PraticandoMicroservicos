package com.example.microservicos.pedido.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long produtoId;

    private Integer quantidade;

    private BigDecimal valorTotal;

    public Pedido(Long id, Long produtoId, Integer quantidade, BigDecimal valorTotal) {
        this.id = id;
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.valorTotal = valorTotal;
    }

    public Pedido() {
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public Long getId() {
        return id;
    }
}

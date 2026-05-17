package com.example.microservicos.pedido.model;

import jakarta.persistence.*;

@Entity
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long produtoId;

    private Integer quantidade;

    @Enumerated(EnumType.STRING)
    private Status status;

    public Pedido(Long id, Long produtoId, Integer quantidade, Status status) {
        this.id = id;
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.status = status;
    }

    public Pedido() {
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

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
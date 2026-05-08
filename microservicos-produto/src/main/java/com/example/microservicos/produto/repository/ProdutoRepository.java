package com.example.microservicos.produto.repository;

import com.example.microservicos.produto.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProdutoRepository  extends JpaRepository<Produto, Long> {
}

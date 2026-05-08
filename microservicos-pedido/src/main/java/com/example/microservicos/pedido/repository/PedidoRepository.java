package com.example.microservicos.pedido.repository;

import com.example.microservicos.pedido.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {


}

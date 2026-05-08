package com.example.microservicos.pedido;

import org.springframework.boot.SpringApplication;

public class TestMicroservicosPedidoApplication {

	public static void main(String[] args) {
		SpringApplication.from(MicroservicosPedidoApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}

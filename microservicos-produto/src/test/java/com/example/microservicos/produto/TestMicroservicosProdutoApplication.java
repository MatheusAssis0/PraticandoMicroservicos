package com.example.microservicos.produto;

import org.springframework.boot.SpringApplication;

public class TestMicroservicosProdutoApplication {

	public static void main(String[] args) {
		SpringApplication.from(MicroservicosProdutoApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}

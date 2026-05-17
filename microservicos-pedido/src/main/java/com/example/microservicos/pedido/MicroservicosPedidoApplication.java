package com.example.microservicos.pedido;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MicroservicosPedidoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroservicosPedidoApplication.class, args);
	}

}

package com.example.microservicos.produto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MicroservicosProdutoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroservicosProdutoApplication.class, args);
	}

}

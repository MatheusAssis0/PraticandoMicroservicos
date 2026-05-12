package com.example.microservicos.pedido.service;

import com.example.microservicos.pedido.client.ProdutoClient;
import com.example.microservicos.pedido.model.DiminuirEstoqueDto;
import com.example.microservicos.pedido.model.ProdutoResponseDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import feign.FeignException;

@Service
public class ProdutoServiceClient {

    private final ProdutoClient produtoClient;

    public ProdutoServiceClient(ProdutoClient produtoClient) {
        this.produtoClient = produtoClient;
    }

    @Retry(name = "produtoService")
    @CircuitBreaker(name = "produtoService", fallbackMethod = "fallbackGetProduto")
    public ProdutoResponseDto getProduto(Long id){

        System.out.println("Tentando buscar produto...");

        return produtoClient.getProductById(id);
    }

    @Retry(name = "produtoService")
    @CircuitBreaker(name = "produtoService", fallbackMethod = "fallbackDecreaseStock")
    public void decreaseStock(Long produtoId, DiminuirEstoqueDto quantidade){

        produtoClient.decreaseStock(produtoId,quantidade);
    }

    public ProdutoResponseDto fallbackGetProduto(Long id, Exception ex) {

        System.out.println("Fallback executado!");
        System.out.println(ex.getClass().getName());

        if (ex instanceof FeignException.NotFound) {

            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado");
        }

        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Serviço de produto indisponível");
    }


    public void fallbackDecreaseStock(Long produtoId, Integer quantidade, Exception ex) {

        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Não foi possível atualizar estoque");
    }
}

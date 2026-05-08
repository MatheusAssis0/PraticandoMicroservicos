package com.example.microservicos.pedido.client;

import com.example.microservicos.pedido.model.DiminuirEstoqueDto;
import com.example.microservicos.pedido.model.ProdutoResponseDto;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "microservicos-produto")
public interface ProdutoClient {

    @GetMapping("/produtos/{id}")
    ProdutoResponseDto getProductById(@PathVariable Long id);

    @PatchMapping("/produtos/{id}")
    void decreaseStock(@PathVariable Long id,
                       @Valid @RequestBody DiminuirEstoqueDto dto);
}

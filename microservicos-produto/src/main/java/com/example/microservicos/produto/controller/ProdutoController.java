package com.example.microservicos.produto.controller;

import com.example.microservicos.produto.model.DiminuirEstoqueDto;
import com.example.microservicos.produto.model.ProdutoDtoRequest;
import com.example.microservicos.produto.model.ProdutoDtoResponse;
import com.example.microservicos.produto.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoDtoResponse> buscar(@PathVariable Long id) {

        var response = produtoService.buscarProdutoPorId(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ProdutoDtoResponse> criarProduto(@Valid @RequestBody ProdutoDtoRequest produtoDto) {

        var response = produtoService.criarProduto(produtoDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProdutoDtoResponse> diminuirEstoque(@PathVariable Long id, @Valid @RequestBody DiminuirEstoqueDto produtoDto) {

        var response = produtoService.diminuirEstoque(id, produtoDto);
        return ResponseEntity.ok(response);
    }

}

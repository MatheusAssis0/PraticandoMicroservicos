package com.example.microservicos.produto.service;

import com.example.microservicos.produto.model.DiminuirEstoqueDto;
import com.example.microservicos.produto.model.Produto;
import com.example.microservicos.produto.model.ProdutoDtoRequest;
import com.example.microservicos.produto.model.ProdutoDtoResponse;
import com.example.microservicos.produto.repository.ProdutoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public ProdutoDtoResponse buscarProdutoPorId(Long id){

        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));

        return new ProdutoDtoResponse(produto);
    }

    public ProdutoDtoResponse criarProduto(ProdutoDtoRequest produtoDto){

        Produto produto = new Produto(null, produtoDto.nome(), produtoDto.preco(), produtoDto.estoque());
        Produto salvo = produtoRepository.save(produto);
        return new ProdutoDtoResponse(salvo);
    }

    public ProdutoDtoResponse diminuirEstoque(Long id, DiminuirEstoqueDto produtoDto) {

        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));

        if (produtoDto.quantidade() > produto.getEstoque()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estoque insuficiente");
        }

        produto.setEstoque(produto.getEstoque() - produtoDto.quantidade());

        produtoRepository.save(produto);

        return  new ProdutoDtoResponse(produto);
    }
}

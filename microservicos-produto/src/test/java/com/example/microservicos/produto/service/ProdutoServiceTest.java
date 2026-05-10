package com.example.microservicos.produto.service;

import com.example.microservicos.produto.model.DiminuirEstoqueDto;
import com.example.microservicos.produto.model.Produto;
import com.example.microservicos.produto.model.ProdutoDtoRequest;
import com.example.microservicos.produto.repository.ProdutoRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @InjectMocks
    private ProdutoService produtoService;

    @Mock
    private ProdutoRepository produtoRepository;

    @Captor
    private ArgumentCaptor<Produto> produtoArgumentCaptor;

    @DisplayName("Quando buscar um produto por ID")
    @Nested
    class BuscarPorId {
        @DisplayName("Então deve buscar com sucesso")
        @Nested
        class Sucesso {
            @DisplayName("Dado um ID válido")
            @Test
            void buscaPorIdSucesso() {

                Long id = 3L;
                Produto produto = new Produto(id, "Lapis", BigDecimal.valueOf(1.00), 10);
                when(produtoRepository.findById(id)).thenReturn(Optional.of(produto));

                var produtoDtoResponse = produtoService.buscarProdutoPorId(id);

                Assertions.assertEquals(produto.getId(), produtoDtoResponse.id());
                Assertions.assertEquals(produto.getNome(), produtoDtoResponse.nome());
                Assertions.assertEquals(produto.getPreco(), produtoDtoResponse.preco());
                Assertions.assertEquals(produto.getEstoque(), produtoDtoResponse.estoque());
            }
        }

        @DisplayName("Então deve falhar ao buscar")
        @Nested
        class Falha {
            @DisplayName("Dado um ID inválido")
            @Test
            void buscaPorIdFalha() {

                Long id = 3L;
                when(produtoRepository.findById(id)).thenReturn(Optional.empty());

                ResponseStatusException exception = Assertions.assertThrows(ResponseStatusException.class,
                        () -> produtoService.buscarProdutoPorId(id));

                Assertions.assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
                Assertions.assertEquals("Produto não encontrado", exception.getReason());
            }
        }
    }

    @DisplayName("Quando criar um produto")
    @Nested
    class Criar {
        @DisplayName("Então deve criar com sucesso")
        @Nested
        class Sucesso {
            @DisplayName("Dado um JSON preenchido corretamente")
            @Test
            void criarProdutoSucesso() {

                Produto produto = new Produto(null, "Lapis", BigDecimal.valueOf(1.00), 10);
                ProdutoDtoRequest produtoDtoRequest = new ProdutoDtoRequest(produto.getNome(), produto.getPreco(), produto.getEstoque());
                when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

                produtoService.criarProduto(produtoDtoRequest);

                then(produtoRepository).should().save(produtoArgumentCaptor.capture());

                Produto produtoSalvo = produtoArgumentCaptor.getValue();

                Assertions.assertEquals(produto.getNome(), produtoSalvo.getNome());
                Assertions.assertEquals(produto.getPreco(), produtoSalvo.getPreco());
                Assertions.assertEquals(produto.getEstoque(), produtoSalvo.getEstoque());
            }
        }
    }

    @DisplayName("Quando diminuir o estoque")
    @Nested
    class DiminuirEstoque {
        @DisplayName("Então deve diminuir com sucesso")
        @Nested
        class Sucesso {
            @DisplayName("Dado um ID válido")
            @Test
            void atualizarProdutoSucesso() {

                Long id = 1L;
                Produto produto = new Produto(id, "Lapis", BigDecimal.valueOf(1.00), 10);
                when(produtoRepository.findById(id)).thenReturn(Optional.of(produto));
                DiminuirEstoqueDto diminuirEstoqueDto = new DiminuirEstoqueDto(2);

                produtoService.diminuirEstoque(id, diminuirEstoqueDto);

                then(produtoRepository).should().save(produtoArgumentCaptor.capture());

                Assertions.assertEquals(8, produtoArgumentCaptor.getValue().getEstoque());
            }
        }
        @DisplayName("Então deve falhar")
        @Nested
        class Falha {
            @DisplayName("Dado um ID inválido")
            @Test
            void atualizarProdutoFalhaID() {

                Long id = 1L;
                when(produtoRepository.findById(id)).thenReturn(Optional.empty());
                DiminuirEstoqueDto diminuirEstoqueDto = new DiminuirEstoqueDto(2);

                ResponseStatusException exception = Assertions.assertThrows(ResponseStatusException.class, () -> produtoService.diminuirEstoque(id, diminuirEstoqueDto));
                Assertions.assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
                Assertions.assertEquals("Produto não encontrado", exception.getReason());
                then(produtoRepository).should(never()).save(any());

            }

            @DisplayName("Dado uma quantidade maior que o estoque")
            @Test
            void atualizarProdutoFalhaEstoqueInsuficiente() {

                Long id = 1L;
                Produto produto = new Produto(id, "Lapis", BigDecimal.valueOf(1.00), 10);
                when(produtoRepository.findById(id)).thenReturn(Optional.of(produto));
                DiminuirEstoqueDto diminuirEstoqueDto = new DiminuirEstoqueDto(11);

                ResponseStatusException exception = Assertions.assertThrows(ResponseStatusException.class, () -> produtoService.diminuirEstoque(id, diminuirEstoqueDto));
                Assertions.assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
                Assertions.assertEquals("Estoque insuficiente", exception.getReason());
                then(produtoRepository).should(never()).save(any());
            }
        }
    }

}


package com.example.microservicos.pedido.controller;

import com.example.microservicos.pedido.model.PedidoRequestDto;
import com.example.microservicos.pedido.model.PedidoResponseDto;
import com.example.microservicos.pedido.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDto> getPedido(@PathVariable Long id){

        var response = pedidoService.getPedido(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<PedidoResponseDto> criarPedido(@RequestBody @Valid PedidoRequestDto dto){

        var response = pedidoService.criarPedido(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

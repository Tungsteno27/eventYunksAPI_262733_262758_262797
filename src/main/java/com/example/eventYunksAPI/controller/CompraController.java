package com.example.eventYunksAPI.controller;

import com.example.eventYunksAPI.dto.CompraRequest;
import com.example.eventYunksAPI.dto.CompraResponse;
import com.example.eventYunksAPI.dto.PagoRequest;
import com.example.eventYunksAPI.dto.ResumenCompraResponse;
import com.example.eventYunksAPI.service.CompraService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/compras")
public class CompraController {

    @Autowired
    private CompraService compraService;

    @PostMapping("/resumen")
    public ResponseEntity<ResumenCompraResponse> resumen(@Valid @RequestBody CompraRequest request) {
        return ResponseEntity.ok(compraService.resumen(request));
    }

    @PostMapping
    public ResponseEntity<CompraResponse> crear(@RequestAttribute("usuarioId") Long usuarioId,
                                                @Valid @RequestBody CompraRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(compraService.crear(usuarioId, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompraResponse> obtener(@RequestAttribute("usuarioId") Long usuarioId,
                                                  @PathVariable Long id) {
        return ResponseEntity.ok(compraService.obtener(usuarioId, id));
    }

    @PostMapping("/{id}/pago")
    public ResponseEntity<CompraResponse> procesarPago(@RequestAttribute("usuarioId") Long usuarioId,
                                                       @PathVariable Long id,
                                                       @RequestBody(required = false) PagoRequest request) {
        return ResponseEntity.ok(compraService.procesarPago(usuarioId, id, request));
    }
}

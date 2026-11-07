package com.example.eventYunksAPI.controller;

import com.example.eventYunksAPI.dto.RegistroAsistenteRequest;
import com.example.eventYunksAPI.dto.SesionResponse;
import com.example.eventYunksAPI.service.AsistenteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/asistentes")
public class AsistenteController {

    @Autowired
    private AsistenteService asistenteService;

    @PostMapping
    public ResponseEntity<SesionResponse> registrar(@Valid @RequestBody RegistroAsistenteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(asistenteService.registrar(request));
    }
}
package com.example.eventYunksAPI.controller;

import com.example.eventYunksAPI.dto.LoginRequest;
import com.example.eventYunksAPI.dto.SesionResponse;
import com.example.eventYunksAPI.service.SesionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sesiones")
public class AuthController {

    @Autowired
    private SesionService sesionService;

    @PostMapping
    public ResponseEntity<SesionResponse> iniciarSesion(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(sesionService.iniciarSesion(request));
    }
}

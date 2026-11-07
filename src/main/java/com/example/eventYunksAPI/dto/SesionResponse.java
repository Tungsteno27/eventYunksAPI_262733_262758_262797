package com.example.eventYunksAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SesionResponse {

    private String token;
    private String tipo;
    private UsuarioResponse usuario;
}


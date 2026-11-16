package com.example.eventYunksAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class BoletoSeleccionadoResponse {

    private Long asientoId;
    private Long boletoId;
    private String tipo;
    private String fila;
    private Integer numero;
    private BigDecimal precio;
}

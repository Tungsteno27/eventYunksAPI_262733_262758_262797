package com.example.eventYunksAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class EventoResumenResponse {

    private Long id;
    private String nombre;
    private LocalDateTime fecha;
    private String urlImagen;
    private String estado;
    private String etiqueta;
    private String recinto;
    private String organizador;
    private BigDecimal precioDesde;
}

package com.example.eventYunksAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class ResumenCompraResponse {

    private Long eventoId;
    private String evento;
    private LocalDateTime fecha;
    private String recinto;
    private String direccion;
    private String organizador;
    private List<BoletoSeleccionadoResponse> boletos;
    private int cantidad;
    private BigDecimal subtotal;
    private BigDecimal total;
    private String metodoPago;
}

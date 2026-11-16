package com.example.eventYunksAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TicketResponse {

    private Long id;
    private String codigoQr;
    private String codigoVisible;
    private String tipoBoleto;
    private String estado;
    private String estadoPago;
    private Long eventoId;
    private String evento;
    private LocalDateTime fechaEvento;
    private String recinto;
    private String direccion;
    private String fila;
    private Integer asiento;
    private BigDecimal precio;
}

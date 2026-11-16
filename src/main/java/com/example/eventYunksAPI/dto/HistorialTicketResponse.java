package com.example.eventYunksAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class HistorialTicketResponse {

    private Long ticketId;
    private String evento;
    private LocalDateTime fechaCompra;
    private String tipoBoleto;
    private String ubicacion;
    private BigDecimal precioPagado;
    private String estado;
}

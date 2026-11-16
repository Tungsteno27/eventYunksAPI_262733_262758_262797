package com.example.eventYunksAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class CompraResponse {

    private Long id;
    private String folio;
    private LocalDateTime fechaCompra;
    private BigDecimal total;
    private String estadoPago;
    private String metodoPago;
    private String evento;
    private LocalDateTime fechaEvento;
    private int cantidadTickets;
    private List<TicketResponse> tickets;
}

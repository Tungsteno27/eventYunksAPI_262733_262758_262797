package com.example.eventYunksAPI.service;

import com.example.eventYunksAPI.dto.CompraResponse;
import com.example.eventYunksAPI.dto.TicketResponse;
import com.example.eventYunksAPI.model.Asiento;
import com.example.eventYunksAPI.model.Boleto;
import com.example.eventYunksAPI.model.Compra;
import com.example.eventYunksAPI.model.Evento;
import com.example.eventYunksAPI.model.Recinto;
import com.example.eventYunksAPI.model.Ticket;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class CompraMapper {

    public CompraResponse aCompra(Compra compra) {
        List<TicketResponse> tickets = compra.getTickets().stream().map(this::aTicket).toList();
        Evento evento = compra.getTickets().isEmpty() ? null : compra.getTickets().get(0).getBoleto().getEvento();
        return new CompraResponse(
                compra.getId(),
                folio(compra),
                compra.getFechaCompra(),
                compra.getTotal(),
                compra.getPago() == null ? null : compra.getPago().getEstado().name(),
                compra.getPago() == null ? null : compra.getPago().getMetodo(),
                evento == null ? null : evento.getNombre(),
                evento == null ? null : evento.getFecha(),
                tickets.size(),
                tickets
        );
    }

    public TicketResponse aTicket(Ticket ticket) {
        Boleto boleto = ticket.getBoleto();
        Evento evento = boleto.getEvento();
        Recinto recinto = evento.getRecinto();
        Asiento asiento = ticket.getAsiento();
        Compra compra = ticket.getCompra();
        return new TicketResponse(
                ticket.getId(),
                ticket.getCodigoQr(),
                codigoVisible(ticket),
                boleto.getTipo(),
                ticket.getEstado().name(),
                compra.getPago() == null ? null : compra.getPago().getEstado().name(),
                evento.getId(),
                evento.getNombre(),
                evento.getFecha(),
                recinto.getNombre(),
                recinto.getDireccion().getResumen(),
                asiento == null ? null : asiento.getFila(),
                asiento == null ? null : asiento.getNumero(),
                boleto.getPrecio()
        );
    }

    public String folio(Compra compra) {
        return String.format("EY-TXN-%s-%05d", compra.getFechaCompra().format(DateTimeFormatter.BASIC_ISO_DATE), compra.getId());
    }

    public String codigoVisible(Ticket ticket) {
        Boleto boleto = ticket.getBoleto();
        Asiento asiento = ticket.getAsiento();
        String inicial = boleto.getTipo() == null || boleto.getTipo().isBlank() ? "X" : boleto.getTipo().trim().substring(0, 1).toUpperCase();
        String ubicacion = asiento == null ? "" : "-" + asiento.getFila() + asiento.getNumero();
        return "EY-" + boleto.getEvento().getFecha().getYear() + "-" + inicial + ubicacion;
    }
}

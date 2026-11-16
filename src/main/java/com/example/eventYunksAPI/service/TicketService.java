package com.example.eventYunksAPI.service;

import com.example.eventYunksAPI.dto.HistorialTicketResponse;
import com.example.eventYunksAPI.dto.TicketResponse;
import com.example.eventYunksAPI.model.enums.EstadoEvento;
import com.example.eventYunksAPI.model.enums.EstadoPago;
import com.example.eventYunksAPI.model.enums.EstadoTicket;
import com.example.eventYunksAPI.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private DisponibilidadService disponibilidadService;

    @Autowired
    private CompraMapper compraMapper;

    @Transactional
    public List<TicketResponse> activos(Long usuarioId) {
        disponibilidadService.liberarVencidas();
        return ticketRepository.findActivos(
                        usuarioId,
                        EstadoTicket.VIGENTE,
                        EstadoPago.APROBADO,
                        List.of(EstadoEvento.CANCELADO, EstadoEvento.FINALIZADO),
                        LocalDate.now().atStartOfDay())
                .stream()
                .map(compraMapper::aTicket)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HistorialTicketResponse> historial(Long usuarioId) {
        return ticketRepository.findHistorial(usuarioId, EstadoPago.APROBADO)
                .stream()
                .map(compraMapper::aHistorial)
                .toList();
    }
}

package com.example.eventYunksAPI.controller;

import com.example.eventYunksAPI.dto.HistorialTicketResponse;
import com.example.eventYunksAPI.dto.TicketResponse;
import com.example.eventYunksAPI.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @GetMapping("/activos")
    public ResponseEntity<List<TicketResponse>> activos(@RequestAttribute("usuarioId") Long usuarioId) {
        return ResponseEntity.ok(ticketService.activos(usuarioId));
    }

    @GetMapping("/historial")
    public ResponseEntity<List<HistorialTicketResponse>> historial(@RequestAttribute("usuarioId") Long usuarioId) {
        return ResponseEntity.ok(ticketService.historial(usuarioId));
    }
}

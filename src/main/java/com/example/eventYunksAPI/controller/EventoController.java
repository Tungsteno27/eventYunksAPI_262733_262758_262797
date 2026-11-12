package com.example.eventYunksAPI.controller;

import com.example.eventYunksAPI.dto.EventoFiltroRequest;
import com.example.eventYunksAPI.dto.EventoResumenResponse;
import com.example.eventYunksAPI.service.EventoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {
    @Autowired
    private EventoService eventoService;


    //Para la pantalla de catalogo.html
    @GetMapping //Para probar este como tiene varios parametros estos deben de especificarse en la url
    public ResponseEntity<List<EventoResumenResponse>> listarEventos(@Valid EventoFiltroRequest filtros) {
        return ResponseEntity.ok(eventoService.catalogo(filtros.getBusqueda(), filtros.getPrecioMin(), filtros.getPrecioMax(), filtros.getFechaDesde()));
    }


}

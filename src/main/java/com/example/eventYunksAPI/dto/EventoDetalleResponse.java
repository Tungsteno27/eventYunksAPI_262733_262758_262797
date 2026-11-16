package com.example.eventYunksAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class EventoDetalleResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private String categoria;
    private LocalDateTime fecha;
    private String urlImagen;
    private String estado;
    private String etiqueta;
    private boolean ventaDisponible;
    private RecintoInfo recinto;
    private OrganizadorInfo organizador;
    private List<ArtistaInfo> artistas;
    private List<ZonaInfo> zonas;

    @Getter
    @AllArgsConstructor
    public static class RecintoInfo {
        private String nombre;
        private String direccion;
        private String ciudad;
        private Integer capacidad;
    }

    @Getter
    @AllArgsConstructor
    public static class OrganizadorInfo {
        private Long id;
        private String nombre;
        private int eventosRealizados;
    }

    @Getter
    @AllArgsConstructor
    public static class ArtistaInfo {
        private Long id;
        private String nombre;
        private String generoMusical;
        private String urlImagen;
        private String iniciales;
    }

    @Getter
    @AllArgsConstructor
    public static class ZonaInfo {
        private Long id;
        private String nombre;
        private Long boletoId;
        private BigDecimal precio;
        private String descripcionBoleto;
        private boolean enVenta;
        private int totalAsientos;
        private int disponibles;
        private List<FilaInfo> filas;
    }

    @Getter
    @AllArgsConstructor
    public static class FilaInfo {
        private String fila;
        private List<AsientoInfo> asientos;
    }

    @Getter
    @AllArgsConstructor
    public static class AsientoInfo {
        private Long id;
        private Integer numero;
        private String estado;
    }
}

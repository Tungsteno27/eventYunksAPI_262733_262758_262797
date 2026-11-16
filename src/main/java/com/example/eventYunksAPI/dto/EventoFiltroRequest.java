package com.example.eventYunksAPI.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EventoFiltroRequest {
    @Size(max = 100, message = "La búsqueda no puede pasar de 100 caracteres")
    private String busqueda;

    @Size(max = 100, message = "La categoría no puede pasar de 100 caracteres")
    private String categoria;

    @DecimalMin(value = "0", message = "El precio mínimo no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El precio mínimo no es válido (máximo 2 decimales)")
    private BigDecimal precioMin;

    @DecimalMin(value = "0", message = "El precio máximo no puede ser negativo")
    @Digits(integer = 8, fraction = 2, message = "El precio máximo no es válido (máximo 2 decimales)")
    private BigDecimal precioMax;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaDesde;
}

package com.example.eventYunksAPI.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CompraRequest {

    @NotNull(message = "El evento es obligatorio")
    private Long eventoId;

    @NotEmpty(message = "Selecciona al menos un asiento")
    private List<@NotNull(message = "El id del asiento no puede ser nulo") Long> asientoIds;
}

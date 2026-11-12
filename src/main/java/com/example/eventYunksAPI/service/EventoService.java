package com.example.eventYunksAPI.service;

import com.example.eventYunksAPI.dto.EventoResumenResponse;
import com.example.eventYunksAPI.exceptions.BadRequestException;
import com.example.eventYunksAPI.model.Evento;
import com.example.eventYunksAPI.model.enums.EstadoEvento;
import com.example.eventYunksAPI.repository.EventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class EventoService {

    @Autowired
    private EventoRepository eventoRepository;

    //Para la pantalla de catalogo.html, carga los eventos con sus filtros
    @Transactional(readOnly = true)
    public List<EventoResumenResponse> catalogo(String busqueda, BigDecimal precioMin, BigDecimal precioMax, LocalDate fechaDesde) {
        if (precioMin != null && precioMax != null && precioMin.compareTo(precioMax) > 0) {
            throw new BadRequestException("El precio mínimo no puede ser mayor que el máximo");
        }
        LocalDateTime fechaActual = LocalDateTime.now();
        String texto = busqueda == null ? "" : normalizar(busqueda);
        //Lo del estado not se refiere a cualquier evento que no esté en borrador (pues esos solamente se ven en administración)
        List<Evento> filtrados = eventoRepository
                .findByEstadoNot(EstadoEvento.BORRADOR)
                .stream()
                .filter(e -> !e.getFecha().isBefore(fechaActual))
                .filter(e -> coincideTexto(e, texto))
                .filter(e -> coincideFecha(e.getFecha(), fechaDesde))
                .filter(e -> coincidePrecio(e.getPrecioDesde(), precioMin, precioMax))
                //Se ordenan por la fecha
                .sorted(Comparator.comparing(Evento::getFecha))
                .toList();
        //Se regresa la lista ya mapeada al dto, para no enviar clases de dominio al controller emoji del pulgar arriba
        return filtrados.stream().map(this::aResumen).toList();
    }

    //Podría mapear todos los campos para construir el resumen, pero es más práctico así
    private EventoResumenResponse aResumen(Evento evento) {
        return new EventoResumenResponse(
                evento.getId(),
                evento.getNombre(),
                evento.getFecha(),
                evento.getUrlImagen(),
                evento.getEstado().name(),
                evento.getEstado().getEtiqueta(),
                evento.getRecinto().getNombre(),
                evento.getOrganizador().getNombre(),
                evento.getPrecioDesde()
        );
    }

    //Para la barra de búsqueda
    private boolean coincideTexto(Evento evento, String texto) {
        if (texto.isEmpty()) {
            return true;
        }
        return (evento.getNombre() != null && normalizar(evento.getNombre()).contains(texto)) || (evento.getCategoria() != null && normalizar(evento.getCategoria()).contains(texto))
                || (evento.getRecinto().getNombre() != null && normalizar(evento.getRecinto().getNombre()).contains(texto))
                || (evento.getOrganizador().getNombre() != null && normalizar(evento.getOrganizador().getNombre()).contains(texto));
    }

    private String normalizar(String texto) {
        //El trim() de toda la vida elimina los espacios en blanco,
        //Lo del Normalizer.Form.NFD es para separar las letras con acentos de sus acentos en dos caracteres diferentes
        //el replaceAll implementa una regex para eliminar los acentos
        // Y el toLowerCase() pues lo convierte a minuscula todo.
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase();
    }

    //Para el filtro de fecha
    private boolean coincideFecha(LocalDateTime fecha, LocalDate fechaDesde) {
        return fechaDesde == null || !fecha.toLocalDate().isBefore(fechaDesde);
    }

    //Para el filtro de precio
    private boolean coincidePrecio(BigDecimal desde, BigDecimal min, BigDecimal max) {
        if (min == null && max == null) {
            return true;
        }
        if (desde == null) {
            return false;
        }
        return (min == null || desde.compareTo(min) >= 0) && (max == null || desde.compareTo(max) <= 0);
    }
}

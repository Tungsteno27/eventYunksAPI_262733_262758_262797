package com.example.eventYunksAPI.service;

import com.example.eventYunksAPI.dto.EventoDetalleResponse;
import com.example.eventYunksAPI.dto.EventoResumenResponse;
import com.example.eventYunksAPI.exceptions.BadRequestException;
import com.example.eventYunksAPI.exceptions.ResourceNotFoundException;
import com.example.eventYunksAPI.model.Asiento;
import com.example.eventYunksAPI.model.Boleto;
import com.example.eventYunksAPI.model.Evento;
import com.example.eventYunksAPI.model.Recinto;
import com.example.eventYunksAPI.model.Zona;
import com.example.eventYunksAPI.model.enums.EstadoAsiento;
import com.example.eventYunksAPI.model.enums.EstadoEvento;
import com.example.eventYunksAPI.repository.EventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class EventoService {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private DisponibilidadService disponibilidadService;

    //Para la pantalla de catalogo.html, carga los eventos con sus filtros
    @Transactional(readOnly = true)
    public List<EventoResumenResponse> catalogo(String busqueda, String categoria, BigDecimal precioMin, BigDecimal precioMax, LocalDate fechaDesde) {
        if (precioMin != null && precioMax != null && precioMin.compareTo(precioMax) > 0) {
            throw new BadRequestException("El precio mínimo no puede ser mayor que el máximo");
        }
        String texto = busqueda == null ? "" : normalizar(busqueda);
        String textoCategoria = categoria == null ? "" : normalizar(categoria);
        //Lo del estado not se refiere a cualquier evento que no esté en borrador (pues esos solamente se ven en administración)
        List<Evento> filtrados = eventoRepository
                .findByEstadoNot(EstadoEvento.BORRADOR)
                .stream()
                .filter(e -> coincideTexto(e, texto))
                .filter(e -> textoCategoria.isEmpty() || (e.getCategoria() != null && normalizar(e.getCategoria()).equals(textoCategoria)))
                .filter(e -> coincideFecha(e.getFecha(), fechaDesde))
                .filter(e -> coincidePrecio(e.getPrecioDesde(), precioMin, precioMax))
                //Los eventos ya finalizados también se muestran (la pantalla del catálogo tiene su tarjeta "Finalizado"),
                //pero al final; los demás van por fecha, del más próximo al más lejano
                .sorted(Comparator.comparing((Evento e) -> disponibilidadService.estadoVisible(e) == EstadoEvento.FINALIZADO)
                        .thenComparing(Evento::getFecha))
                .toList();
        //Se regresa la lista ya mapeada al dto, para no enviar clases de dominio al controller emoji del pulgar arriba
        return filtrados.stream().map(this::aResumen).toList();
    }

    //Para el filtro de categoría de catalogo.html
    @Transactional(readOnly = true)
    public List<String> categorias() {
        return eventoRepository.findByEstadoNot(EstadoEvento.BORRADOR).stream()
                .map(Evento::getCategoria)
                .filter(c -> c != null && !c.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    //Podría mapear todos los campos para construir el resumen, pero es más práctico así
    private EventoResumenResponse aResumen(Evento evento) {
        return new EventoResumenResponse(
                evento.getId(),
                evento.getNombre(),
                evento.getFecha(),
                evento.getUrlImagen(),
                disponibilidadService.estadoVisible(evento).name(),
                disponibilidadService.estadoVisible(evento).getEtiqueta(),
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

    @Transactional
    public EventoDetalleResponse detalle(Long id) {
        disponibilidadService.liberarVencidas();
        Evento evento = eventoRepository.findById(id)
                .filter(e -> e.getEstado() != EstadoEvento.BORRADOR)
                .orElseThrow(() -> new ResourceNotFoundException("El evento no existe"));
        boolean ventaDisponible = disponibilidadService.ventaDisponible(evento);
        Recinto recinto = evento.getRecinto();

        EventoDetalleResponse.RecintoInfo recintoInfo = new EventoDetalleResponse.RecintoInfo(
                recinto.getNombre(),
                recinto.getDireccion().getResumen(),
                recinto.getDireccion().getCiudad(),
                recinto.getCroquis().getCapacidad());

        int eventosRealizados = (int) evento.getOrganizador().getEventos().stream()
                .filter(e -> e.getEstado() == EstadoEvento.FINALIZADO)
                .count();
        EventoDetalleResponse.OrganizadorInfo organizadorInfo = new EventoDetalleResponse.OrganizadorInfo(
                evento.getOrganizador().getId(),
                evento.getOrganizador().getNombre(),
                eventosRealizados);

        List<EventoDetalleResponse.ArtistaInfo> artistas = evento.getArtistas().stream()
                .map(a -> new EventoDetalleResponse.ArtistaInfo(
                        a.getId(), a.getNombre(), a.getGeneroMusical(), a.getUrlImagen(), a.getIniciales()))
                .toList();

        List<EventoDetalleResponse.ZonaInfo> zonas = ventaDisponible ? armarZonas(evento) : List.of();

        return new EventoDetalleResponse(
                evento.getId(),
                evento.getNombre(),
                evento.getDescripcion(),
                evento.getCategoria(),
                evento.getFecha(),
                evento.getUrlImagen(),
                disponibilidadService.estadoVisible(evento).name(),
                disponibilidadService.estadoVisible(evento).getEtiqueta(),
                ventaDisponible,
                recintoInfo,
                organizadorInfo,
                artistas,
                zonas
        );
    }

    private List<EventoDetalleResponse.ZonaInfo> armarZonas(Evento evento) {
        Map<Long, EstadoAsiento> ocupados = disponibilidadService.estadosPorEvento(evento.getId());
        List<Zona> zonasOrdenadas = evento.getRecinto().getCroquis().getZonas().stream()
                .sorted(Comparator.comparing(Zona::getId))
                .toList();
        List<EventoDetalleResponse.ZonaInfo> resultado = new ArrayList<>();
        for (Zona zona : zonasOrdenadas) {
            Boleto boleto = disponibilidadService.boletoDeZona(evento.getBoletos(), zona);
            // Cupo que queda del boleto (null = sin límite). Si se agotó, los asientos libres de la zona no se pueden comprar
            Integer cupo = boleto == null ? null : disponibilidadService.cupoRestante(boleto);
            boolean agotado = cupo != null && cupo <= 0;
            int libres = 0;
            List<EventoDetalleResponse.FilaInfo> filas = new ArrayList<>();
            for (Map.Entry<String, List<Asiento>> fila : zona.getAsientosPorFila().entrySet()) {
                List<EventoDetalleResponse.AsientoInfo> asientos = new ArrayList<>();
                for (Asiento asiento : fila.getValue()) {
                    String estado;
                    if (boleto == null) {
                        estado = "NO_DISPONIBLE";
                    } else {
                        EstadoAsiento estadoAsiento = ocupados.getOrDefault(asiento.getId(), EstadoAsiento.DISPONIBLE);
                        if (estadoAsiento == EstadoAsiento.DISPONIBLE && agotado) {
                            estado = "NO_DISPONIBLE";
                        } else {
                            if (estadoAsiento == EstadoAsiento.DISPONIBLE) {
                                libres++;
                            }
                            estado = estadoAsiento.name();
                        }
                    }
                    asientos.add(new EventoDetalleResponse.AsientoInfo(asiento.getId(), asiento.getNumero(), estado));
                }
                filas.add(new EventoDetalleResponse.FilaInfo(fila.getKey(), asientos));
            }
            int disponibles = cupo == null ? libres : Math.min(libres, cupo);
            resultado.add(new EventoDetalleResponse.ZonaInfo(
                    zona.getId(),
                    zona.getNombre(),
                    boleto == null ? null : boleto.getId(),
                    boleto == null ? null : boleto.getPrecio(),
                    boleto == null ? null : boleto.getDescripcion(),
                    boleto != null,
                    zona.getAsientos().size(),
                    disponibles,
                    filas));
        }
        return resultado;
    }
}

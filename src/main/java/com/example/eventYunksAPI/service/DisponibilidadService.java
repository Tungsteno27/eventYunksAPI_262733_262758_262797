package com.example.eventYunksAPI.service;

import com.example.eventYunksAPI.model.Boleto;
import com.example.eventYunksAPI.model.Compra;
import com.example.eventYunksAPI.model.Ticket;
import com.example.eventYunksAPI.model.Zona;
import com.example.eventYunksAPI.model.enums.EstadoAsiento;
import com.example.eventYunksAPI.model.enums.EstadoEvento;
import com.example.eventYunksAPI.model.enums.EstadoPago;
import com.example.eventYunksAPI.model.enums.EstadoTicket;
import com.example.eventYunksAPI.model.Evento;
import com.example.eventYunksAPI.repository.CompraRepository;
import com.example.eventYunksAPI.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DisponibilidadService {

    public static final List<EstadoPago> ESTADOS_QUE_OCUPAN = List.of(EstadoPago.PENDIENTE, EstadoPago.APROBADO);

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private CompraRepository compraRepository;

    @Value("${compra.minutos-apartado:10}")
    private long minutosApartado;

    public boolean ventaDisponible(Evento evento) {
        return evento.getEstado() == EstadoEvento.PUBLICADO && evento.getFecha().isAfter(LocalDateTime.now());
    }

    /**
     * Busca el boleto que corresponde a una zona. No hay relación directa en el modelo, así que se empareja
     * el nombre de la zona con el tipo del boleto (sin importar mayúsculas, acentos ni espacios sobrantes).
     * Si hubiera dos boletos con el mismo tipo se usa siempre el de menor id, para que el resultado sea estable.
     * @return el boleto de la zona, o null si el evento no tiene un boleto con ese tipo
     */
    public Boleto boletoDeZona(Collection<Boleto> boletos, Zona zona) {
        String nombreZona = normalizar(zona.getNombre());
        return boletos.stream()
                .filter(b -> b.getTipo() != null && normalizar(b.getTipo()).equals(nombreZona))
                .min(Comparator.comparing(Boleto::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElse(null);
    }

    /**
     * Estado del evento tal como se le muestra al cliente. Un evento publicado o pausado cuya fecha ya pasó
     * se muestra como FINALIZADO aunque el organizador no lo haya cerrado, para que el catálogo y el detalle
     * coincidan con las pantallas de evento finalizado y con la regla de venta.
     */
    public EstadoEvento estadoVisible(Evento evento) {
        EstadoEvento estado = evento.getEstado();
        boolean yaOcurrio = !evento.getFecha().isAfter(LocalDateTime.now());
        if (yaOcurrio && (estado == EstadoEvento.PUBLICADO || estado == EstadoEvento.PAUSADO)) {
            return EstadoEvento.FINALIZADO;
        }
        return estado;
    }

    /**
     * Boletos que todavía se pueden vender de un tipo (cupo total menos tickets apartados o vendidos).
     * @return el cupo restante, o null si el boleto no tiene cupo definido (sin límite)
     */
    @Transactional(readOnly = true)
    public Integer cupoRestante(Boleto boleto) {
        if (boleto.getCantidadTotal() == null) {
            return null;
        }
        long ocupados = ticketRepository.contarOcupadosPorBoleto(boleto.getId(), EstadoTicket.CANCELADO, ESTADOS_QUE_OCUPAN);
        return (int) Math.max(0, boleto.getCantidadTotal() - ocupados);
    }

    @Transactional
    public void liberarVencidas() {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(Math.max(1, minutosApartado));
        for (Compra compra : compraRepository.findPendientesAntesDe(EstadoPago.PENDIENTE, limite)) {
            compra.getPago().setEstado(EstadoPago.RECHAZADO);
            compra.getTickets().forEach(t -> t.setEstado(EstadoTicket.CANCELADO));
        }
    }

    @Transactional(readOnly = true)
    public Map<Long, EstadoAsiento> estadosPorEvento(Long eventoId) {
        Map<Long, EstadoAsiento> estados = new HashMap<>();
        List<Ticket> tickets = ticketRepository.findOcupadosPorEvento(eventoId, EstadoTicket.CANCELADO, ESTADOS_QUE_OCUPAN);
        for (Ticket ticket : tickets) {
            EstadoAsiento estado = ticket.getCompra().getPago().getEstado() == EstadoPago.APROBADO
                    ? EstadoAsiento.VENDIDO
                    : EstadoAsiento.APARTADO;
            estados.merge(ticket.getAsiento().getId(), estado,
                    (actual, nuevo) -> actual == EstadoAsiento.VENDIDO || nuevo == EstadoAsiento.VENDIDO
                            ? EstadoAsiento.VENDIDO
                            : EstadoAsiento.APARTADO);
        }
        return estados;
    }

    private String normalizar(String texto) {
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase();
    }
}

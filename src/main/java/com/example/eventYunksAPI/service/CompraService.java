package com.example.eventYunksAPI.service;

import com.example.eventYunksAPI.dto.BoletoSeleccionadoResponse;
import com.example.eventYunksAPI.dto.CompraRequest;
import com.example.eventYunksAPI.dto.CompraResponse;
import com.example.eventYunksAPI.dto.PagoRequest;
import com.example.eventYunksAPI.dto.ResumenCompraResponse;
import com.example.eventYunksAPI.exceptions.BadRequestException;
import com.example.eventYunksAPI.exceptions.ConflictException;
import com.example.eventYunksAPI.exceptions.ForbiddenException;
import com.example.eventYunksAPI.exceptions.ResourceNotFoundException;
import com.example.eventYunksAPI.exceptions.UnauthorizedException;
import com.example.eventYunksAPI.model.Asiento;
import com.example.eventYunksAPI.model.Asistente;
import com.example.eventYunksAPI.model.Boleto;
import com.example.eventYunksAPI.model.Compra;
import com.example.eventYunksAPI.model.Evento;
import com.example.eventYunksAPI.model.Pago;
import com.example.eventYunksAPI.model.Ticket;
import com.example.eventYunksAPI.model.enums.EstadoAsiento;
import com.example.eventYunksAPI.model.enums.EstadoEvento;
import com.example.eventYunksAPI.model.enums.EstadoPago;
import com.example.eventYunksAPI.model.enums.EstadoTicket;
import com.example.eventYunksAPI.repository.AsientoRepository;
import com.example.eventYunksAPI.repository.AsistenteRepository;
import com.example.eventYunksAPI.repository.BoletoRepository;
import com.example.eventYunksAPI.repository.CompraRepository;
import com.example.eventYunksAPI.repository.EventoRepository;
import com.example.eventYunksAPI.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CompraService {

    public static final String METODO_PAGO = "Tarjeta •••• 4242";

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private AsientoRepository asientoRepository;

    @Autowired
    private BoletoRepository boletoRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private AsistenteRepository asistenteRepository;

    @Autowired
    private DisponibilidadService disponibilidadService;

    @Autowired
    private CompraMapper compraMapper;

    private record Linea(Asiento asiento, Boleto boleto) {
    }

    private record Seleccion(Evento evento, List<Linea> lineas, BigDecimal total) {
    }

    @Transactional
    public ResumenCompraResponse resumen(CompraRequest request) {
        Seleccion seleccion = validar(request, false);
        Evento evento = seleccion.evento();
        List<BoletoSeleccionadoResponse> boletos = seleccion.lineas().stream()
                .map(l -> new BoletoSeleccionadoResponse(
                        l.asiento().getId(),
                        l.boleto().getId(),
                        l.boleto().getTipo(),
                        l.asiento().getFila(),
                        l.asiento().getNumero(),
                        l.boleto().getPrecio()))
                .toList();
        return new ResumenCompraResponse(
                evento.getId(),
                evento.getNombre(),
                evento.getFecha(),
                evento.getRecinto().getNombre(),
                evento.getRecinto().getDireccion().getResumen(),
                evento.getOrganizador().getNombre(),
                boletos,
                boletos.size(),
                seleccion.total(),
                seleccion.total(),
                METODO_PAGO
        );
    }

    // READ_COMMITTED: tras esperar el bloqueo de los boletos, la lectura de asientos ocupados ve lo que
    // otra compra concurrente ya confirmó (con REPEATABLE READ de MySQL vería una foto vieja y permitiría doble venta)
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CompraResponse crear(Long usuarioId, CompraRequest request) {
        Asistente asistente = asistenteRepository.findById(usuarioId)
                .orElseThrow(() -> new UnauthorizedException("La sesión no es válida"));
        if (!Boolean.TRUE.equals(asistente.getActivo())) {
            throw new ForbiddenException("La cuenta fue suspendida");
        }
        Seleccion seleccion = validar(request, true);

        Compra compra = new Compra();
        compra.setAsistente(asistente);
        compra.setTotal(seleccion.total());

        Pago pago = new Pago();
        pago.setMonto(seleccion.total());
        pago.setMetodo(METODO_PAGO);
        pago.setEstado(EstadoPago.PENDIENTE);
        pago.setCompra(compra);
        compra.setPago(pago);

        for (Linea linea : seleccion.lineas()) {
            Ticket ticket = new Ticket();
            ticket.setBoleto(linea.boleto());
            ticket.setAsiento(linea.asiento());
            ticket.setEstado(EstadoTicket.VIGENTE);
            compra.agregarTicket(ticket);
        }
        return compraMapper.aCompra(compraRepository.save(compra));
    }

    @Transactional
    public CompraResponse obtener(Long usuarioId, Long compraId) {
        disponibilidadService.liberarVencidas();
        Compra compra = compraRepository.findByIdAndAsistente_Id(compraId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("La compra no existe"));
        return compraMapper.aCompra(compra);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public CompraResponse procesarPago(Long usuarioId, Long compraId, PagoRequest request) {
        // Se bloquea la compra primero: si llegan dos pagos a la vez, el segundo espera y ve el estado final
        Compra compra = compraRepository.bloquearPorIdYAsistente(compraId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("La compra no existe"));
        disponibilidadService.liberarVencidas();
        Pago pago = compra.getPago();
        if (pago.getEstado() == EstadoPago.APROBADO) {
            throw new ConflictException("El pago de esta compra ya fue aprobado");
        }
        if (pago.getEstado() == EstadoPago.RECHAZADO) {
            throw new ConflictException("La compra fue cancelada y sus asientos ya fueron liberados");
        }
        boolean simularError = request != null && Boolean.TRUE.equals(request.getSimularError());
        // Si mientras tanto el evento se canceló, se pausó o ya pasó, el cobro no se aprueba (pantalla pago-error)
        boolean eventoSinVenta = !compra.getTickets().isEmpty()
                && !disponibilidadService.ventaDisponible(compra.getTickets().get(0).getBoleto().getEvento());
        if (simularError || eventoSinVenta) {
            pago.setEstado(EstadoPago.RECHAZADO);
            compra.getTickets().forEach(t -> t.setEstado(EstadoTicket.CANCELADO));
        } else {
            pago.setEstado(EstadoPago.APROBADO);
        }
        return compraMapper.aCompra(compra);
    }

    private Seleccion validar(CompraRequest request, boolean bloquear) {
        disponibilidadService.liberarVencidas();

        Evento evento = eventoRepository.findById(request.getEventoId())
                .filter(e -> e.getEstado() != EstadoEvento.BORRADOR)
                .orElseThrow(() -> new ResourceNotFoundException("El evento no existe"));
        if (!disponibilidadService.ventaDisponible(evento)) {
            throw new ConflictException("El evento no tiene venta de boletos disponible");
        }

        Set<Long> ids = new LinkedHashSet<>(request.getAsientoIds());
        if (ids.size() != request.getAsientoIds().size()) {
            throw new BadRequestException("Hay asientos repetidos en la selección");
        }

        List<Boleto> boletos = bloquear ? boletoRepository.bloquearPorEvento(evento.getId()) : evento.getBoletos();

        List<Asiento> encontrados = asientoRepository.findConZonaPorIds(ids);
        if (encontrados.size() != ids.size()) {
            throw new ResourceNotFoundException("Alguno de los asientos no existe");
        }
        Map<Long, Asiento> asientosPorId = encontrados.stream().collect(Collectors.toMap(Asiento::getId, a -> a));

        Long croquisId = evento.getRecinto().getCroquis().getId();
        Map<Long, EstadoAsiento> ocupados = disponibilidadService.estadosPorEvento(evento.getId());

        List<Linea> lineas = new ArrayList<>();
        Map<Long, Integer> pedidosPorBoleto = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Long id : ids) {
            Asiento asiento = asientosPorId.get(id);
            if (!asiento.getZona().getCroquis().getId().equals(croquisId)) {
                throw new BadRequestException("Alguno de los asientos no pertenece al recinto del evento");
            }
            Boleto boleto = disponibilidadService.boletoDeZona(boletos, asiento.getZona());
            if (boleto == null) {
                throw new BadRequestException("La zona " + asiento.getZona().getNombre() + " no tiene boletos a la venta");
            }
            if (ocupados.containsKey(id)) {
                throw new ConflictException("El asiento " + asiento.getFila() + "-" + asiento.getNumero() + " ya no está disponible");
            }
            pedidosPorBoleto.merge(boleto.getId(), 1, Integer::sum);
            lineas.add(new Linea(asiento, boleto));
            total = total.add(boleto.getPrecio());
        }

        for (Boleto boleto : boletos) {
            Integer pedidos = pedidosPorBoleto.get(boleto.getId());
            if (pedidos == null || boleto.getCantidadTotal() == null) {
                continue;
            }
            long ocupadosDelBoleto = ticketRepository.contarOcupadosPorBoleto(
                    boleto.getId(), EstadoTicket.CANCELADO, DisponibilidadService.ESTADOS_QUE_OCUPAN);
            if (ocupadosDelBoleto + pedidos > boleto.getCantidadTotal()) {
                throw new ConflictException("Ya no hay boletos disponibles de tipo " + boleto.getTipo());
            }
        }
        // Pago.monto es @Positive: se rechaza aquí para que el resumen y la compra respondan igual
        if (total.signum() <= 0) {
            throw new BadRequestException("El total de la compra debe ser mayor a 0");
        }
        return new Seleccion(evento, lineas, total);
    }
}

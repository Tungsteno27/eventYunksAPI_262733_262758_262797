package com.example.eventYunksAPI.repository;

import com.example.eventYunksAPI.model.Ticket;
import com.example.eventYunksAPI.model.enums.EstadoEvento;
import com.example.eventYunksAPI.model.enums.EstadoPago;
import com.example.eventYunksAPI.model.enums.EstadoTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("SELECT t FROM Ticket t JOIN FETCH t.compra c JOIN FETCH c.pago p JOIN FETCH t.asiento a " +
            "WHERE t.boleto.evento.id = :eventoId AND t.estado <> :excluido AND p.estado IN :estadosPago")
    List<Ticket> findOcupadosPorEvento(@Param("eventoId") Long eventoId,
                                       @Param("excluido") EstadoTicket excluido,
                                       @Param("estadosPago") Collection<EstadoPago> estadosPago);

    @Query("SELECT COUNT(t) FROM Ticket t JOIN t.compra c JOIN c.pago p " +
            "WHERE t.boleto.id = :boletoId AND t.estado <> :excluido AND p.estado IN :estadosPago")
    long contarOcupadosPorBoleto(@Param("boletoId") Long boletoId,
                                 @Param("excluido") EstadoTicket excluido,
                                 @Param("estadosPago") Collection<EstadoPago> estadosPago);

    @Query("SELECT t FROM Ticket t JOIN FETCH t.compra c JOIN FETCH c.pago p JOIN FETCH t.boleto b " +
            "JOIN FETCH b.evento e JOIN FETCH e.recinto r JOIN FETCH r.direccion JOIN FETCH t.asiento a " +
            "WHERE c.asistente.id = :asistenteId AND t.estado = :estado AND p.estado = :estadoPago " +
            "AND e.estado NOT IN :estadosEventoExcluidos AND e.fecha >= :desde " +
            "ORDER BY e.fecha ASC, t.id ASC")
    List<Ticket> findActivos(@Param("asistenteId") Long asistenteId,
                             @Param("estado") EstadoTicket estado,
                             @Param("estadoPago") EstadoPago estadoPago,
                             @Param("estadosEventoExcluidos") Collection<EstadoEvento> estadosEventoExcluidos,
                             @Param("desde") LocalDateTime desde);

    @Query("SELECT t FROM Ticket t JOIN FETCH t.compra c JOIN FETCH c.pago p JOIN FETCH t.boleto b " +
            "JOIN FETCH b.evento e LEFT JOIN FETCH t.asiento a " +
            "WHERE c.asistente.id = :asistenteId AND p.estado = :estadoPago " +
            "ORDER BY c.fechaCompra DESC, t.id ASC")
    List<Ticket> findHistorial(@Param("asistenteId") Long asistenteId,
                               @Param("estadoPago") EstadoPago estadoPago);
}

package com.example.eventYunksAPI.repository;

import com.example.eventYunksAPI.model.Ticket;
import com.example.eventYunksAPI.model.enums.EstadoPago;
import com.example.eventYunksAPI.model.enums.EstadoTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}

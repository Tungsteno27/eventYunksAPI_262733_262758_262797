package com.example.eventYunksAPI.repository;

import com.example.eventYunksAPI.model.Compra;
import com.example.eventYunksAPI.model.enums.EstadoPago;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {

    Optional<Compra> findByIdAndAsistente_Id(Long id, Long asistenteId);

    /** Igual que findByIdAndAsistente_Id pero con bloqueo de escritura, para que dos pagos de la misma compra no se pisen. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Compra c WHERE c.id = :id AND c.asistente.id = :asistenteId")
    Optional<Compra> bloquearPorIdYAsistente(@Param("id") Long id, @Param("asistenteId") Long asistenteId);

    @Query("SELECT c FROM Compra c JOIN FETCH c.pago p WHERE p.estado = :estado AND c.fechaCompra < :limite")
    List<Compra> findPendientesAntesDe(@Param("estado") EstadoPago estado, @Param("limite") LocalDateTime limite);
}

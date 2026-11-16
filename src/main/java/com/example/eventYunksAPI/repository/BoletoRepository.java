package com.example.eventYunksAPI.repository;

import com.example.eventYunksAPI.model.Boleto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoletoRepository extends JpaRepository<Boleto, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Boleto b WHERE b.evento.id = :eventoId ORDER BY b.id")
    List<Boleto> bloquearPorEvento(@Param("eventoId") Long eventoId);
}

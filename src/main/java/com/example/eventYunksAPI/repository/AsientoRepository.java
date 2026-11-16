package com.example.eventYunksAPI.repository;

import com.example.eventYunksAPI.model.Asiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface AsientoRepository extends JpaRepository<Asiento, Long> {

    @Query("SELECT a FROM Asiento a JOIN FETCH a.zona z JOIN FETCH z.croquis WHERE a.id IN :ids")
    List<Asiento> findConZonaPorIds(@Param("ids") Collection<Long> ids);
}

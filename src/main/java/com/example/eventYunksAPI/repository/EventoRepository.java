package com.example.eventYunksAPI.repository;

import com.example.eventYunksAPI.model.Evento;
import com.example.eventYunksAPI.model.enums.EstadoEvento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventoRepository extends JpaRepository<Evento, Long> {

    //Para la pantalla de catalogo.html
    @EntityGraph(attributePaths = {"recinto", "organizador", "boletos"})
    List<Evento> findByEstadoNot(EstadoEvento estado);
}

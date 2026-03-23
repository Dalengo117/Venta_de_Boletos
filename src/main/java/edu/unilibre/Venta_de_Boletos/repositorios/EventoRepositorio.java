package edu.unilibre.Venta_de_Boletos.repositorios;

import edu.unilibre.Venta_de_Boletos.entidades.Evento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventoRepositorio extends JpaRepository<Evento, Long> {

    // Buscar eventos por fecha (los más próximos primero)
    List<Evento> findAllByOrderByFechaAsc();

    // Buscar eventos futuros (opcional, según necesidad)
    List<Evento> findByFechaGreaterThanEqualOrderByFechaAsc(LocalDate fecha);

    // Buscar eventos por nombre (para búsquedas)
    List<Evento> findByNombreContainingIgnoreCase(String nombre);
}
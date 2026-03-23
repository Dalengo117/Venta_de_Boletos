package edu.unilibre.Venta_de_Boletos.repositorios;

import edu.unilibre.Venta_de_Boletos.entidades.Compra;
import edu.unilibre.Venta_de_Boletos.entidades.EstadoCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CompraRepositorio extends JpaRepository<Compra, Long> {

    // Buscar compras por comprador
    List<Compra> findByCompradorIdentificacionOrderByFechaReservaDesc(String identificacion);

    // Buscar compras por estado
    List<Compra> findByEstado(EstadoCompra estado);

    // Buscar compras expiradas (reservadas con fecha expiracion anterior a ahora)
    @Query("SELECT c FROM Compra c WHERE c.estado = :estado AND c.fechaExpiracion < :ahora")
    List<Compra> findReservasExpiradas(@Param("estado") EstadoCompra estado, @Param("ahora") LocalDateTime ahora);

    // Buscar compras por evento
    List<Compra> findByEventoId(Long eventoId);
}
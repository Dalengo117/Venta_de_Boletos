package edu.unilibre.Venta_de_Boletos.servicios;

import edu.unilibre.Venta_de_Boletos.entidades.Compra;
import edu.unilibre.Venta_de_Boletos.entidades.EstadoCompra;
import edu.unilibre.Venta_de_Boletos.entidades.ItemCompra;
import edu.unilibre.Venta_de_Boletos.entidades.Zona;
import edu.unilibre.Venta_de_Boletos.repositorios.CompraRepositorio;
import edu.unilibre.Venta_de_Boletos.repositorios.ZonaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@EnableScheduling
@Transactional
public class LiberacionReservasServicio {

    @Autowired
    private CompraRepositorio compraRepositorio;

    @Autowired
    private ZonaRepositorio zonaRepositorio;

    @Value("${reservas.tiempo-expiracion-horas:24}")
    private int tiempoExpiracionHoras;

    private static final DateTimeFormatter LOG_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Proceso automático que se ejecuta cada hora según configuración
     * Se ejecuta 1 hora después de iniciar la aplicación
     */
    @Scheduled(initialDelay = 3600000, fixedDelayString = "${reservas.intervalo-ejecucion-horas:1}0000")
    public void ejecutarLiberacionAutomatica() {
        System.out.println("[" + LocalDateTime.now().format(LOG_FORMATTER) + "] INICIO: Ejecutando proceso de liberación de reservas vencidas");

        try {
            List<Compra> reservasExpiradas = compraRepositorio.findReservasExpiradas(
                    EstadoCompra.RESERVADA,
                    LocalDateTime.now()
            );

            if (reservasExpiradas.isEmpty()) {
                System.out.println("[" + LocalDateTime.now().format(LOG_FORMATTER) + "] No hay reservas vencidas para liberar");
                return;
            }

            System.out.println("[" + LocalDateTime.now().format(LOG_FORMATTER) + "] Se encontraron " +
                    reservasExpiradas.size() + " reservas vencidas");

            int liberadas = 0;
            for (Compra compra : reservasExpiradas) {
                try {
                    liberarReserva(compra);
                    liberadas++;
                    System.out.println("[" + LocalDateTime.now().format(LOG_FORMATTER) +
                            "] ✅ Reserva #" + compra.getId() + " cancelada y boletas liberadas");
                } catch (Exception e) {
                    System.err.println("[" + LocalDateTime.now().format(LOG_FORMATTER) +
                            "] ❌ Error al liberar reserva #" + compra.getId() + ": " + e.getMessage());
                }
            }

            System.out.println("[" + LocalDateTime.now().format(LOG_FORMATTER) +
                    "] FIN: Proceso completado. Reservas liberadas: " + liberadas + "/" + reservasExpiradas.size());

        } catch (Exception e) {
            System.err.println("[" + LocalDateTime.now().format(LOG_FORMATTER) +
                    "] ERROR en proceso de liberación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Liberar una reserva expirada (cambiar estado y devolver boletas)
     */
    @Transactional
    public void liberarReserva(Compra compra) {
        // Verificar que la reserva esté en estado RESERVADA
        if (compra.getEstado() != EstadoCompra.RESERVADA) {
            throw new RuntimeException("La reserva no está en estado RESERVADA");
        }

        // Cambiar estado a CANCELADA
        compra.setEstado(EstadoCompra.CANCELADA);
        compraRepositorio.save(compra);

        // Liberar las boletas de cada item de la compra
        for (ItemCompra item : compra.getItems()) {
            // Buscar la zona correspondiente en el evento
            Zona zona = compra.getEvento().getZonas().stream()
                    .filter(z -> z.getTipoZona().equals(item.getTipoZona()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Zona no encontrada para liberar boletas"));

            // Liberar boletas
            zona.liberar(item.getCantidad());
            zonaRepositorio.save(zona);
        }
    }

    /**
     * Ejecutar liberación manual (para pruebas o desde interfaz)
     */
    public int ejecutarLiberacionManual() {
        System.out.println("[" + LocalDateTime.now().format(LOG_FORMATTER) + "] INICIO: Ejecución MANUAL de liberación");

        List<Compra> reservasExpiradas = compraRepositorio.findReservasExpiradas(
                EstadoCompra.RESERVADA,
                LocalDateTime.now()
        );

        if (reservasExpiradas.isEmpty()) {
            System.out.println("[" + LocalDateTime.now().format(LOG_FORMATTER) + "] No hay reservas vencidas para liberar");
            return 0;
        }

        int liberadas = 0;
        for (Compra compra : reservasExpiradas) {
            try {
                liberarReserva(compra);
                liberadas++;
            } catch (Exception e) {
                System.err.println("Error al liberar reserva #" + compra.getId() + ": " + e.getMessage());
            }
        }

        System.out.println("[" + LocalDateTime.now().format(LOG_FORMATTER) +
                "] FIN: Liberación manual completada. Reservas liberadas: " + liberadas);

        return liberadas;
    }

    /**
     * Obtener número de reservas vencidas pendientes
     */
    public long contarReservasVencidas() {
        return compraRepositorio.findReservasExpiradas(EstadoCompra.RESERVADA, LocalDateTime.now()).size();
    }
}
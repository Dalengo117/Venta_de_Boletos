package edu.unilibre.Venta_de_Boletos.servicios;

import edu.unilibre.Venta_de_Boletos.entidades.*;
import edu.unilibre.Venta_de_Boletos.repositorios.CompraRepositorio;
import edu.unilibre.Venta_de_Boletos.repositorios.EventoRepositorio;
import edu.unilibre.Venta_de_Boletos.repositorios.UsuarioRepositorio;
import edu.unilibre.Venta_de_Boletos.repositorios.ZonaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class CompraServicio {

    @Autowired
    private CompraRepositorio compraRepositorio;

    @Autowired
    private EventoRepositorio eventoRepositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private ZonaRepositorio zonaRepositorio;

    // ============ MÉTODOS PARA HU03 (ya existentes) ============

    /**
     * Reservar boletas para un evento
     */
    public Compra reservarBoletas(
            Long eventoId,
            String identificacionComprador,
            String nombreComprador,
            List<ItemCompra> items,
            MetodoPago metodoPago) {

        // Validar evento
        Evento evento = eventoRepositorio.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento no encontrado"));

        // Validar fecha del evento (no puede ser pasado)
        if (evento.getFecha().isBefore(java.time.LocalDate.now())) {
            throw new RuntimeException("No se pueden reservar boletas para eventos pasados");
        }

        // Validar disponibilidad de cada zona
        for (ItemCompra item : items) {
            Zona zona = obtenerZonaPorTipo(evento, item.getTipoZona());

            if (zona == null) {
                throw new RuntimeException("Zona " + item.getTipoZona() + " no existe en este evento");
            }

            // Validar cantidad máxima por zona (10)
            if (item.getCantidad() > 10) {
                throw new RuntimeException("Máximo 10 boletas por zona");
            }
            if (item.getCantidad() < 1) {
                throw new RuntimeException("Debe seleccionar al menos 1 boleta");
            }

            // Validar disponibilidad
            if (zona.getBoletasDisponibles() < item.getCantidad()) {
                throw new RuntimeException("No hay suficientes boletas disponibles en " +
                        obtenerNombreZona(item.getTipoZona()));
            }
        }

        // Buscar o crear comprador
        Usuario comprador = usuarioRepositorio.findByIdentificacion(identificacionComprador)
                .orElseGet(() -> {
                    Usuario nuevo = Usuario.builder()
                            .identificacion(identificacionComprador)
                            .nombre(nombreComprador)
                            .build();
                    return usuarioRepositorio.save(nuevo);
                });

        // Crear compra
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime expiracion = ahora.plusHours(24);

        Compra compra = Compra.builder()
                .fechaReserva(ahora)
                .fechaExpiracion(expiracion)
                .estado(EstadoCompra.RESERVADA)
                .metodoPago(metodoPago)
                .comprador(comprador)
                .evento(evento)
                .build();

        // Agregar items y actualizar disponibilidad de zonas
        for (ItemCompra item : items) {
            Zona zona = obtenerZonaPorTipo(evento, item.getTipoZona());

            // Guardar precio unitario actual
            item.setPrecioUnitario(zona.getPrecio());

            // Agregar a la compra
            compra.agregarItem(item);

            // Actualizar disponibilidad
            zona.reservar(item.getCantidad());
            zonaRepositorio.save(zona);
        }

        // Guardar compra
        return compraRepositorio.save(compra);
    }

    /**
     * Obtener zona por tipo dentro de un evento
     */
    private Zona obtenerZonaPorTipo(Evento evento, TipoZona tipoZona) {
        return evento.getZonas().stream()
                .filter(z -> z.getTipoZona().equals(tipoZona))
                .findFirst()
                .orElse(null);
    }

    /**
     * Obtener nombre legible de zona
     */
    private String obtenerNombreZona(TipoZona tipoZona) {
        switch (tipoZona) {
            case ZONA_A: return "Zona A";
            case ZONA_B: return "Zona B";
            case ZONA_C: return "Zona C";
            default: return "Desconocida";
        }
    }

    // ============ NUEVOS MÉTODOS PARA HU04 ============

    /**
     * Buscar reservas pendientes (RESERVADA) por identificación del comprador
     */
    public List<Compra> buscarReservasPendientes(String identificacion) {
        List<Compra> compras = compraRepositorio.findByCompradorIdentificacionOrderByFechaReservaDesc(identificacion);
        // Filtrar solo las que están en estado RESERVADA
        return compras.stream()
                .filter(c -> c.getEstado() == EstadoCompra.RESERVADA)
                .toList();
    }

    /**
     * Procesar el pago de una reserva
     */
    public Compra procesarPago(Long compraId, String numeroComprobante, double montoPagado) {
        Compra compra = compraRepositorio.findById(compraId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        // Verificar estado
        if (compra.getEstado() != EstadoCompra.RESERVADA) {
            throw new RuntimeException("La reserva no está en estado RESERVADA");
        }

        // Verificar expiración
        if (compra.estaExpirada()) {
            throw new RuntimeException("La reserva ha expirado");
        }

        // Validar monto
        double total = compra.calcularTotal();
        if (montoPagado != total) {
            throw new RuntimeException("El valor pagado (" + String.format("$%,.0f", montoPagado) +
                    ") no coincide con el total de la reserva ($" + String.format("$%,.0f", total) + ")");
        }

        // Procesar pago
        compra.pagar(montoPagado, numeroComprobante);
        return compraRepositorio.save(compra);
    }

    /**
     * Obtener el tiempo restante de una reserva formateado
     */
    public String obtenerTiempoRestante(Compra compra) {
        if (compra.getEstado() != EstadoCompra.RESERVADA) {
            return "N/A";
        }
        if (compra.estaExpirada()) {
            return "EXPIRADA";
        }
        LocalDateTime ahora = LocalDateTime.now();
        long horas = java.time.Duration.between(ahora, compra.getFechaExpiracion()).toHours();
        long minutos = java.time.Duration.between(ahora, compra.getFechaExpiracion()).toMinutes() % 60;

        if (horas <= 0 && minutos <= 0) {
            return "EXPIRADA";
        }
        return String.format("%d horas, %d minutos", horas, minutos);
    }

    // ============ MÉTODOS PARA FUTURAS HUS ============

    /**
     * Buscar compras por identificación de comprador (todas)
     */
    public List<Compra> buscarComprasPorIdentificacion(String identificacion) {
        return compraRepositorio.findByCompradorIdentificacionOrderByFechaReservaDesc(identificacion);
    }

    /**
     * Buscar compras por estado
     */
    public List<Compra> buscarComprasPorEstado(EstadoCompra estado) {
        return compraRepositorio.findByEstado(estado);
    }

    /**
     * Buscar reservas expiradas
     */
    public List<Compra> buscarReservasExpiradas() {
        return compraRepositorio.findReservasExpiradas(
                EstadoCompra.RESERVADA,
                LocalDateTime.now()
        );
    }

    // ============ NUEVO MÉTODO PARA HU06 ============

    /**
     * Cancelar manualmente una reserva (por expiración o por administrador)
     */
    public void cancelarReserva(Long compraId) {
        Compra compra = compraRepositorio.findById(compraId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        if (compra.getEstado() != EstadoCompra.RESERVADA) {
            throw new RuntimeException("Solo se pueden cancelar reservas en estado RESERVADA");
        }

        // Cambiar estado
        compra.setEstado(EstadoCompra.CANCELADA);
        compraRepositorio.save(compra);

        // Liberar boletas
        for (ItemCompra item : compra.getItems()) {
            Zona zona = compra.getEvento().getZonas().stream()
                    .filter(z -> z.getTipoZona().equals(item.getTipoZona()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Zona no encontrada"));

            zona.liberar(item.getCantidad());
            zonaRepositorio.save(zona);
        }
    }
}
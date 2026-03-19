package edu.unilibre.Venta_de_Boletos.servicios;

import edu.unilibre.Venta_de_Boletos.entidades.Evento;
import edu.unilibre.Venta_de_Boletos.entidades.Zona;
import edu.unilibre.Venta_de_Boletos.entidades.Usuario;
import edu.unilibre.Venta_de_Boletos.entidades.TipoZona;
import edu.unilibre.Venta_de_Boletos.repositorios.EventoRepositorio;
import edu.unilibre.Venta_de_Boletos.repositorios.UsuarioRepositorio;
import edu.unilibre.Venta_de_Boletos.repositorios.ZonaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional
public class EventoServicio {

    @Autowired
    private EventoRepositorio eventoRepositorio;

    @Autowired
    private ZonaRepositorio zonaRepositorio;

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    /**
     * Crea un nuevo evento con sus zonas
     */
    public Evento crearEvento(Evento evento, Long organizadorId) {
        // Validar que la fecha no sea anterior a hoy
        if (evento.getFecha().isBefore(LocalDate.now())) {
            throw new RuntimeException("La fecha del evento no puede ser anterior a la fecha actual");
        }

        // Validar que la suma de boletas por zona sea igual al total
        int sumaBoletasZonas = evento.getZonas().stream()
                .mapToInt(Zona::getBoletasTotales)
                .sum();

        if (sumaBoletasZonas != evento.getTotalBoletas()) {
            throw new RuntimeException("La suma de boletas por zona debe ser igual al total de boletas del evento");
        }

        // Asignar el organizador
        Usuario organizador = usuarioRepositorio.findById(organizadorId)
                .orElseThrow(() -> new RuntimeException("Organizador no encontrado"));
        evento.setOrganizador(organizador);

        // Guardar el evento (las zonas se guardan en cascada)
        return eventoRepositorio.save(evento);
    }

    /**
     * Valida los precios de las zonas y establece boletas disponibles
     */
    public boolean validarPreciosZonas(Evento evento) {
        for (Zona zona : evento.getZonas()) {
            switch (zona.getTipoZona()) {
                case ZONA_A:
                    if (zona.getPrecio() != 200000) return false;
                    break;
                case ZONA_B:
                    if (zona.getPrecio() != 100000) return false;
                    break;
                case ZONA_C:
                    if (zona.getPrecio() != 50000) return false;
                    break;
            }
            // Inicialmente, boletasDisponibles = boletasTotales
            zona.setBoletasDisponibles(zona.getBoletasTotales());
        }
        return true;
    }

    /**
     * Busca o crea un usuario por identificación
     */
    public Usuario buscarOCrearUsuario(String identificacion, String nombre) {
        return usuarioRepositorio.findByIdentificacion(identificacion)
                .orElseGet(() -> {
                    Usuario nuevoUsuario = Usuario.builder()
                            .identificacion(identificacion)
                            .nombre(nombre)
                            .build();
                    return usuarioRepositorio.save(nuevoUsuario);
                });
    }
}
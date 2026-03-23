package edu.unilibre.Venta_de_Boletos.entidades;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "zonas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Zona {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TipoZona tipoZona;

    private int precio;

    private int boletasTotales;

    private int boletasDisponibles;

    @ManyToOne
    @JoinColumn(name = "evento_id")
    private Evento evento;

    /**
     * Reservar una cantidad de boletas de esta zona
     * @param cantidad Número de boletas a reservar
     * @return true si la reserva fue exitosa, false si no hay suficiente disponibilidad
     */
    public boolean reservar(int cantidad) {
        if (cantidad <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a cero");
        }
        if (cantidad > boletasDisponibles) {
            throw new RuntimeException("No hay suficientes boletas disponibles en " + obtenerNombreZona());
        }
        if (cantidad > 10) {
            throw new RuntimeException("Máximo 10 boletas por zona");
        }
        this.boletasDisponibles -= cantidad;
        return true;
    }

    /**
     * Liberar una cantidad de boletas de esta zona (cuando una reserva expira o se cancela)
     * @param cantidad Número de boletas a liberar
     */
    public void liberar(int cantidad) {
        if (cantidad <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a cero");
        }
        int nuevasDisponibles = this.boletasDisponibles + cantidad;
        if (nuevasDisponibles > this.boletasTotales) {
            throw new RuntimeException("No se pueden liberar más boletas de las que existen en la zona");
        }
        this.boletasDisponibles = nuevasDisponibles;
    }

    /**
     * Calcular subtotal para una cantidad de boletas
     * @param cantidad Número de boletas
     * @return Subtotal (cantidad * precio)
     */
    public int calcularSubtotal(int cantidad) {
        return cantidad * this.precio;
    }

    /**
     * Obtener nombre legible de la zona
     */
    public String obtenerNombreZona() {
        switch (this.tipoZona) {
            case ZONA_A: return "Zona A";
            case ZONA_B: return "Zona B";
            case ZONA_C: return "Zona C";
            default: return "Desconocida";
        }
    }
}
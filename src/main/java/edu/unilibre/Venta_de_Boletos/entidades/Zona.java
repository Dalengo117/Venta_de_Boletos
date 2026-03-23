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
}
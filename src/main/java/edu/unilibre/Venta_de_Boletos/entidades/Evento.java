package edu.unilibre.Venta_de_Boletos.entidades;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "eventos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private LocalDate fecha;

    private LocalTime hora;

    private String lugar;

    private String patrocinador;

    private int totalBoletas;

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<Zona> zonas = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "organizador_id")
    private Usuario organizador;

    // Método helper para agregar zonas
    public void agregarZona(Zona zona) {
        zonas.add(zona);
        zona.setEvento(this);
    }
}
package edu.unilibre.Venta_de_Boletos.entidades;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String identificacion;

    @OneToMany(mappedBy = "organizador")
    private List<Evento> eventosCreados;

    @OneToMany(mappedBy = "comprador")
    private List<Compra> compras;
}
package edu.unilibre.Venta_de_Boletos.entidades;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "items_compra")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TipoZona tipoZona;

    private int cantidad;

    private int precioUnitario;

    @ManyToOne
    @JoinColumn(name = "compra_id")
    private Compra compra;

    // Método para calcular subtotal
    public double getSubtotal() {
        return cantidad * precioUnitario;
    }
}
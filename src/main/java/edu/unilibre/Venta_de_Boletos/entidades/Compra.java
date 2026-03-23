package edu.unilibre.Venta_de_Boletos.entidades;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compras")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Compra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaReserva;

    private LocalDateTime fechaExpiracion;

    @Enumerated(EnumType.STRING)
    private EstadoCompra estado;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;

    private double totalPagado;

    private String numeroComprobante;

    @ManyToOne
    @JoinColumn(name = "comprador_id")
    private Usuario comprador;

    @ManyToOne
    @JoinColumn(name = "evento_id")
    private Evento evento;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<ItemCompra> items = new ArrayList<>();

    // Método para agregar item
    public void agregarItem(ItemCompra item) {
        items.add(item);
        item.setCompra(this);
    }

    // Método para calcular total
    public double calcularTotal() {
        return items.stream()
                .mapToDouble(ItemCompra::getSubtotal)
                .sum();
    }

    // Verificar si está expirada
    public boolean estaExpirada() {
        return LocalDateTime.now().isAfter(fechaExpiracion);
    }

    // Método para pagar
    public boolean pagar(double monto, String comprobante) {
        if (estado != EstadoCompra.RESERVADA) {
            throw new RuntimeException("La reserva no está en estado RESERVADA");
        }
        if (estaExpirada()) {
            throw new RuntimeException("La reserva ha expirado");
        }
        if (monto != calcularTotal()) {
            throw new RuntimeException("El valor pagado no coincide con el total de la reserva");
        }
        this.totalPagado = monto;
        this.numeroComprobante = comprobante;
        this.estado = EstadoCompra.PAGADA;
        return true;
    }
}
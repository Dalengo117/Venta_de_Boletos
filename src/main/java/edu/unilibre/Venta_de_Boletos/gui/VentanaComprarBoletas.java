package edu.unilibre.Venta_de_Boletos.gui;

import edu.unilibre.Venta_de_Boletos.entidades.*;
import edu.unilibre.Venta_de_Boletos.servicios.CompraServicio;
import edu.unilibre.Venta_de_Boletos.servicios.EventoServicio;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class VentanaComprarBoletas extends JFrame {

    @Autowired
    private EventoServicio eventoServicio;

    @Autowired
    private CompraServicio compraServicio;

    private JComboBox<EventoComboItem> cbEventos;
    private JTextField txtNombreComprador, txtIdentificacion;
    private JSpinner spCantidadA, spCantidadB, spCantidadC;
    private JComboBox<MetodoPago> cbMetodoPago;
    private JLabel lblTotal, lblInfoEvento;
    private JButton btnReservar, btnLimpiar;
    private JPanel panelZonas;

    private Evento eventoSeleccionado;

    public VentanaComprarBoletas() {
        initComponents();
        cargarEventos();
    }

    @PostConstruct
    public void init() {
        cargarEventos();
    }

    private void initComponents() {
        setTitle("Comprar Boletas");
        setSize(650, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);

        int y = 20;
        int labelX = 30;
        int fieldX = 200;
        int width = 300;
        int height = 25;

        // Evento
        agregarLabel("Evento:", labelX, y, 80, height);
        cbEventos = new JComboBox<>();
        cbEventos.setBounds(fieldX, y, width, height);
        cbEventos.addActionListener(e -> cargarInfoEvento());
        add(cbEventos);
        y += 35;

        // Información del evento
        lblInfoEvento = new JLabel("Seleccione un evento para ver detalles");
        lblInfoEvento.setBounds(labelX, y, 550, 25);
        lblInfoEvento.setFont(new Font("Arial", Font.ITALIC, 11));
        add(lblInfoEvento);
        y += 35;

        // Datos del comprador
        agregarLabel("Nombre:", labelX, y, 80, height);
        txtNombreComprador = new JTextField();
        txtNombreComprador.setBounds(fieldX, y, width, height);
        add(txtNombreComprador);
        y += 30;

        agregarLabel("Identificación:", labelX, y, 100, height);
        txtIdentificacion = new JTextField();
        txtIdentificacion.setBounds(fieldX, y, width, height);
        add(txtIdentificacion);
        y += 45;

        // Zonas
        JLabel lblZonas = new JLabel("SELECCIONE CANTIDAD POR ZONA");
        lblZonas.setFont(new Font("Arial", Font.BOLD, 12));
        lblZonas.setBounds(labelX, y, 250, 25);
        add(lblZonas);
        y += 30;

        // Panel para zonas
        panelZonas = new JPanel();
        panelZonas.setLayout(new GridLayout(3, 2, 10, 5));
        panelZonas.setBounds(labelX, y, 550, 100);
        panelZonas.setBorder(BorderFactory.createTitledBorder("Cantidades (máx 10 por zona)"));
        add(panelZonas);

        // Spinners para cada zona
        panelZonas.add(new JLabel("Zona A ($200,000):"));
        spCantidadA = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
        panelZonas.add(spCantidadA);

        panelZonas.add(new JLabel("Zona B ($100,000):"));
        spCantidadB = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
        panelZonas.add(spCantidadB);

        panelZonas.add(new JLabel("Zona C ($50,000):"));
        spCantidadC = new JSpinner(new SpinnerNumberModel(0, 0, 10, 1));
        panelZonas.add(spCantidadC);

        y += 115;

        // Método de pago
        agregarLabel("Método de pago:", labelX, y, 120, height);
        cbMetodoPago = new JComboBox<>(MetodoPago.values());
        cbMetodoPago.setBounds(fieldX, y, 200, height);
        add(cbMetodoPago);
        y += 35;

        // Total
        agregarLabel("Total a pagar:", labelX, y, 100, height);
        lblTotal = new JLabel("$0");
        lblTotal.setFont(new Font("Arial", Font.BOLD, 14));
        lblTotal.setForeground(new Color(0, 100, 0));
        lblTotal.setBounds(fieldX, y, 200, height);
        add(lblTotal);
        y += 45;

        // Botones
        btnReservar = new JButton("Reservar Boletas");
        btnReservar.setBounds(150, y, 150, 35);
        btnReservar.addActionListener(e -> realizarReserva());
        add(btnReservar);

        btnLimpiar = new JButton("Limpiar");
        btnLimpiar.setBounds(320, y, 100, 35);
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        add(btnLimpiar);

        // Agregar listener para actualizar total
        spCantidadA.addChangeListener(e -> actualizarTotal());
        spCantidadB.addChangeListener(e -> actualizarTotal());
        spCantidadC.addChangeListener(e -> actualizarTotal());
    }

    private void agregarLabel(String texto, int x, int y, int ancho, int alto) {
        JLabel label = new JLabel(texto);
        label.setBounds(x, y, ancho, alto);
        add(label);
    }

    private void cargarEventos() {

        if (eventoServicio == null) {
            System.err.println("Error: eventoServicio es null");
            return;
        }

        List<Evento> eventos = eventoServicio.obtenerEventosFuturos();
        cbEventos.removeAllItems();

        if (eventos.isEmpty()) {
            cbEventos.addItem(new EventoComboItem(null, "No hay eventos disponibles"));
        } else {
            for (Evento e : eventos) {
                cbEventos.addItem(new EventoComboItem(e, e.getNombre() + " - " + e.getFecha()));
            }
        }
    }

    private void cargarInfoEvento() {
        EventoComboItem item = (EventoComboItem) cbEventos.getSelectedItem();
        if (item != null && item.getEvento() != null) {
            eventoSeleccionado = item.getEvento();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String info = String.format("<html>📅 %s | 🕒 %s | 📍 %s | 🎤 %s</html>",
                    eventoSeleccionado.getFecha().toString(),
                    eventoSeleccionado.getHora().toString(),
                    eventoSeleccionado.getLugar(),
                    eventoSeleccionado.getPatrocinador());

            lblInfoEvento.setText(info);

            // Limpiar cantidades al cambiar de evento
            spCantidadA.setValue(0);
            spCantidadB.setValue(0);
            spCantidadC.setValue(0);
            actualizarTotal();
        } else {
            eventoSeleccionado = null;
            lblInfoEvento.setText("Seleccione un evento para ver detalles");
        }
    }

    private void actualizarTotal() {
        int cantA = (int) spCantidadA.getValue();
        int cantB = (int) spCantidadB.getValue();
        int cantC = (int) spCantidadC.getValue();

        double total = (cantA * 200000) + (cantB * 100000) + (cantC * 50000);
        lblTotal.setText(String.format("$%,.0f", total));
    }

    private void limpiarFormulario() {
        txtNombreComprador.setText("");
        txtIdentificacion.setText("");
        spCantidadA.setValue(0);
        spCantidadB.setValue(0);
        spCantidadC.setValue(0);
        actualizarTotal();
    }

    private void realizarReserva() {
        try {
            // Validar selección de evento
            if (eventoSeleccionado == null) {
                JOptionPane.showMessageDialog(this,
                        "Debe seleccionar un evento",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validar datos del comprador
            String nombre = txtNombreComprador.getText().trim();
            String identificacion = txtIdentificacion.getText().trim();

            if (nombre.isEmpty() || identificacion.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Debe ingresar nombre e identificación del comprador",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Crear items de compra
            List<ItemCompra> items = new ArrayList<>();

            int cantA = (int) spCantidadA.getValue();
            if (cantA > 0) {
                items.add(ItemCompra.builder()
                        .tipoZona(TipoZona.ZONA_A)
                        .cantidad(cantA)
                        .precioUnitario(200000)
                        .build());
            }

            int cantB = (int) spCantidadB.getValue();
            if (cantB > 0) {
                items.add(ItemCompra.builder()
                        .tipoZona(TipoZona.ZONA_B)
                        .cantidad(cantB)
                        .precioUnitario(100000)
                        .build());
            }

            int cantC = (int) spCantidadC.getValue();
            if (cantC > 0) {
                items.add(ItemCompra.builder()
                        .tipoZona(TipoZona.ZONA_C)
                        .cantidad(cantC)
                        .precioUnitario(50000)
                        .build());
            }

            // Validar que se haya seleccionado al menos una boleta
            if (items.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Debe seleccionar al menos una boleta",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Método de pago
            MetodoPago metodoPago = (MetodoPago) cbMetodoPago.getSelectedItem();

            // Realizar reserva
            Compra compra = compraServicio.reservarBoletas(
                    eventoSeleccionado.getId(),
                    identificacion,
                    nombre,
                    items,
                    metodoPago
            );

            // Mostrar confirmación
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            JOptionPane.showMessageDialog(this,
                    String.format("✅ RESERVA EXITOSA\n\n" +
                                    "Número de reserva: %d\n" +
                                    "Total a pagar: $%,.0f\n" +
                                    "Vence: %s\n\n" +
                                    "Para confirmar la compra, debe reportar el pago\n" +
                                    "antes de la fecha de vencimiento.",
                            compra.getId(),
                            compra.calcularTotal(),
                            compra.getFechaExpiracion().format(formatter)),
                    "Reserva confirmada",
                    JOptionPane.INFORMATION_MESSAGE);

            // Limpiar formulario
            limpiarFormulario();

        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this,
                    "Error: " + e.getMessage(),
                    "Error en la reserva",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Clase helper para el combo de eventos
    private static class EventoComboItem {
        private final Evento evento;
        private final String displayText;

        public EventoComboItem(Evento evento, String displayText) {
            this.evento = evento;
            this.displayText = displayText;
        }

        public Evento getEvento() {
            return evento;
        }

        @Override
        public String toString() {
            return displayText;
        }
    }
}
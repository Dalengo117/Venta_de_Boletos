package edu.unilibre.Venta_de_Boletos.gui;

import edu.unilibre.Venta_de_Boletos.entidades.Compra;
import edu.unilibre.Venta_de_Boletos.entidades.ItemCompra;
import edu.unilibre.Venta_de_Boletos.servicios.CompraServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class VentanaReportarPago extends JFrame {

    @Autowired
    private CompraServicio compraServicio;

    private JTextField txtIdentificacion;
    private JTable tablaReservas;
    private DefaultTableModel modeloTabla;
    private JButton btnBuscar, btnPagar, btnCerrar;
    private JTextField txtNumeroComprobante;
    private JLabel lblSeleccion;

    private Compra compraSeleccionada;

    public VentanaReportarPago() {
        initComponents();
    }

    @PostConstruct
    public void init() {
        // Inicialización después de inyección de dependencias
    }

    private void initComponents() {
        setTitle("Reportar Pago");
        setSize(900, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);

        int y = 20;
        int labelX = 30;
        int fieldX = 180;
        int width = 200;
        int height = 25;

        // Campo de identificación
        JLabel lblIdentificacion = new JLabel("Número de identificación:");
        lblIdentificacion.setBounds(labelX, y, 180, height);
        add(lblIdentificacion);

        txtIdentificacion = new JTextField();
        txtIdentificacion.setBounds(fieldX, y, width, height);
        add(txtIdentificacion);

        btnBuscar = new JButton("Buscar Reservas");
        btnBuscar.setBounds(fieldX + width + 10, y, 140, height);
        btnBuscar.addActionListener(e -> buscarReservas());
        add(btnBuscar);
        y += 45;

        // Tabla de reservas
        String[] columnas = {"ID", "Evento", "Fecha Evento", "Detalle", "Total", "Tiempo Restante"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Ninguna celda es editable directamente
            }
        };

        tablaReservas = new JTable(modeloTabla);
        tablaReservas.setRowHeight(30);
        tablaReservas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaReservas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarReserva();
            }
        });

        // Configurar renderizado centrado
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tablaReservas.getColumnCount(); i++) {
            tablaReservas.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Ajustar anchos
        tablaReservas.getColumnModel().getColumn(0).setPreferredWidth(60);   // ID
        tablaReservas.getColumnModel().getColumn(1).setPreferredWidth(150);  // Evento
        tablaReservas.getColumnModel().getColumn(2).setPreferredWidth(100);  // Fecha Evento
        tablaReservas.getColumnModel().getColumn(3).setPreferredWidth(200);  // Detalle
        tablaReservas.getColumnModel().getColumn(4).setPreferredWidth(100);  // Total
        tablaReservas.getColumnModel().getColumn(5).setPreferredWidth(120);  // Tiempo Restante

        JScrollPane scrollPane = new JScrollPane(tablaReservas);
        scrollPane.setBounds(labelX, y, 820, 250);
        add(scrollPane);
        y += 270;

        // Label de selección
        lblSeleccion = new JLabel("Seleccione una reserva de la tabla para pagar");
        lblSeleccion.setBounds(labelX, y, 400, height);
        lblSeleccion.setForeground(Color.BLUE);
        add(lblSeleccion);
        y += 35;

        // Campo comprobante
        JLabel lblComprobante = new JLabel("Número de comprobante:");
        lblComprobante.setBounds(labelX, y, 170, height);
        add(lblComprobante);

        txtNumeroComprobante = new JTextField();
        txtNumeroComprobante.setBounds(fieldX, y, 250, height);
        txtNumeroComprobante.setEnabled(false);
        add(txtNumeroComprobante);
        y += 40;

        // Botones
        btnPagar = new JButton("Confirmar Pago");
        btnPagar.setBounds(200, y, 150, 35);
        btnPagar.setEnabled(false);
        btnPagar.addActionListener(e -> confirmarPago());
        add(btnPagar);

        btnCerrar = new JButton("Cerrar");
        btnCerrar.setBounds(370, y, 100, 35);
        btnCerrar.addActionListener(e -> dispose());
        add(btnCerrar);
    }

    private void buscarReservas() {
        String identificacion = txtIdentificacion.getText().trim();

        if (identificacion.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Debe ingresar un número de identificación",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        modeloTabla.setRowCount(0);
        compraSeleccionada = null;
        lblSeleccion.setText("Seleccione una reserva de la tabla para pagar");
        txtNumeroComprobante.setEnabled(false);
        txtNumeroComprobante.setText("");
        btnPagar.setEnabled(false);

        List<Compra> reservas = compraServicio.buscarReservasPendientes(identificacion);

        if (reservas.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay reservas pendientes para esta identificación",
                    "Información",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Compra c : reservas) {
            // Construir detalle de zonas
            StringBuilder detalle = new StringBuilder();
            for (ItemCompra item : c.getItems()) {
                if (detalle.length() > 0) detalle.append(", ");
                detalle.append(item.getTipoZona())
                        .append(": ")
                        .append(item.getCantidad())
                        .append(" x $")
                        .append(String.format("%,d", item.getPrecioUnitario()));
            }

            String tiempoRestante = compraServicio.obtenerTiempoRestante(c);

            Object[] fila = {
                    c.getId(),
                    c.getEvento().getNombre(),
                    c.getEvento().getFecha().format(dateFormatter),
                    detalle.toString(),
                    String.format("$%,.0f", c.calcularTotal()),
                    tiempoRestante
            };
            modeloTabla.addRow(fila);
        }

        JOptionPane.showMessageDialog(this,
                "Se encontraron " + reservas.size() + " reservas pendientes.\n" +
                        "Seleccione una de la tabla para continuar con el pago.",
                "Reservas encontradas",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void seleccionarReserva() {
        int fila = tablaReservas.getSelectedRow();
        if (fila >= 0) {
            Long compraId = (Long) modeloTabla.getValueAt(fila, 0);
            compraSeleccionada = compraServicio.buscarComprasPorIdentificacion(txtIdentificacion.getText().trim())
                    .stream()
                    .filter(c -> c.getId().equals(compraId))
                    .findFirst()
                    .orElse(null);

            if (compraSeleccionada != null) {
                // Verificar expiración
                if (compraSeleccionada.estaExpirada()) {
                    lblSeleccion.setText("⚠️ Esta reserva ha EXPIRADO. No se puede pagar.");
                    lblSeleccion.setForeground(Color.RED);
                    txtNumeroComprobante.setEnabled(false);
                    btnPagar.setEnabled(false);
                } else {
                    lblSeleccion.setText("✅ Reserva seleccionada. Total: " +
                            String.format("$%,.0f", compraSeleccionada.calcularTotal()));
                    lblSeleccion.setForeground(new Color(0, 100, 0));
                    txtNumeroComprobante.setEnabled(true);
                    txtNumeroComprobante.requestFocus();
                    btnPagar.setEnabled(true);
                }
            }
        }
    }

    private void confirmarPago() {
        if (compraSeleccionada == null) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar una reserva",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String numeroComprobante = txtNumeroComprobante.getText().trim();

        if (numeroComprobante.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Debe ingresar el número de comprobante",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double total = compraSeleccionada.calcularTotal();

            // Confirmación con el usuario
            int confirmacion = JOptionPane.showConfirmDialog(this,
                    String.format("Confirmar pago:\n\n" +
                                    "Reserva #%d\n" +
                                    "Evento: %s\n" +
                                    "Total: $%,.0f\n" +
                                    "Comprobante: %s\n\n" +
                                    "¿Es correcto?",
                            compraSeleccionada.getId(),
                            compraSeleccionada.getEvento().getNombre(),
                            total,
                            numeroComprobante),
                    "Confirmar pago",
                    JOptionPane.YES_NO_OPTION);

            if (confirmacion != JOptionPane.YES_OPTION) {
                return;
            }

            // Procesar pago
            Compra compraPagada = compraServicio.procesarPago(
                    compraSeleccionada.getId(),
                    numeroComprobante,
                    total
            );

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            JOptionPane.showMessageDialog(this,
                    String.format("✅ PAGO REGISTRADO EXITOSAMENTE\n\n" +
                                    "Reserva #%d\n" +
                                    "Evento: %s\n" +
                                    "Total pagado: $%,.0f\n" +
                                    "Comprobante: %s\n" +
                                    "Fecha de pago: %s\n\n" +
                                    "Sus boletas han sido confirmadas.",
                            compraPagada.getId(),
                            compraPagada.getEvento().getNombre(),
                            compraPagada.getTotalPagado(),
                            compraPagada.getNumeroComprobante(),
                            compraPagada.getFechaExpiracion().format(formatter)),
                    "Pago exitoso",
                    JOptionPane.INFORMATION_MESSAGE);

            // Limpiar y recargar
            txtNumeroComprobante.setText("");
            txtNumeroComprobante.setEnabled(false);
            btnPagar.setEnabled(false);
            compraSeleccionada = null;
            buscarReservas(); // Recargar tabla

        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al procesar el pago:\n" + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
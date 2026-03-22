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
public class VentanaMisCompras extends JFrame {

    @Autowired
    private CompraServicio compraServicio;

    private JTextField txtIdentificacion;
    private JTable tablaCompras;
    private DefaultTableModel modeloTabla;
    private JButton btnBuscar, btnCerrar;

    public VentanaMisCompras() {
        initComponents();
    }

    @PostConstruct
    public void init() {
        // Inicialización después de inyección de dependencias
    }

    private void initComponents() {
        setTitle("Mis Compras");
        setSize(1000, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);

        int y = 20;
        int labelX = 30;
        int fieldX = 200;
        int width = 200;
        int height = 25;

        // Campo de identificación
        JLabel lblIdentificacion = new JLabel("Número de identificación:");
        lblIdentificacion.setBounds(labelX, y, 180, height);
        add(lblIdentificacion);

        txtIdentificacion = new JTextField();
        txtIdentificacion.setBounds(fieldX, y, width, height);
        add(txtIdentificacion);

        btnBuscar = new JButton("Buscar Mis Compras");
        btnBuscar.setBounds(fieldX + width + 10, y, 150, height);
        btnBuscar.addActionListener(e -> buscarCompras());
        add(btnBuscar);
        y += 45;

        // Tabla de compras
        String[] columnas = {
                "ID", "Evento", "Fecha Evento", "Detalle Boletas",
                "Total Pagado", "Estado", "Fecha Reserva", "Vencimiento"
        };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Ninguna celda es editable
            }
        };

        tablaCompras = new JTable(modeloTabla);
        tablaCompras.setRowHeight(35);

        // Configurar renderizado centrado
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < tablaCompras.getColumnCount(); i++) {
            tablaCompras.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Configurar renderizado personalizado para columna de estado (con colores)
        tablaCompras.getColumnModel().getColumn(5).setCellRenderer(new EstadoCellRenderer());

        // Ajustar anchos
        tablaCompras.getColumnModel().getColumn(0).setPreferredWidth(60);   // ID
        tablaCompras.getColumnModel().getColumn(1).setPreferredWidth(150);  // Evento
        tablaCompras.getColumnModel().getColumn(2).setPreferredWidth(100);  // Fecha Evento
        tablaCompras.getColumnModel().getColumn(3).setPreferredWidth(250);  // Detalle Boletas
        tablaCompras.getColumnModel().getColumn(4).setPreferredWidth(100);  // Total Pagado
        tablaCompras.getColumnModel().getColumn(5).setPreferredWidth(100);  // Estado
        tablaCompras.getColumnModel().getColumn(6).setPreferredWidth(120);  // Fecha Reserva
        tablaCompras.getColumnModel().getColumn(7).setPreferredWidth(120);  // Vencimiento

        JScrollPane scrollPane = new JScrollPane(tablaCompras);
        scrollPane.setBounds(labelX, y, 920, 400);
        add(scrollPane);
        y += 420;

        // Botón cerrar
        btnCerrar = new JButton("Cerrar");
        btnCerrar.setBounds(450, y, 100, 35);
        btnCerrar.addActionListener(e -> dispose());
        add(btnCerrar);
    }

    private void buscarCompras() {
        String identificacion = txtIdentificacion.getText().trim();

        if (identificacion.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Debe ingresar un número de identificación",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        modeloTabla.setRowCount(0);

        List<Compra> compras = compraServicio.buscarComprasPorIdentificacion(identificacion);

        if (compras.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay compras registradas para esta identificación",
                    "Información",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (Compra c : compras) {
            // Construir detalle de zonas
            StringBuilder detalle = new StringBuilder();
            for (ItemCompra item : c.getItems()) {
                if (detalle.length() > 0) detalle.append(", ");
                detalle.append(item.getTipoZona().toString().replace("ZONA_", "Zona "))
                        .append(": ")
                        .append(item.getCantidad())
                        .append(" boletas");
            }

            String estado = "";
            switch (c.getEstado()) {
                case RESERVADA:
                    estado = "RESERVADA";
                    break;
                case PAGADA:
                    estado = "PAGADA";
                    break;
                case CANCELADA:
                    estado = "CANCELADA";
                    break;
            }

            String totalFormateado = String.format("$%,.0f", c.getTotalPagado() > 0 ? c.getTotalPagado() : c.calcularTotal());

            Object[] fila = {
                    c.getId(),
                    c.getEvento().getNombre(),
                    c.getEvento().getFecha().format(dateFormatter),
                    detalle.toString(),
                    totalFormateado,
                    estado,  // Guardamos el estado para el renderizador
                    c.getFechaReserva().format(dateTimeFormatter),
                    c.getFechaExpiracion().format(dateTimeFormatter)
            };
            modeloTabla.addRow(fila);
        }

        // Aplicar color a las filas según estado
        aplicarColoresPorEstado();

        JOptionPane.showMessageDialog(this,
                "Se encontraron " + compras.size() + " compras registradas.",
                "Información",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void aplicarColoresPorEstado() {
        for (int row = 0; row < modeloTabla.getRowCount(); row++) {
            String estado = (String) modeloTabla.getValueAt(row, 5);
            Color colorFondo = null;

            switch (estado) {
                case "RESERVADA":
                    colorFondo = new Color(255, 255, 200); // Amarillo claro
                    break;
                case "PAGADA":
                    colorFondo = new Color(200, 255, 200); // Verde claro
                    break;
                case "CANCELADA":
                    colorFondo = new Color(255, 200, 200); // Rojo claro
                    break;
            }

            if (colorFondo != null) {
                tablaCompras.setRowSelectionAllowed(true);
                // Aplicar color a la fila completa
                for (int col = 0; col < tablaCompras.getColumnCount(); col++) {
                    tablaCompras.getColumnModel().getColumn(col).setCellRenderer(new RowColorRenderer(colorFondo, row));
                }
            }
        }
    }

    // Renderizador personalizado para colores por fila
    class RowColorRenderer extends DefaultTableCellRenderer {
        private final Color colorFondo;
        private final int rowIndex;

        public RowColorRenderer(Color colorFondo, int rowIndex) {
            this.colorFondo = colorFondo;
            this.rowIndex = rowIndex;
        }

        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                                                                boolean isSelected, boolean hasFocus,
                                                                int row, int column) {
            java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (row == rowIndex) {
                c.setBackground(colorFondo);
                if (isSelected) {
                    c.setBackground(colorFondo.darker());
                }
            } else {
                // Restaurar color por defecto si no es la fila seleccionada
                if (!isSelected) {
                    c.setBackground(table.getBackground());
                }
            }
            return c;
        }
    }

    // Renderizador para columna de estado con iconos y colores
    class EstadoCellRenderer extends DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                                                                boolean isSelected, boolean hasFocus,
                                                                int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);

            String estado = (String) value;
            if (estado != null) {
                switch (estado) {
                    case "RESERVADA":
                        label.setText("🟡 RESERVADA");
                        label.setBackground(new Color(255, 255, 200));
                        break;
                    case "PAGADA":
                        label.setText("🟢 PAGADA");
                        label.setBackground(new Color(200, 255, 200));
                        break;
                    case "CANCELADA":
                        label.setText("🔴 CANCELADA");
                        label.setBackground(new Color(255, 200, 200));
                        break;
                }
            }
            return label;
        }
    }
}
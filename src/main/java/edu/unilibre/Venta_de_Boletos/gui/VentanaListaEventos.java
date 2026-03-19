package edu.unilibre.Venta_de_Boletos.gui;

import edu.unilibre.Venta_de_Boletos.entidades.Evento;
import edu.unilibre.Venta_de_Boletos.entidades.TipoZona;
import edu.unilibre.Venta_de_Boletos.servicios.EventoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.util.List;

@Component
public class VentanaListaEventos extends JFrame {

    @Autowired
    private EventoServicio eventoServicio;

    private JTable tablaEventos;
    private DefaultTableModel modeloTabla;
    private JButton btnActualizar, btnReservar, btnCerrar;
    private JComboBox<String> cbFiltro;

    public VentanaListaEventos() {
        initComponents();
        cargarEventos();
    }

    private void initComponents() {
        setTitle("Eventos Disponibles");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);

        int y = 10;

        // Panel de filtros
        JLabel lblFiltro = new JLabel("Mostrar:");
        lblFiltro.setBounds(20, y, 60, 25);
        add(lblFiltro);

        cbFiltro = new JComboBox<>(new String[]{"Todos los eventos", "Solo eventos futuros"});
        cbFiltro.setBounds(80, y, 200, 25);
        cbFiltro.addActionListener(e -> cargarEventos());
        add(cbFiltro);

        btnActualizar = new JButton("Actualizar");
        btnActualizar.setBounds(300, y, 100, 25);
        btnActualizar.addActionListener(e -> cargarEventos());
        add(btnActualizar);
        y += 35;

        // Crear la tabla
        String[] columnas = {
                "ID", "Nombre del Evento", "Fecha", "Hora", "Lugar",
                "Zona A", "Zona B", "Zona C", "Acción"
        };

        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 8; // Solo la columna de acción es editable (para el botón)
            }
        };

        tablaEventos = new JTable(modeloTabla);
        tablaEventos.setRowHeight(30);

        // Configurar renderizado de celdas
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < tablaEventos.getColumnCount(); i++) {
            tablaEventos.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Ajustar anchos de columna
        tablaEventos.getColumnModel().getColumn(0).setPreferredWidth(50);   // ID
        tablaEventos.getColumnModel().getColumn(1).setPreferredWidth(200);  // Nombre
        tablaEventos.getColumnModel().getColumn(2).setPreferredWidth(100);  // Fecha
        tablaEventos.getColumnModel().getColumn(3).setPreferredWidth(80);   // Hora
        tablaEventos.getColumnModel().getColumn(4).setPreferredWidth(150);  // Lugar
        tablaEventos.getColumnModel().getColumn(5).setPreferredWidth(80);   // Zona A
        tablaEventos.getColumnModel().getColumn(6).setPreferredWidth(80);   // Zona B
        tablaEventos.getColumnModel().getColumn(7).setPreferredWidth(80);   // Zona C
        tablaEventos.getColumnModel().getColumn(8).setPreferredWidth(80);   // Acción

        // Agregar botón de reservar en la tabla
        tablaEventos.getColumn("Acción").setCellRenderer(new ButtonRenderer());
        tablaEventos.getColumn("Acción").setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(tablaEventos);
        scrollPane.setBounds(20, y, 850, 350);
        add(scrollPane);
        y += 360;

        // Botón cerrar
        btnCerrar = new JButton("Cerrar");
        btnCerrar.setBounds(400, y, 100, 30);
        btnCerrar.addActionListener(e -> dispose());
        add(btnCerrar);
    }

    private void cargarEventos() {
        modeloTabla.setRowCount(0);

        List<Evento> eventos;
        if (cbFiltro.getSelectedIndex() == 0) {
            eventos = eventoServicio.obtenerTodosLosEventos();
        } else {
            eventos = eventoServicio.obtenerEventosFuturos();
        }

        if (eventos.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No hay eventos disponibles en este momento",
                    "Información",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            for (Evento evento : eventos) {
                Object[] fila = {
                        evento.getId(),
                        evento.getNombre(),
                        evento.getFecha().toString(),
                        evento.getHora().toString(),
                        evento.getLugar(),
                        eventoServicio.obtenerDisponibilidadPorZona(evento, TipoZona.ZONA_A),
                        eventoServicio.obtenerDisponibilidadPorZona(evento, TipoZona.ZONA_B),
                        eventoServicio.obtenerDisponibilidadPorZona(evento, TipoZona.ZONA_C),
                        "Reservar"
                };
                modeloTabla.addRow(fila);
            }
        }
    }

    // Renderizador para botones en la tabla
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                                                                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "Reservar" : value.toString());
            return this;
        }
    }

    // Editor para botones en la tabla
    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private String label;
        private boolean isPushed;
        private int row;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public java.awt.Component getTableCellEditorComponent(JTable table, Object value,
                                                              boolean isSelected, int row, int column) {
            this.row = row;
            label = (value == null) ? "Reservar" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                // Aquí se maneja el clic en Reservar
                Long eventoId = (Long) modeloTabla.getValueAt(row, 0);
                String eventoNombre = (String) modeloTabla.getValueAt(row, 1);

                int opcion = JOptionPane.showConfirmDialog(VentanaListaEventos.this,
                        "¿Desea reservar boletas para el evento:\n" + eventoNombre + "?",
                        "Confirmar reserva",
                        JOptionPane.YES_NO_OPTION);

                if (opcion == JOptionPane.YES_OPTION) {
                    JOptionPane.showMessageDialog(VentanaListaEventos.this,
                            "Funcionalidad de reserva en construcción\n(HU03 - Comprar boletas)",
                            "Próximamente",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }
}
package edu.unilibre.Venta_de_Boletos.gui;

import edu.unilibre.Venta_de_Boletos.entidades.Evento;
import edu.unilibre.Venta_de_Boletos.entidades.Zona;
import edu.unilibre.Venta_de_Boletos.entidades.TipoZona;
import edu.unilibre.Venta_de_Boletos.entidades.Usuario;
import edu.unilibre.Venta_de_Boletos.servicios.EventoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@Component
public class VentanaCrearEvento extends JFrame {

    @Autowired
    private EventoServicio eventoServicio;

    // Campos de texto
    private JTextField txtNombre, txtFecha, txtHora, txtLugar, txtPatrocinador, txtTotalBoletas;
    private JTextField txtBoletasA, txtBoletasB, txtBoletasC;
    private JTextField txtIdentificacionOrganizador, txtNombreOrganizador;

    private JButton btnGuardar, btnLimpiar;

    public VentanaCrearEvento() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Crear Nuevo Evento");
        setSize(550, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);

        int y = 20;
        int labelX = 30;
        int fieldX = 200;
        int width = 250;
        int height = 25;

        // Título sección
        JLabel lblTituloEvento = new JLabel("DATOS DEL EVENTO");
        lblTituloEvento.setFont(new Font("Arial", Font.BOLD, 14));
        lblTituloEvento.setBounds(labelX, y, 200, height);
        add(lblTituloEvento);
        y += 30;

        // Campos del evento
        agregarLabel("Nombre:", labelX, y, 120, height);
        txtNombre = new JTextField();
        txtNombre.setBounds(fieldX, y, width, height);
        add(txtNombre);
        y += 30;

        agregarLabel("Fecha (YYYY-MM-DD):", labelX, y, 150, height);
        txtFecha = new JTextField();
        txtFecha.setBounds(fieldX, y, width, height);
        add(txtFecha);
        y += 30;

        agregarLabel("Hora (HH:MM):", labelX, y, 120, height);
        txtHora = new JTextField();
        txtHora.setBounds(fieldX, y, width, height);
        add(txtHora);
        y += 30;

        agregarLabel("Lugar:", labelX, y, 120, height);
        txtLugar = new JTextField();
        txtLugar.setBounds(fieldX, y, width, height);
        add(txtLugar);
        y += 30;

        agregarLabel("Patrocinador:", labelX, y, 120, height);
        txtPatrocinador = new JTextField();
        txtPatrocinador.setBounds(fieldX, y, width, height);
        add(txtPatrocinador);
        y += 30;

        agregarLabel("Total Boletas:", labelX, y, 120, height);
        txtTotalBoletas = new JTextField();
        txtTotalBoletas.setBounds(fieldX, y, width, height);
        add(txtTotalBoletas);
        y += 40;

        // Zonas
        JLabel lblTituloZonas = new JLabel("DISTRIBUCIÓN POR ZONAS");
        lblTituloZonas.setFont(new Font("Arial", Font.BOLD, 14));
        lblTituloZonas.setBounds(labelX, y, 250, height);
        add(lblTituloZonas);
        y += 30;

        agregarLabel("Zona A ($200,000):", labelX, y, 150, height);
        txtBoletasA = new JTextField();
        txtBoletasA.setBounds(fieldX, y, width, height);
        add(txtBoletasA);
        y += 30;

        agregarLabel("Zona B ($100,000):", labelX, y, 150, height);
        txtBoletasB = new JTextField();
        txtBoletasB.setBounds(fieldX, y, width, height);
        add(txtBoletasB);
        y += 30;

        agregarLabel("Zona C ($50,000):", labelX, y, 150, height);
        txtBoletasC = new JTextField();
        txtBoletasC.setBounds(fieldX, y, width, height);
        add(txtBoletasC);
        y += 40;

        // Organizador
        JLabel lblTituloOrg = new JLabel("ORGANIZADOR");
        lblTituloOrg.setFont(new Font("Arial", Font.BOLD, 14));
        lblTituloOrg.setBounds(labelX, y, 200, height);
        add(lblTituloOrg);
        y += 30;

        agregarLabel("Identificación:", labelX, y, 120, height);
        txtIdentificacionOrganizador = new JTextField();
        txtIdentificacionOrganizador.setBounds(fieldX, y, width, height);
        add(txtIdentificacionOrganizador);
        y += 30;

        agregarLabel("Nombre:", labelX, y, 120, height);
        txtNombreOrganizador = new JTextField();
        txtNombreOrganizador.setBounds(fieldX, y, width, height);
        add(txtNombreOrganizador);
        y += 40;

        // Botones
        btnGuardar = new JButton("Guardar Evento");
        btnGuardar.setBounds(150, y, 140, 35);
        btnGuardar.addActionListener(e -> guardarEvento());
        add(btnGuardar);

        btnLimpiar = new JButton("Limpiar");
        btnLimpiar.setBounds(310, y, 100, 35);
        btnLimpiar.addActionListener(e -> limpiarCampos());
        add(btnLimpiar);

        // Agregar listener para validar suma de boletas en tiempo real
        txtTotalBoletas.getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override
            public void update() {
                validarSumaBoletas();
            }
        });

        txtBoletasA.getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override
            public void update() {
                validarSumaBoletas();
            }
        });

        txtBoletasB.getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override
            public void update() {
                validarSumaBoletas();
            }
        });

        txtBoletasC.getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override
            public void update() {
                validarSumaBoletas();
            }
        });
    }

    private void agregarLabel(String texto, int x, int y, int ancho, int alto) {
        JLabel label = new JLabel(texto);
        label.setBounds(x, y, ancho, alto);
        add(label);
    }

    private void validarSumaBoletas() {
        try {
            if (!txtTotalBoletas.getText().trim().isEmpty() &&
                    !txtBoletasA.getText().trim().isEmpty() &&
                    !txtBoletasB.getText().trim().isEmpty() &&
                    !txtBoletasC.getText().trim().isEmpty()) {

                int total = Integer.parseInt(txtTotalBoletas.getText().trim());
                int suma = Integer.parseInt(txtBoletasA.getText().trim()) +
                        Integer.parseInt(txtBoletasB.getText().trim()) +
                        Integer.parseInt(txtBoletasC.getText().trim());

                if (suma != total) {
                    txtTotalBoletas.setBackground(new Color(255, 200, 200));
                } else {
                    txtTotalBoletas.setBackground(Color.WHITE);
                }
            }
        } catch (NumberFormatException e) {
            // Ignorar mientras se escribe
        }
    }

    private void guardarEvento() {
        try {
            // Validar campos obligatorios
            if (txtNombre.getText().trim().isEmpty() ||
                    txtFecha.getText().trim().isEmpty() ||
                    txtHora.getText().trim().isEmpty() ||
                    txtLugar.getText().trim().isEmpty() ||
                    txtPatrocinador.getText().trim().isEmpty() ||
                    txtTotalBoletas.getText().trim().isEmpty() ||
                    txtBoletasA.getText().trim().isEmpty() ||
                    txtBoletasB.getText().trim().isEmpty() ||
                    txtBoletasC.getText().trim().isEmpty() ||
                    txtIdentificacionOrganizador.getText().trim().isEmpty() ||
                    txtNombreOrganizador.getText().trim().isEmpty()) {

                JOptionPane.showMessageDialog(this,
                        "Todos los campos son obligatorios",
                        "Error de validación",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validar que la suma de boletas sea correcta
            int total = Integer.parseInt(txtTotalBoletas.getText().trim());
            int suma = Integer.parseInt(txtBoletasA.getText().trim()) +
                    Integer.parseInt(txtBoletasB.getText().trim()) +
                    Integer.parseInt(txtBoletasC.getText().trim());

            if (suma != total) {
                JOptionPane.showMessageDialog(this,
                        "La suma de boletas por zona (" + suma +
                                ") debe ser igual al total de boletas (" + total + ")",
                        "Error de validación",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Crear o buscar organizador
            Usuario organizador = eventoServicio.buscarOCrearUsuario(
                    txtIdentificacionOrganizador.getText().trim(),
                    txtNombreOrganizador.getText().trim()
            );

            // Crear el evento
            Evento evento = Evento.builder()
                    .nombre(txtNombre.getText().trim())
                    .fecha(LocalDate.parse(txtFecha.getText().trim()))
                    .hora(LocalTime.parse(txtHora.getText().trim()))
                    .lugar(txtLugar.getText().trim())
                    .patrocinador(txtPatrocinador.getText().trim())
                    .totalBoletas(total)
                    .build();

            // Crear zonas
            Zona zonaA = Zona.builder()
                    .tipoZona(TipoZona.ZONA_A)
                    .precio(200000)
                    .boletasTotales(Integer.parseInt(txtBoletasA.getText().trim()))
                    .build();

            Zona zonaB = Zona.builder()
                    .tipoZona(TipoZona.ZONA_B)
                    .precio(100000)
                    .boletasTotales(Integer.parseInt(txtBoletasB.getText().trim()))
                    .build();

            Zona zonaC = Zona.builder()
                    .tipoZona(TipoZona.ZONA_C)
                    .precio(50000)
                    .boletasTotales(Integer.parseInt(txtBoletasC.getText().trim()))
                    .build();

            // Agregar zonas al evento
            evento.agregarZona(zonaA);
            evento.agregarZona(zonaB);
            evento.agregarZona(zonaC);

            // Validar precios
            if (!eventoServicio.validarPreciosZonas(evento)) {
                JOptionPane.showMessageDialog(this,
                        "Los precios de las zonas no son correctos",
                        "Error de validación",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Guardar evento
            Evento eventoGuardado = eventoServicio.crearEvento(evento, organizador.getId());

            JOptionPane.showMessageDialog(this,
                    "✅ Evento creado exitosamente\n" +
                            "ID del evento: " + eventoGuardado.getId() + "\n" +
                            "Nombre: " + eventoGuardado.getNombre(),
                    "Operación exitosa",
                    JOptionPane.INFORMATION_MESSAGE);

            limpiarCampos();

        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this,
                    "Formato de fecha u hora inválido.\nUse YYYY-MM-DD para fecha y HH:MM para hora",
                    "Error de formato",
                    JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Los valores numéricos deben ser válidos",
                    "Error de formato",
                    JOptionPane.ERROR_MESSAGE);
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this,
                    "Error: " + e.getMessage(),
                    "Error en la operación",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarCampos() {
        txtNombre.setText("");
        txtFecha.setText("");
        txtHora.setText("");
        txtLugar.setText("");
        txtPatrocinador.setText("");
        txtTotalBoletas.setText("");
        txtBoletasA.setText("");
        txtBoletasB.setText("");
        txtBoletasC.setText("");
        txtIdentificacionOrganizador.setText("");
        txtNombreOrganizador.setText("");
        txtTotalBoletas.setBackground(Color.WHITE);
    }

    // Interface para DocumentListener simple
    interface SimpleDocumentListener extends javax.swing.event.DocumentListener {
        void update();

        @Override
        default void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
        @Override
        default void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
        @Override
        default void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
    }
}
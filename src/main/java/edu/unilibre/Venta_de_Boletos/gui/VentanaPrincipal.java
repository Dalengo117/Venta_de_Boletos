package edu.unilibre.Venta_de_Boletos.gui;

import edu.unilibre.Venta_de_Boletos.servicios.LiberacionReservasServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;

@Component
public class VentanaPrincipal extends JFrame {

    @Autowired
    private VentanaCrearEvento ventanaCrearEvento;

    @Autowired
    private VentanaListaEventos ventanaListaEventos;

    @Autowired
    private VentanaComprarBoletas ventanaComprarBoletas;

    @Autowired
    private VentanaReportarPago ventanaReportarPago;

    @Autowired
    private VentanaMisCompras ventanaMisCompras;

    @Autowired
    private LiberacionReservasServicio liberacionReservasServicio;

    public VentanaPrincipal() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Sistema de Venta de Boletos");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);

        JLabel lblTitulo = new JLabel("SISTEMA DE VENTA DE BOLETOS");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitulo.setBounds(50, 30, 300, 30);
        add(lblTitulo);

        JButton btnCrearEvento = new JButton("1. Crear Evento");
        btnCrearEvento.setBounds(90, 80, 200, 40);
        btnCrearEvento.addActionListener(e -> {
            ventanaCrearEvento.setVisible(true);
        });
        add(btnCrearEvento);

        JButton btnVerEventos = new JButton("2. Ver Eventos Disponibles");
        btnVerEventos.setBounds(90, 130, 200, 40);
        btnVerEventos.addActionListener(e -> {
            ventanaListaEventos.setVisible(true);
        });
        add(btnVerEventos);

        JButton btnComprarBoletas = new JButton("3. Comprar Boletas");
        btnComprarBoletas.setBounds(90, 180, 200, 40);
        btnComprarBoletas.addActionListener(e -> ventanaComprarBoletas.setVisible(true));
        add(btnComprarBoletas);

        JButton btnReportarPago = new JButton("4. Reportar Pago");
        btnReportarPago.setBounds(90, 230, 200, 40);
        btnReportarPago.addActionListener(e -> ventanaReportarPago.setVisible(true));
        add(btnReportarPago);

        JButton btnMisCompras = new JButton("5. Ver Mis Compras");
        btnMisCompras.setBounds(90, 280, 200, 40);
        btnMisCompras.addActionListener(e -> ventanaMisCompras.setVisible(true));
        add(btnMisCompras);

        JButton btnLiberarReservas = new JButton("6. Liberar Reservas Vencidas");
        btnLiberarReservas.setBounds(90, 330, 200, 40);
        btnLiberarReservas.addActionListener(e -> ejecutarLiberacionManual());
        add(btnLiberarReservas);

        JButton btnSalir = new JButton("Salir");
        btnSalir.setBounds(140, 380, 100, 30);
        btnSalir.addActionListener(e -> System.exit(0));
        add(btnSalir);
    }

    private void ejecutarLiberacionManual() {
        try {
            long pendientes = liberacionReservasServicio.contarReservasVencidas();

            if (pendientes == 0) {
                JOptionPane.showMessageDialog(this,
                        "No hay reservas vencidas pendientes de liberar",
                        "Liberación de reservas",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            int confirmacion = JOptionPane.showConfirmDialog(this,
                    "Se encontraron " + pendientes + " reservas vencidas.\n" +
                            "¿Desea ejecutar la liberación manualmente?",
                    "Confirmar liberación",
                    JOptionPane.YES_NO_OPTION);

            if (confirmacion == JOptionPane.YES_OPTION) {
                int liberadas = liberacionReservasServicio.ejecutarLiberacionManual();
                JOptionPane.showMessageDialog(this,
                        "Proceso completado.\n" +
                                "Reservas liberadas: " + liberadas,
                        "Liberación exitosa",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al ejecutar liberación: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}

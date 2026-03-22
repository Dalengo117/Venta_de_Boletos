package edu.unilibre.Venta_de_Boletos.gui;

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

    public VentanaPrincipal() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Sistema de Venta de Boletos");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);

        JLabel lblTitulo = new JLabel("SISTEMA DE VENTA DE BOLETOS");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        lblTitulo.setBounds(50, 30, 300, 30);
        add(lblTitulo);

        JButton btnCrearEvento = new JButton("1. Crear Evento");
        btnCrearEvento.setBounds(100, 80, 200, 40);
        btnCrearEvento.addActionListener(e -> {
            ventanaCrearEvento.setVisible(true);
        });
        add(btnCrearEvento);

        JButton btnVerEventos = new JButton("2. Ver Eventos Disponibles");
        btnVerEventos.setBounds(100, 130, 200, 40);
        btnVerEventos.addActionListener(e -> {
            ventanaListaEventos.setVisible(true);
        });
        add(btnVerEventos);

        JButton btnComprarBoletas = new JButton("3. Comprar Boletas");
        btnComprarBoletas.setBounds(100, 180, 200, 40);
        btnComprarBoletas.addActionListener(e -> ventanaComprarBoletas.setVisible(true));
        add(btnComprarBoletas);

        JButton btnReportarPago = new JButton("4. Reportar Pago");
        btnReportarPago.setBounds(100, 230, 200, 40);
        btnReportarPago.addActionListener(e -> ventanaReportarPago.setVisible(true));
        add(btnReportarPago);

        JButton btnMisCompras = new JButton("5. Ver Mis Compras");
        btnMisCompras.setBounds(100, 280, 200, 40);
        btnMisCompras.addActionListener(e -> ventanaMisCompras.setVisible(true));
        add(btnMisCompras);

        JButton btnSalir = new JButton("Salir");
        btnSalir.setBounds(150, 330, 100, 30);
        btnSalir.addActionListener(e -> System.exit(0));
        add(btnSalir);
    }
}

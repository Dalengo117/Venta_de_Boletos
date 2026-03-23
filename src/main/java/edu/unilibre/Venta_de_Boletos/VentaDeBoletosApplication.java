package edu.unilibre.Venta_de_Boletos;

import edu.unilibre.Venta_de_Boletos.gui.VentanaCrearEvento;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.awt.*;

@SpringBootApplication
public class VentaDeBoletosApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = new SpringApplicationBuilder(VentaDeBoletosApplication.class)
				.headless(false)
				.run(args);

		EventQueue.invokeLater(() -> {
			VentanaCrearEvento ventana = context.getBean(VentanaCrearEvento.class);
			ventana.setVisible(true);
		});
	}
}

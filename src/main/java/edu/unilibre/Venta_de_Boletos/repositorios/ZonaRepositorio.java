package edu.unilibre.Venta_de_Boletos.repositorios;

import edu.unilibre.Venta_de_Boletos.entidades.Zona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ZonaRepositorio extends JpaRepository<Zona, Long> {
}

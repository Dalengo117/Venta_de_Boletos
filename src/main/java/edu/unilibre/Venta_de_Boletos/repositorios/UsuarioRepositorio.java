package edu.unilibre.Venta_de_Boletos.repositorios;

import edu.unilibre.Venta_de_Boletos.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByIdentificacion(String identificacion);
}

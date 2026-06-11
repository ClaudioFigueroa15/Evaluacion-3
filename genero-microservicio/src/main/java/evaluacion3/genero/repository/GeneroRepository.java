package evaluacion3.genero.repository;

import evaluacion3.genero.model.Genero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GeneroRepository extends JpaRepository<Genero, Long> {

    boolean existsByNombre(String nombre);
}

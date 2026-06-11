package evaluacion2.streamflow.repository;

import evaluacion2.streamflow.model.Genero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GeneroRepository extends JpaRepository<Genero, Long> {

    boolean existsByNombre(String nombre);
}
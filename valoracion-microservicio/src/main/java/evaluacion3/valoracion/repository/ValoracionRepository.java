package evaluacion3.valoracion.repository;

import evaluacion3.valoracion.model.Valoracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ValoracionRepository extends JpaRepository<Valoracion, Long> {

    List<Valoracion> findByIdPelicula(Long idPelicula);
}

package evaluacion2.streamflow.repository;

import evaluacion2.streamflow.model.Valoracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ValoracionRepository extends JpaRepository<Valoracion, Long> {

    List<Valoracion> findByPeliculaId(Long idPelicula);
}
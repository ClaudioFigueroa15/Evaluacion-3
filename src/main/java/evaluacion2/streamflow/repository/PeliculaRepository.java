package evaluacion2.streamflow.repository;

import evaluacion2.streamflow.model.Pelicula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PeliculaRepository extends JpaRepository<Pelicula, Long> {

    List<Pelicula> findByAnioEstrenoGreaterThanEqual(Integer anioEstreno);

    List<Pelicula> findByGeneroId(Long idGenero);
}
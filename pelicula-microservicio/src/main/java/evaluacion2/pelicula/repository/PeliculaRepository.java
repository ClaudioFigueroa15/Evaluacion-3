package evaluacion2.pelicula.repository;

import evaluacion2.pelicula.model.Pelicula;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PeliculaRepository extends JpaRepository<Pelicula, Long> {

    List<Pelicula> findByAnioEstrenoGreaterThanEqual(Integer anioEstreno);

    List<Pelicula> findByIdGenero(Long idGenero);
}

package evaluacion3.search.repository;

import evaluacion3.search.model.SearchIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SearchRepository extends JpaRepository<SearchIndex, Long> {

    Optional<SearchIndex> findByPeliculaId(Long peliculaId);

    List<SearchIndex> findByAnio(Integer anio);

    @Query("SELECT s FROM SearchIndex s WHERE LOWER(s.titulo) LIKE LOWER(CONCAT('%', :termino, '%')) OR LOWER(s.director) LIKE LOWER(CONCAT('%', :termino, '%')) OR LOWER(s.actores) LIKE LOWER(CONCAT('%', :termino, '%')) OR LOWER(s.etiquetas) LIKE LOWER(CONCAT('%', :termino, '%'))")
    List<SearchIndex> buscarPorTermino(@Param("termino") String termino);
}

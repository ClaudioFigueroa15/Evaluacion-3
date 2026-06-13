package evaluacion3.favorites.repository;

import evaluacion3.favorites.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUsuarioIdAndPeliculaId(Long usuarioId, Long peliculaId);

    List<Favorite> findByUsuarioId(Long usuarioId);

    Optional<Favorite> findByUsuarioIdAndPeliculaId(Long usuarioId, Long peliculaId);
}

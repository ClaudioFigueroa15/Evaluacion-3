package evaluacion3.watchhistory.repository;

import evaluacion3.watchhistory.model.WatchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchHistoryRepository extends JpaRepository<WatchHistory, Long> {

    Optional<WatchHistory> findByUsuarioIdAndPeliculaId(Long usuarioId, Long peliculaId);

    List<WatchHistory> findByUsuarioId(Long usuarioId);

    List<WatchHistory> findByUsuarioIdAndCompletadoFalseAndProgresoPorcentajeGreaterThan(Long usuarioId, Integer progreso);
}

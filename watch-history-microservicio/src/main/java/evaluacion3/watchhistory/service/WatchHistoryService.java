package evaluacion3.watchhistory.service;

import evaluacion3.watchhistory.dto.request.WatchHistoryRequestDTO;
import evaluacion3.watchhistory.dto.response.WatchHistoryResponseDTO;
import evaluacion3.watchhistory.exception.RecursoNoEncontradoException;
import evaluacion3.watchhistory.exception.ReglaNegocioException;
import evaluacion3.watchhistory.model.WatchHistory;
import evaluacion3.watchhistory.repository.WatchHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WatchHistoryService {

    private static final Logger log = LoggerFactory.getLogger(WatchHistoryService.class);

    private final WatchHistoryRepository repository;

    public WatchHistoryService(WatchHistoryRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public WatchHistoryResponseDTO obtenerPorId(Long id) {
        log.info("Buscando historial con id {}", id);
        WatchHistory history = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("WatchHistory", id));
        log.info("Historial encontrado para usuario {} y película {}", history.getUsuarioId(), history.getPeliculaId());
        return mapearAResponse(history);
    }

    @Transactional(readOnly = true)
    public List<WatchHistoryResponseDTO> obtenerHistorialPorUsuario(Long usuarioId) {
        log.info("Consultando historial completo del usuario {}", usuarioId);
        List<WatchHistory> lista = repository.findByUsuarioId(usuarioId);
        log.info("Se encontraron {} registros para el usuario {}", lista.size(), usuarioId);
        return lista.stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<WatchHistoryResponseDTO> obtenerContenidoEnProgreso(Long usuarioId) {
        log.info("Consultando contenido en progreso del usuario {}", usuarioId);
        List<WatchHistory> lista = repository.findByUsuarioIdAndCompletadoFalseAndProgresoPorcentajeGreaterThan(usuarioId, 0);
        log.info("Se encontraron {} contenidos en progreso para el usuario {}", lista.size(), usuarioId);
        return lista.stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public WatchHistoryResponseDTO registrarProgreso(WatchHistoryRequestDTO dto) {
        log.info("Registrando progreso: usuarioId={}, peliculaId={}, progreso={}%", dto.getUsuarioId(), dto.getPeliculaId(), dto.getProgresoPorcentaje());

        if (dto.getProgresoPorcentaje() < 0 || dto.getProgresoPorcentaje() > 100) {
            log.warn("Progreso inválido: {}", dto.getProgresoPorcentaje());
            throw new ReglaNegocioException("El progreso debe estar entre 0 y 100");
        }

        Optional<WatchHistory> existente = repository.findByUsuarioIdAndPeliculaId(dto.getUsuarioId(), dto.getPeliculaId());

        WatchHistory history;
        if (existente.isPresent()) {
            history = existente.get();
            log.info("Actualizando registro existente id={}", history.getId());
            history.setProgresoPorcentaje(dto.getProgresoPorcentaje());
            history.setUltimaVezVisto(dto.getUltimaVezVisto());
        } else {
            history = new WatchHistory();
            history.setUsuarioId(dto.getUsuarioId());
            history.setPeliculaId(dto.getPeliculaId());
            history.setProgresoPorcentaje(dto.getProgresoPorcentaje());
            history.setUltimaVezVisto(dto.getUltimaVezVisto());
        }

        history.setCompletado(dto.getProgresoPorcentaje() == 100);

        WatchHistory guardado = repository.save(history);
        log.info("Progreso registrado exitosamente id={}, completado={}", guardado.getId(), guardado.getCompletado());
        return mapearAResponse(guardado);
    }

    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando historial con id {}", id);
        WatchHistory history = repository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("WatchHistory", id));
        repository.delete(history);
        log.info("Historial con id {} eliminado exitosamente", id);
    }

    private WatchHistoryResponseDTO mapearAResponse(WatchHistory history) {
        WatchHistoryResponseDTO dto = new WatchHistoryResponseDTO();
        dto.setId(history.getId());
        dto.setUsuarioId(history.getUsuarioId());
        dto.setPeliculaId(history.getPeliculaId());
        dto.setProgresoPorcentaje(history.getProgresoPorcentaje());
        dto.setCompletado(history.getCompletado());
        dto.setUltimaVezVisto(history.getUltimaVezVisto());
        return dto;
    }
}

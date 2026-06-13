package evaluacion3.favorites.service;

import evaluacion3.favorites.dto.request.FavoriteRequestDTO;
import evaluacion3.favorites.dto.response.FavoriteResponseDTO;
import evaluacion3.favorites.exception.RecursoNoEncontradoException;
import evaluacion3.favorites.exception.ReglaNegocioException;
import evaluacion3.favorites.model.Favorite;
import evaluacion3.favorites.repository.FavoriteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    private static final Logger log = LoggerFactory.getLogger(FavoriteService.class);

    private final FavoriteRepository favoriteRepository;

    public FavoriteService(FavoriteRepository favoriteRepository) {
        this.favoriteRepository = favoriteRepository;
    }

    @Transactional(readOnly = true)
    public FavoriteResponseDTO obtenerFavoritoPorId(Long id) {
        log.info("Buscando favorito con id {}", id);
        Favorite favorite = favoriteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Favorito", id));
        return mapearAResponse(favorite);
    }

    @Transactional(readOnly = true)
    public List<FavoriteResponseDTO> obtenerFavoritosPorUsuario(Long usuarioId) {
        log.info("Consultando favoritos para el usuario {}", usuarioId);
        List<Favorite> favoritos = favoriteRepository.findByUsuarioId(usuarioId);
        log.info("Se encontraron {} favoritos para el usuario {}", favoritos.size(), usuarioId);
        return favoritos.stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean esFavorito(Long usuarioId, Long peliculaId) {
        log.info("Verificando si la película {} es favorita del usuario {}", peliculaId, usuarioId);
        return favoriteRepository.existsByUsuarioIdAndPeliculaId(usuarioId, peliculaId);
    }

    @Transactional
    public FavoriteResponseDTO agregarFavorito(FavoriteRequestDTO dto) {
        log.info("Intentando agregar favorito: usuarioId={}, peliculaId={}", dto.getUsuarioId(), dto.getPeliculaId());

        if (favoriteRepository.existsByUsuarioIdAndPeliculaId(dto.getUsuarioId(), dto.getPeliculaId())) {
            log.warn("La película {} ya está en la lista de favoritos del usuario {}", dto.getPeliculaId(), dto.getUsuarioId());
            throw new ReglaNegocioException("La película ya está en la lista de favoritos del usuario");
        }

        Favorite favorite = new Favorite();
        favorite.setUsuarioId(dto.getUsuarioId());
        favorite.setPeliculaId(dto.getPeliculaId());
        favorite.setFechaAgregado(LocalDateTime.now());

        Favorite guardado = favoriteRepository.save(favorite);
        log.info("Favorito agregado exitosamente con id {}", guardado.getId());
        return mapearAResponse(guardado);
    }

    @Transactional
    public void eliminarFavoritoPorId(Long id) {
        log.info("Eliminando favorito con id {}", id);
        Favorite favorite = favoriteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Favorito", id));
        favoriteRepository.delete(favorite);
        log.info("Favorito con id {} eliminado exitosamente", id);
    }

    @Transactional
    public void eliminarPorUsuarioYPelicula(Long usuarioId, Long peliculaId) {
        log.info("Eliminando favorito del usuario {} para la película {}", usuarioId, peliculaId);
        Favorite favorite = favoriteRepository.findByUsuarioIdAndPeliculaId(usuarioId, peliculaId)
                .orElseThrow(() -> new ReglaNegocioException("La película no está en la lista de favoritos del usuario"));
        favoriteRepository.delete(favorite);
        log.info("Favorito eliminado exitosamente");
    }

    private FavoriteResponseDTO mapearAResponse(Favorite favorite) {
        FavoriteResponseDTO dto = new FavoriteResponseDTO();
        dto.setId(favorite.getId());
        dto.setUsuarioId(favorite.getUsuarioId());
        dto.setPeliculaId(favorite.getPeliculaId());
        dto.setFechaAgregado(favorite.getFechaAgregado());
        return dto;
    }
}

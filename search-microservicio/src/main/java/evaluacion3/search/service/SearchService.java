package evaluacion3.search.service;

import evaluacion3.search.client.PeliculaClient;
import evaluacion3.search.client.PeliculaSimpleDTO;
import evaluacion3.search.dto.request.SearchRequestDTO;
import evaluacion3.search.dto.response.SearchResponseDTO;
import evaluacion3.search.exception.RecursoNoEncontradoException;
import evaluacion3.search.exception.ReglaNegocioException;
import evaluacion3.search.model.SearchIndex;
import evaluacion3.search.repository.SearchRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    private final SearchRepository searchRepository;
    private final PeliculaClient peliculaClient;

    public SearchService(SearchRepository searchRepository, PeliculaClient peliculaClient) {
        this.searchRepository = searchRepository;
        this.peliculaClient = peliculaClient;
    }

    @Transactional(readOnly = true)
    public List<SearchResponseDTO> buscarPorTermino(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            log.warn("Intento de búsqueda con término vacío");
            throw new ReglaNegocioException("El término de búsqueda no puede estar vacío");
        }
        log.info("Buscando por término: {}", termino);
        List<SearchIndex> resultados = searchRepository.buscarPorTermino(termino.trim());
        log.info("Se encontraron {} resultados para el término '{}'", resultados.size(), termino);
        return resultados.stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SearchResponseDTO> buscarPorAnio(Integer anio) {
        log.info("Buscando películas del año {}", anio);
        List<SearchIndex> resultados = searchRepository.findByAnio(anio);
        log.info("Se encontraron {} resultados para el año {}", resultados.size(), anio);
        return resultados.stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SearchResponseDTO obtenerPorId(Long id) {
        log.info("Buscando índice de búsqueda con id {}", id);
        SearchIndex index = searchRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("SearchIndex", id));
        return mapearAResponse(index);
    }

    @Transactional
    public SearchResponseDTO indexar(SearchRequestDTO dto) {
        log.info("Indexando película con peliculaId {}", dto.getPeliculaId());
        Optional<SearchIndex> existente = searchRepository.findByPeliculaId(dto.getPeliculaId());
        SearchIndex index;
        if (existente.isPresent()) {
            index = existente.get();
            index.setTitulo(dto.getTitulo());
            index.setDirector(dto.getDirector());
            index.setActores(dto.getActores());
            index.setEtiquetas(dto.getEtiquetas());
            index.setAnio(dto.getAnio());
            log.info("Actualizando índice existente con id {}", index.getId());
        } else {
            index = new SearchIndex();
            index.setPeliculaId(dto.getPeliculaId());
            index.setTitulo(dto.getTitulo());
            index.setDirector(dto.getDirector());
            index.setActores(dto.getActores());
            index.setEtiquetas(dto.getEtiquetas());
            index.setAnio(dto.getAnio());
            log.info("Creando nuevo índice para película {}", dto.getPeliculaId());
        }
        SearchIndex guardado = searchRepository.save(index);
        return mapearAResponse(guardado);
    }

    @Transactional
    public SearchResponseDTO sincronizarDesdePelicula(Long peliculaId) {
        log.info("Sincronizando desde película con id {}", peliculaId);
        PeliculaSimpleDTO pelicula;
        try {
            pelicula = peliculaClient.obtenerPeliculaPorId(peliculaId);
        } catch (FeignException | RecursoNoEncontradoException | ReglaNegocioException e) {
            log.warn("Error al obtener datos de la película {} desde pelicula-microservicio: {}", peliculaId, e.getMessage());
            throw new ReglaNegocioException("No se pudo obtener la información de la película con id " + peliculaId);
        }

        Optional<SearchIndex> existente = searchRepository.findByPeliculaId(peliculaId);
        SearchIndex index;
        if (existente.isPresent()) {
            index = existente.get();
            log.info("Actualizando índice existente desde sincronización para película {}", peliculaId);
        } else {
            index = new SearchIndex();
            index.setPeliculaId(peliculaId);
            log.info("Creando nuevo índice desde sincronización para película {}", peliculaId);
        }
        index.setTitulo(pelicula.getTitulo());
        index.setDirector(pelicula.getDirector());
        index.setAnio(pelicula.getAnio());
        index.setActores(null);
        index.setEtiquetas(null);

        SearchIndex guardado = searchRepository.save(index);
        log.info("Sincronización completada para película {}", peliculaId);
        return mapearAResponse(guardado);
    }

    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando índice de búsqueda con id {}", id);
        SearchIndex index = searchRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("SearchIndex", id));
        searchRepository.delete(index);
        log.info("Índice con id {} eliminado exitosamente", id);
    }

    private SearchResponseDTO mapearAResponse(SearchIndex index) {
        SearchResponseDTO dto = new SearchResponseDTO();
        dto.setId(index.getId());
        dto.setPeliculaId(index.getPeliculaId());
        dto.setTitulo(index.getTitulo());
        dto.setDirector(index.getDirector());
        dto.setActores(index.getActores());
        dto.setEtiquetas(index.getEtiquetas());
        dto.setAnio(index.getAnio());
        return dto;
    }
}

package evaluacion2.streamflow.service;

import evaluacion2.streamflow.dto.request.PeliculaRequestDTO;
import evaluacion2.streamflow.dto.response.PeliculaResponseDTO;
import evaluacion2.streamflow.exception.RecursoNoEncontradoException;
import evaluacion2.streamflow.exception.ReglaNegocioException;
import evaluacion2.streamflow.model.Genero;
import evaluacion2.streamflow.model.Pelicula;
import evaluacion2.streamflow.model.Valoracion;
import evaluacion2.streamflow.repository.PeliculaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PeliculaService {

    private static final Logger log = LoggerFactory.getLogger(PeliculaService.class);

    private final PeliculaRepository peliculaRepository;
    private final GeneroService generoService;

    public PeliculaService(PeliculaRepository peliculaRepository, GeneroService generoService) {
        this.peliculaRepository = peliculaRepository;
        this.generoService = generoService;
    }

    @Transactional(readOnly = true)
    public List<PeliculaResponseDTO> obtenerTodasLasPeliculas() {
        log.info("Consultando todas las películas");
        List<Pelicula> peliculas = peliculaRepository.findAll();
        log.info("Se encontraron {} películas", peliculas.size());
        return peliculas.stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PeliculaResponseDTO obtenerPeliculaPorId(Long id) {
        log.info("Buscando película con id {}", id);
        Pelicula pelicula = buscarPeliculaPorId(id);
        log.info("Película '{}' encontrada", pelicula.getTitulo());
        return mapearAResponse(pelicula);
    }

    @Transactional(readOnly = true)
    public List<PeliculaResponseDTO> obtenerPeliculaPorGenero(Long idGenero) {
        log.info("Buscando películas del género con id {}", idGenero);
        generoService.buscarGeneroPorId(idGenero);
        List<Pelicula> peliculas = peliculaRepository.findByGeneroId(idGenero);
        log.info("Se encontraron {} películas para el género id {}", peliculas.size(), idGenero);
        return peliculas.stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PeliculaResponseDTO> obtenerDesdeAnio(Integer anio) {
        log.info("Buscando películas desde el año {}", anio);
        List<Pelicula> peliculas = peliculaRepository.findByAnioEstrenoGreaterThanEqual(anio);
        log.info("Se encontraron {} películas desde {}", peliculas.size(), anio);
        return peliculas.stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PeliculaResponseDTO crearPelicula(PeliculaRequestDTO dto) {
        log.info("Intentando crear película '{}'", dto.getTitulo());

        Genero genero = generoService.buscarGeneroPorId(dto.getIdGenero());

        int anioActual = java.time.Year.now().getValue();
        if (dto.getAnioEstreno() > anioActual + 5) {
            log.warn("Año de estreno {} inválido para la película '{}'", dto.getAnioEstreno(), dto.getTitulo());
            throw new ReglaNegocioException("El año de estreno no puede ser superior a " + (anioActual + 5));
        }

        Pelicula pelicula = new Pelicula();
        pelicula.setTitulo(dto.getTitulo());
        pelicula.setAnioEstreno(dto.getAnioEstreno());
        pelicula.setDuracion(dto.getDuracion());
        pelicula.setGenero(genero);

        Pelicula guardada = peliculaRepository.save(pelicula);
        log.info("Película '{}' creada con id {}", guardada.getTitulo(), guardada.getId());
        return mapearAResponse(guardada);
    }

    @Transactional
    public PeliculaResponseDTO actualizarPelicula(Long id, PeliculaRequestDTO dto) {
        log.info("Actualizando película con id {}", id);
        Pelicula pelicula = buscarPeliculaPorId(id);
        Genero genero = generoService.buscarGeneroPorId(dto.getIdGenero());

        pelicula.setTitulo(dto.getTitulo());
        pelicula.setAnioEstreno(dto.getAnioEstreno());
        pelicula.setDuracion(dto.getDuracion());
        pelicula.setGenero(genero);

        Pelicula actualizada = peliculaRepository.save(pelicula);
        log.info("Película con id {} actualizada correctamente", id);
        return mapearAResponse(actualizada);
    }

    @Transactional
    public void eliminarPelicula(Long id) {
        log.info("Eliminando película con id {}", id);
        Pelicula pelicula = buscarPeliculaPorId(id);
        peliculaRepository.delete(pelicula);
        log.info("Película con id {} eliminada exitosamente", id);
    }

    public Pelicula buscarPeliculaPorId(Long id) {
        return peliculaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Película", id));
    }

    private Double calcularPromedio(List<Valoracion> valoraciones) {
        if (valoraciones == null || valoraciones.isEmpty()) {
            return null;
        }
        return valoraciones.stream()
                .mapToInt(Valoracion::getPuntaje)
                .average()
                .orElse(0.0);
    }

    private PeliculaResponseDTO mapearAResponse(Pelicula pelicula) {
        PeliculaResponseDTO dto = new PeliculaResponseDTO();
        dto.setId(pelicula.getId());
        dto.setTitulo(pelicula.getTitulo());
        dto.setAnioEstreno(pelicula.getAnioEstreno());
        dto.setDuracion(pelicula.getDuracion());
        dto.setNombreGenero(pelicula.getGenero() != null ? pelicula.getGenero().getNombre() : null);
        dto.setValoracion(calcularPromedio(pelicula.getValoraciones()));
        return dto;
    }
}

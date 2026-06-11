package evaluacion2.pelicula.service;

import evaluacion2.pelicula.client.GeneroClient;
import evaluacion2.pelicula.client.ValoracionClient;
import evaluacion2.pelicula.dto.request.PeliculaRequestDTO;
import evaluacion2.pelicula.dto.response.PeliculaResponseDTO;
import evaluacion2.pelicula.dto.response.ValoracionResponseDTO;
import evaluacion2.pelicula.exception.RecursoNoEncontradoException;
import evaluacion2.pelicula.exception.ReglaNegocioException;
import evaluacion2.pelicula.model.Pelicula;
import evaluacion2.pelicula.repository.PeliculaRepository;
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
    private final GeneroClient generoClient;
    private final ValoracionClient valoracionClient;

    public PeliculaService(PeliculaRepository peliculaRepository, GeneroClient generoClient, ValoracionClient valoracionClient) {
        this.peliculaRepository = peliculaRepository;
        this.generoClient = generoClient;
        this.valoracionClient = valoracionClient;
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
        generoClient.obtenerGeneroPorId(idGenero);
        List<Pelicula> peliculas = peliculaRepository.findByIdGenero(idGenero);
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

        generoClient.obtenerGeneroPorId(dto.getIdGenero());

        int anioActual = java.time.Year.now().getValue();
        if (dto.getAnioEstreno() > anioActual + 5) {
            log.warn("Año de estreno {} inválido para la película '{}'", dto.getAnioEstreno(), dto.getTitulo());
            throw new ReglaNegocioException("El año de estreno no puede ser superior a " + (anioActual + 5));
        }

        Pelicula pelicula = new Pelicula();
        pelicula.setTitulo(dto.getTitulo());
        pelicula.setAnioEstreno(dto.getAnioEstreno());
        pelicula.setDuracion(dto.getDuracion());
        pelicula.setIdGenero(dto.getIdGenero());

        Pelicula guardada = peliculaRepository.save(pelicula);
        log.info("Película '{}' creada con id {}", guardada.getTitulo(), guardada.getId());
        return mapearAResponse(guardada);
    }

    @Transactional
    public PeliculaResponseDTO actualizarPelicula(Long id, PeliculaRequestDTO dto) {
        log.info("Actualizando película con id {}", id);
        Pelicula pelicula = buscarPeliculaPorId(id);
        generoClient.obtenerGeneroPorId(dto.getIdGenero());

        pelicula.setTitulo(dto.getTitulo());
        pelicula.setAnioEstreno(dto.getAnioEstreno());
        pelicula.setDuracion(dto.getDuracion());
        pelicula.setIdGenero(dto.getIdGenero());

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

    private Double calcularPromedio(List<ValoracionResponseDTO> valoraciones) {
        if (valoraciones == null || valoraciones.isEmpty()) {
            return null;
        }
        return valoraciones.stream()
                .mapToInt(ValoracionResponseDTO::getPuntaje)
                .average()
                .orElse(0.0);
    }

    private PeliculaResponseDTO mapearAResponse(Pelicula pelicula) {
        PeliculaResponseDTO dto = new PeliculaResponseDTO();
        dto.setId(pelicula.getId());
        dto.setTitulo(pelicula.getTitulo());
        dto.setAnioEstreno(pelicula.getAnioEstreno());
        dto.setDuracion(pelicula.getDuracion());
        try {
            var genero = generoClient.obtenerGeneroPorId(pelicula.getIdGenero());
            dto.setNombreGenero(genero.getNombre());
        } catch (Exception e) {
            log.warn("No se pudo obtener el género para la película {}", pelicula.getId());
        }
        try {
            List<ValoracionResponseDTO> valoraciones = valoracionClient.obtenerPorPelicula(pelicula.getId());
            dto.setValoracion(calcularPromedio(valoraciones));
        } catch (Exception e) {
            log.warn("No se pudieron obtener las valoraciones para la película {}", pelicula.getId());
        }
        return dto;
    }
}

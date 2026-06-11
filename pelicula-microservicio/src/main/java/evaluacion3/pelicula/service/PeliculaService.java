package evaluacion3.pelicula.service;

import evaluacion3.pelicula.client.GeneroClient;
import evaluacion3.pelicula.client.ValoracionClient;
import evaluacion3.pelicula.dto.request.PeliculaRequestDTO;
import evaluacion3.pelicula.dto.response.PeliculaResponseDTO;
import evaluacion3.pelicula.dto.response.ValoracionResponseDTO;
import evaluacion3.pelicula.exception.RecursoNoEncontradoException;
import evaluacion3.pelicula.exception.ReglaNegocioException;
import evaluacion3.pelicula.model.Pelicula;
import evaluacion3.pelicula.repository.PeliculaRepository;
import feign.FeignException;
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
        try {
            generoClient.obtenerGeneroPorId(idGenero);
        } catch (RecursoNoEncontradoException e) {
            throw new RecursoNoEncontradoException("Género", idGenero);
        } catch (FeignException e) {
            log.error("Error de comunicación con genero-microservicio: {}", e.getMessage());
            throw new RuntimeException("No se pudo validar el género. Intente nuevamente.");
        }
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
        String tituloNormalizado = dto.getTitulo().trim();
        log.info("Intentando crear película '{}'", tituloNormalizado);

        try {
            generoClient.obtenerGeneroPorId(dto.getIdGenero());
        } catch (RecursoNoEncontradoException e) {
            throw new RecursoNoEncontradoException("Género", dto.getIdGenero());
        } catch (FeignException e) {
            log.error("Error de comunicación con genero-microservicio: {}", e.getMessage());
            throw new RuntimeException("No se pudo validar el género. Intente nuevamente.");
        }

        if (peliculaRepository.findByTituloAndAnioEstreno(tituloNormalizado, dto.getAnioEstreno()).isPresent()) {
            log.warn("Ya existe una película con título '{}' y año {}", tituloNormalizado, dto.getAnioEstreno());
            throw new ReglaNegocioException("Ya existe una película con el título '" + tituloNormalizado + "' del año " + dto.getAnioEstreno());
        }

        int anioActual = java.time.Year.now().getValue();
        if (dto.getAnioEstreno() > anioActual + 5) {
            log.warn("Año de estreno {} inválido para la película '{}'", dto.getAnioEstreno(), tituloNormalizado);
            throw new ReglaNegocioException("El año de estreno no puede ser superior a " + (anioActual + 5));
        }

        Pelicula pelicula = new Pelicula();
        pelicula.setTitulo(tituloNormalizado);
        pelicula.setAnioEstreno(dto.getAnioEstreno());
        pelicula.setDuracion(dto.getDuracion());
        pelicula.setIdGenero(dto.getIdGenero());

        Pelicula guardada = peliculaRepository.save(pelicula);
        log.info("Película '{}' creada con id {}", guardada.getTitulo(), guardada.getId());
        return mapearAResponse(guardada);
    }

    @Transactional
    public PeliculaResponseDTO actualizarPelicula(Long id, PeliculaRequestDTO dto) {
        String tituloNormalizado = dto.getTitulo().trim();
        log.info("Actualizando película con id {}", id);
        Pelicula pelicula = buscarPeliculaPorId(id);

        try {
            generoClient.obtenerGeneroPorId(dto.getIdGenero());
        } catch (RecursoNoEncontradoException e) {
            throw new RecursoNoEncontradoException("Género", dto.getIdGenero());
        } catch (FeignException e) {
            log.error("Error de comunicación con genero-microservicio: {}", e.getMessage());
            throw new RuntimeException("No se pudo validar el género. Intente nuevamente.");
        }

        if (!pelicula.getTitulo().equalsIgnoreCase(tituloNormalizado) || !pelicula.getAnioEstreno().equals(dto.getAnioEstreno())) {
            if (peliculaRepository.findByTituloAndAnioEstreno(tituloNormalizado, dto.getAnioEstreno()).isPresent()) {
                log.warn("Ya existe otra película con título '{}' y año {}", tituloNormalizado, dto.getAnioEstreno());
                throw new ReglaNegocioException("Ya existe otra película con el título '" + tituloNormalizado + "' del año " + dto.getAnioEstreno());
            }
        }

        pelicula.setTitulo(tituloNormalizado);
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
            log.warn("No se pudo obtener el género para la película {}: {}", pelicula.getId(), e.getMessage());
        }
        try {
            List<ValoracionResponseDTO> valoraciones = valoracionClient.obtenerPorPelicula(pelicula.getId());
            dto.setValoracion(calcularPromedio(valoraciones));
        } catch (Exception e) {
            log.warn("No se pudieron obtener las valoraciones para la película {}: {}", pelicula.getId(), e.getMessage());
        }
        return dto;
    }
}

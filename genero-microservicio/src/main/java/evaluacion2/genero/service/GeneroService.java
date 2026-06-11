package evaluacion2.genero.service;

import evaluacion2.genero.client.PeliculaClient;
import evaluacion2.genero.dto.request.GeneroRequestDTO;
import evaluacion2.genero.dto.response.GeneroResponseDTO;
import evaluacion2.genero.exception.RecursoNoEncontradoException;
import evaluacion2.genero.exception.ReglaNegocioException;
import evaluacion2.genero.model.Genero;
import evaluacion2.genero.repository.GeneroRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GeneroService {

    private static final Logger log = LoggerFactory.getLogger(GeneroService.class);

    private final GeneroRepository generoRepository;
    private final PeliculaClient peliculaClient;

    public GeneroService(GeneroRepository generoRepository, PeliculaClient peliculaClient) {
        this.generoRepository = generoRepository;
        this.peliculaClient = peliculaClient;
    }

    @Transactional(readOnly = true)
    public List<GeneroResponseDTO> obtenerTodosLosGeneros() {
        log.info("Consultando todos los géneros");
        List<Genero> generos = generoRepository.findAll();
        log.info("Se encontraron {} géneros", generos.size());
        return generos.stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GeneroResponseDTO obtenerGeneroPorId(Long id) {
        log.info("Buscando género con id {}", id);
        Genero genero = buscarGeneroPorId(id);
        log.info("Género '{}' encontrado", genero.getNombre());
        return mapearAResponse(genero);
    }

    @Transactional
    public GeneroResponseDTO crearGenero(GeneroRequestDTO dto) {
        String nombreNormalizado = dto.getNombre().trim();
        log.info("Intentando crear género con nombre '{}'", nombreNormalizado);

        if (!nombreNormalizado.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) {
            log.warn("El nombre '{}' contiene caracteres no permitidos", nombreNormalizado);
            throw new ReglaNegocioException("El nombre del género solo puede contener letras y espacios");
        }

        if (generoRepository.existsByNombre(nombreNormalizado)) {
            log.warn("Creación fallida: ya existe un género con el nombre '{}'", nombreNormalizado);
            throw new ReglaNegocioException("Ya existe un género con el nombre: " + nombreNormalizado);
        }

        Genero genero = new Genero();
        genero.setNombre(nombreNormalizado);
        genero.setDescripcion(dto.getDescripcion() != null ? dto.getDescripcion().trim() : null);

        Genero guardado = generoRepository.save(genero);
        log.info("Género creado exitosamente con id {}", guardado.getId());
        return mapearAResponse(guardado);
    }

    @Transactional
    public GeneroResponseDTO actualizarGenero(Long id, GeneroRequestDTO dto) {
        String nombreNormalizado = dto.getNombre().trim();
        log.info("Actualizando género con id {}", id);
        Genero genero = buscarGeneroPorId(id);

        if (!nombreNormalizado.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) {
            log.warn("El nombre '{}' contiene caracteres no permitidos", nombreNormalizado);
            throw new ReglaNegocioException("El nombre del género solo puede contener letras y espacios");
        }

        if (!genero.getNombre().equals(nombreNormalizado) && generoRepository.existsByNombre(nombreNormalizado)) {
            log.warn("Actualización fallida: el nombre '{}' ya está en uso", nombreNormalizado);
            throw new ReglaNegocioException("Ya existe un género con el nombre: " + nombreNormalizado);
        }

        genero.setNombre(nombreNormalizado);
        genero.setDescripcion(dto.getDescripcion() != null ? dto.getDescripcion().trim() : null);

        Genero actualizado = generoRepository.save(genero);
        log.info("Género con id {} actualizado correctamente", id);
        return mapearAResponse(actualizado);
    }

    @Transactional
    public void eliminarGenero(Long id) {
        log.info("Eliminando género con id {}", id);
        Genero genero = buscarGeneroPorId(id);

        try {
            List<?> peliculas = peliculaClient.obtenerPeliculasPorGenero(id);
            if (peliculas != null && !peliculas.isEmpty()) {
                log.warn("No se puede eliminar el género '{}' porque tiene {} películas asociadas", genero.getNombre(), peliculas.size());
                throw new ReglaNegocioException(
                        "No se puede eliminar el género '" + genero.getNombre() + "' porque tiene " + peliculas.size() + " película(s) asociada(s)");
            }
        } catch (ReglaNegocioException e) {
            throw e;
        } catch (Exception e) {
            log.warn("No se pudo verificar si el género {} tiene películas asociadas. Se permite la eliminación.", id);
        }

        generoRepository.delete(genero);
        log.info("Género con id {} eliminado exitosamente", id);
    }

    public Genero buscarGeneroPorId(Long id) {
        return generoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Género", id));
    }

    private GeneroResponseDTO mapearAResponse(Genero genero) {
        GeneroResponseDTO dto = new GeneroResponseDTO();
        dto.setId(genero.getId());
        dto.setNombre(genero.getNombre());
        dto.setDescripcion(genero.getDescripcion());
        return dto;
    }
}

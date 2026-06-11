package evaluacion2.genero.service;

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

    public GeneroService(GeneroRepository generoRepository) {
        this.generoRepository = generoRepository;
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
        log.info("Intentando crear género con nombre '{}'", dto.getNombre());

        if (generoRepository.existsByNombre(dto.getNombre())) {
            log.warn("Creación fallida: ya existe un género con el nombre '{}'", dto.getNombre());
            throw new ReglaNegocioException("Ya existe un género con el nombre: " + dto.getNombre());
        }

        Genero genero = new Genero();
        genero.setNombre(dto.getNombre());
        genero.setDescripcion(dto.getDescripcion());

        Genero guardado = generoRepository.save(genero);
        log.info("Género creado exitosamente con id {}", guardado.getId());
        return mapearAResponse(guardado);
    }

    @Transactional
    public GeneroResponseDTO actualizarGenero(Long id, GeneroRequestDTO dto) {
        log.info("Actualizando género con id {}", id);
        Genero genero = buscarGeneroPorId(id);

        if (!genero.getNombre().equals(dto.getNombre()) && generoRepository.existsByNombre(dto.getNombre())) {
            log.warn("Actualización fallida: el nombre '{}' ya está en uso", dto.getNombre());
            throw new ReglaNegocioException("Ya existe un género con el nombre: " + dto.getNombre());
        }

        genero.setNombre(dto.getNombre());
        genero.setDescripcion(dto.getDescripcion());

        Genero actualizado = generoRepository.save(genero);
        log.info("Género con id {} actualizado correctamente", id);
        return mapearAResponse(actualizado);
    }

    @Transactional
    public void eliminarGenero(Long id) {
        log.info("Eliminando género con id {}", id);
        Genero genero = buscarGeneroPorId(id);

        if (genero.getDescripcion() != null && !genero.getDescripcion().isEmpty()) {
            log.warn("No se puede eliminar el género '{}' porque tiene restricciones asociadas", genero.getNombre());
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

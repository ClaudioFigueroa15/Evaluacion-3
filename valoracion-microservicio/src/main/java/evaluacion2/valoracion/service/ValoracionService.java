package evaluacion2.valoracion.service;

import evaluacion2.valoracion.client.PeliculaClient;
import evaluacion2.valoracion.dto.request.ValoracionRequestDTO;
import evaluacion2.valoracion.dto.response.ValoracionResponseDTO;
import evaluacion2.valoracion.exception.RecursoNoEncontradoException;
import evaluacion2.valoracion.exception.ReglaNegocioException;
import evaluacion2.valoracion.model.Valoracion;
import evaluacion2.valoracion.repository.ValoracionRepository;
import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ValoracionService {

    private static final Logger log = LoggerFactory.getLogger(ValoracionService.class);

    private final ValoracionRepository valoracionRepository;
    private final PeliculaClient peliculaClient;

    public ValoracionService(ValoracionRepository valoracionRepository, PeliculaClient peliculaClient) {
        this.valoracionRepository = valoracionRepository;
        this.peliculaClient = peliculaClient;
    }

    @Transactional(readOnly = true)
    public List<ValoracionResponseDTO> obtenerTodasLasValoraciones() {
        log.info("Consultando todas las valoraciones");
        List<Valoracion> valoraciones = valoracionRepository.findAll();
        log.info("Se encontraron {} valoraciones", valoraciones.size());
        return valoraciones.stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ValoracionResponseDTO obtenerValoracionPorId(Long id) {
        log.info("Buscando valoración con id {}", id);
        Valoracion valoracion = buscarValoracionPorId(id);
        log.info("Valoración con id {} encontrada", id);
        return mapearAResponse(valoracion);
    }

    @Transactional(readOnly = true)
    public List<ValoracionResponseDTO> obtenerValoracionPorPelicula(Long idPelicula) {
        log.info("Buscando valoraciones de la película id {}", idPelicula);
        List<Valoracion> valoraciones = valoracionRepository.findByIdPelicula(idPelicula);
        log.info("Se encontraron {} valoraciones para la película id {}", valoraciones.size(), idPelicula);
        return valoraciones.stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ValoracionResponseDTO crearValoracion(ValoracionRequestDTO dto) {
        log.info("Creando valoración para la película id {}", dto.getIdPelicula());

        if (dto.getPuntaje() < 1 || dto.getPuntaje() > 10) {
            log.warn("Puntaje inválido {} para la película id {}", dto.getPuntaje(), dto.getIdPelicula());
            throw new ReglaNegocioException("El puntaje debe estar entre 1 y 10. Valor proporcionado: " + dto.getPuntaje());
        }

        validarExistenciaPelicula(dto.getIdPelicula());

        if (dto.getComentario() != null && dto.getComentario().trim().isEmpty()) {
            dto.setComentario(null);
        }

        Valoracion valoracion = new Valoracion();
        valoracion.setPuntaje(dto.getPuntaje());
        valoracion.setComentario(dto.getComentario() != null ? dto.getComentario().trim() : null);
        valoracion.setIdPelicula(dto.getIdPelicula());

        Valoracion guardada = valoracionRepository.save(valoracion);
        log.info("Valoración creada con id {} (puntaje: {})", guardada.getId(), guardada.getPuntaje());
        return mapearAResponse(guardada);
    }

    @Transactional
    public ValoracionResponseDTO actualizarValoracion(Long id, ValoracionRequestDTO dto) {
        log.info("Actualizando valoración con id {}", id);
        Valoracion valoracion = buscarValoracionPorId(id);

        if (dto.getPuntaje() < 1 || dto.getPuntaje() > 10) {
            log.warn("Puntaje inválido {} para la película id {}", dto.getPuntaje(), dto.getIdPelicula());
            throw new ReglaNegocioException("El puntaje debe estar entre 1 y 10. Valor proporcionado: " + dto.getPuntaje());
        }

        validarExistenciaPelicula(dto.getIdPelicula());

        if (dto.getComentario() != null && dto.getComentario().trim().isEmpty()) {
            dto.setComentario(null);
        }

        valoracion.setPuntaje(dto.getPuntaje());
        valoracion.setComentario(dto.getComentario() != null ? dto.getComentario().trim() : null);
        valoracion.setIdPelicula(dto.getIdPelicula());

        Valoracion actualizada = valoracionRepository.save(valoracion);
        log.info("Valoración con id {} actualizada correctamente", id);
        return mapearAResponse(actualizada);
    }

    @Transactional
    public void eliminarValoracion(Long id) {
        log.info("Eliminando valoración con id {}", id);
        Valoracion valoracion = buscarValoracionPorId(id);
        valoracionRepository.delete(valoracion);
        log.info("Valoración con id {} eliminada exitosamente", id);
    }

    private void validarExistenciaPelicula(Long idPelicula) {
        try {
            peliculaClient.obtenerPeliculaPorId(idPelicula);
        } catch (RecursoNoEncontradoException e) {
            log.warn("La película con id {} no existe en pelicula-microservicio", idPelicula);
            throw new RecursoNoEncontradoException("Película", idPelicula);
        } catch (FeignException e) {
            log.error("Error de comunicación con pelicula-microservicio al validar película {}: {}", idPelicula, e.getMessage());
            throw new RuntimeException("No se pudo validar la existencia de la película. Intente nuevamente.");
        }
    }

    private Valoracion buscarValoracionPorId(Long id) {
        return valoracionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Valoración", id));
    }

    private ValoracionResponseDTO mapearAResponse(Valoracion valoracion) {
        ValoracionResponseDTO dto = new ValoracionResponseDTO();
        dto.setId(valoracion.getId());
        dto.setPuntaje(valoracion.getPuntaje());
        dto.setComentario(valoracion.getComentario());
        dto.setIdPelicula(valoracion.getIdPelicula());
        try {
            var pelicula = peliculaClient.obtenerPeliculaPorId(valoracion.getIdPelicula());
            dto.setTituloPelicula(pelicula.getTitulo());
        } catch (Exception e) {
            log.warn("No se pudo obtener el título para la película {}: {}", valoracion.getIdPelicula(), e.getMessage());
        }
        return dto;
    }
}

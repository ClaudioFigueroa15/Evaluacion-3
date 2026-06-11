package evaluacion2.streamflow.service;

import evaluacion2.streamflow.dto.request.ValoracionRequestDTO;
import evaluacion2.streamflow.dto.response.ValoracionResponseDTO;
import evaluacion2.streamflow.exception.RecursoNoEncontradoException;
import evaluacion2.streamflow.exception.ReglaNegocioException;
import evaluacion2.streamflow.model.Pelicula;
import evaluacion2.streamflow.model.Valoracion;
import evaluacion2.streamflow.repository.ValoracionRepository;
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
    private final PeliculaService peliculaService;

    public ValoracionService(ValoracionRepository valoracionRepository, PeliculaService peliculaService) {
        this.valoracionRepository = valoracionRepository;
        this.peliculaService = peliculaService;
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
        peliculaService.buscarPeliculaPorId(idPelicula);
        List<Valoracion> valoraciones = valoracionRepository.findByPeliculaId(idPelicula);
        log.info("Se encontraron {} valoraciones para la película id {}", valoraciones.size(), idPelicula);
        return valoraciones.stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ValoracionResponseDTO crearValoracion(ValoracionRequestDTO dto) {
        log.info("Creando valoración para la película id {}", dto.getIdPelicula());

        Pelicula pelicula = peliculaService.buscarPeliculaPorId(dto.getIdPelicula());

        if (dto.getPuntaje() < 1 || dto.getPuntaje() > 10) {
            log.warn("Puntaje inválido {} para la película id {}", dto.getPuntaje(), dto.getIdPelicula());
            throw new ReglaNegocioException("El puntaje debe estar entre 1 y 10");
        }

        Valoracion valoracion = new Valoracion();
        valoracion.setPuntaje(dto.getPuntaje());
        valoracion.setComentario(dto.getComentario());
        valoracion.setPelicula(pelicula);

        Valoracion guardada = valoracionRepository.save(valoracion);
        log.info("Valoración creada con id {} (puntaje: {})", guardada.getId(), guardada.getPuntaje());
        return mapearAResponse(guardada);
    }

    @Transactional
    public ValoracionResponseDTO actualizarValoracion(Long id, ValoracionRequestDTO dto) {
        log.info("Actualizando valoración con id {}", id);
        Valoracion valoracion = buscarValoracionPorId(id);
        Pelicula pelicula = peliculaService.buscarPeliculaPorId(dto.getIdPelicula());

        valoracion.setPuntaje(dto.getPuntaje());
        valoracion.setComentario(dto.getComentario());
        valoracion.setPelicula(pelicula);

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

    private Valoracion buscarValoracionPorId(Long id) {
        return valoracionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Valoración", id));
    }

    private ValoracionResponseDTO mapearAResponse(Valoracion valoracion) {
        ValoracionResponseDTO dto = new ValoracionResponseDTO();
        dto.setId(valoracion.getId());
        dto.setPuntaje(valoracion.getPuntaje());
        dto.setComentario(valoracion.getComentario());
        if (valoracion.getPelicula() != null) {
            dto.setIdPelicula(valoracion.getPelicula().getId());
            dto.setTituloPelicula(valoracion.getPelicula().getTitulo());
        }
        return dto;
    }
}

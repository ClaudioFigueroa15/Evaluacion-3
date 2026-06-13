package evaluacion3.notification.service;

import evaluacion3.notification.dto.request.NotificationRequestDTO;
import evaluacion3.notification.dto.response.NotificationResponseDTO;
import evaluacion3.notification.exception.RecursoNoEncontradoException;
import evaluacion3.notification.exception.ReglaNegocioException;
import evaluacion3.notification.model.Notification;
import evaluacion3.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> obtenerTodasPorUsuario(Long usuarioId) {
        log.info("Consultando notificaciones para el usuario {}", usuarioId);
        List<Notification> notificaciones = notificationRepository.findByUsuarioId(usuarioId);
        log.info("Se encontraron {} notificaciones para el usuario {}", notificaciones.size(), usuarioId);
        return notificaciones.stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> obtenerNoLeidas(Long usuarioId) {
        log.info("Consultando notificaciones no leídas para el usuario {}", usuarioId);
        List<Notification> noLeidas = notificationRepository.findByUsuarioIdAndLeidaFalse(usuarioId);
        log.info("Se encontraron {} notificaciones no leídas para el usuario {}", noLeidas.size(), usuarioId);
        return noLeidas.stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public NotificationResponseDTO obtenerPorId(Long id) {
        log.info("Buscando notificación con id {}", id);
        Notification notificacion = buscarNotificacionPorId(id);
        log.info("Notificación {} encontrada", id);
        return mapearAResponse(notificacion);
    }

    @Transactional
    public NotificationResponseDTO crear(NotificationRequestDTO dto) {
        log.info("Creando notificación para el usuario {}", dto.getUsuarioId());

        Notification notificacion = new Notification();
        notificacion.setUsuarioId(dto.getUsuarioId());
        notificacion.setTipo(dto.getTipo());
        notificacion.setTitulo(dto.getTitulo());
        notificacion.setMensaje(dto.getMensaje());
        notificacion.setLeida(false);
        notificacion.setFechaCreacion(LocalDateTime.now());

        Notification guardada = notificationRepository.save(notificacion);
        log.info("Notificación creada exitosamente con id {}", guardada.getId());
        return mapearAResponse(guardada);
    }

    @Transactional
    public NotificationResponseDTO marcarLeida(Long id) {
        log.info("Marcando notificación {} como leída", id);
        Notification notificacion = buscarNotificacionPorId(id);

        if (notificacion.getLeida()) {
            log.warn("La notificación {} ya estaba marcada como leída", id);
            throw new ReglaNegocioException("La notificación ya fue marcada como leída");
        }

        notificacion.setLeida(true);
        notificacion.setFechaLectura(LocalDateTime.now());

        Notification actualizada = notificationRepository.save(notificacion);
        log.info("Notificación {} marcada como leída exitosamente", id);
        return mapearAResponse(actualizada);
    }

    @Transactional
    public int marcarTodasLeidas(Long usuarioId) {
        log.info("Marcando todas las notificaciones como leídas para el usuario {}", usuarioId);
        List<Notification> noLeidas = notificationRepository.findByUsuarioIdAndLeidaFalse(usuarioId);

        if (noLeidas.isEmpty()) {
            log.info("El usuario {} no tiene notificaciones pendientes por marcar", usuarioId);
            return 0;
        }

        for (Notification notificacion : noLeidas) {
            notificacion.setLeida(true);
            notificacion.setFechaLectura(LocalDateTime.now());
        }

        notificationRepository.saveAll(noLeidas);
        log.info("Se marcaron {} notificaciones como leídas para el usuario {}", noLeidas.size(), usuarioId);
        return noLeidas.size();
    }

    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando notificación con id {}", id);
        Notification notificacion = buscarNotificacionPorId(id);
        notificationRepository.delete(notificacion);
        log.info("Notificación con id {} eliminada exitosamente", id);
    }

    private Notification buscarNotificacionPorId(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Notificación", id));
    }

    private NotificationResponseDTO mapearAResponse(Notification notificacion) {
        NotificationResponseDTO dto = new NotificationResponseDTO();
        dto.setId(notificacion.getId());
        dto.setUsuarioId(notificacion.getUsuarioId());
        dto.setTipo(notificacion.getTipo());
        dto.setTitulo(notificacion.getTitulo());
        dto.setMensaje(notificacion.getMensaje());
        dto.setLeida(notificacion.getLeida());
        dto.setFechaCreacion(notificacion.getFechaCreacion());
        dto.setFechaLectura(notificacion.getFechaLectura());
        return dto;
    }
}

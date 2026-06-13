package evaluacion3.subscription.service;

import evaluacion3.subscription.dto.request.SubscriptionRequestDTO;
import evaluacion3.subscription.dto.response.SubscriptionResponseDTO;
import evaluacion3.subscription.exception.RecursoNoEncontradoException;
import evaluacion3.subscription.exception.ReglaNegocioException;
import evaluacion3.subscription.model.Subscription;
import evaluacion3.subscription.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponseDTO> obtenerTodas() {
        log.info("Consultando todas las suscripciones");
        List<Subscription> subscriptions = subscriptionRepository.findAll();
        log.info("Se encontraron {} suscripciones", subscriptions.size());
        return subscriptions.stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SubscriptionResponseDTO obtenerPorId(Long id) {
        log.info("Buscando suscripción con id {}", id);
        Subscription subscription = buscarPorId(id);
        log.info("Suscripción encontrada para usuario {}", subscription.getUsuarioId());
        return mapearAResponse(subscription);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponseDTO> obtenerPorUsuario(Long usuarioId) {
        log.info("Buscando suscripciones del usuario {}", usuarioId);
        List<Subscription> subscriptions = subscriptionRepository.findByUsuarioId(usuarioId);
        log.info("Se encontraron {} suscripciones para el usuario {}", subscriptions.size(), usuarioId);
        return subscriptions.stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public SubscriptionResponseDTO crear(SubscriptionRequestDTO dto) {
        log.info("Intentando crear suscripción para usuario {}", dto.getUsuarioId());

        if (!dto.getFechaVencimiento().isAfter(dto.getFechaInicio())) {
            log.warn("Fecha de vencimiento {} no es posterior a fecha de inicio {}", dto.getFechaVencimiento(), dto.getFechaInicio());
            throw new ReglaNegocioException("La fecha de vencimiento debe ser posterior a la fecha de inicio");
        }

        if (subscriptionRepository.existsByUsuarioIdAndEstado(dto.getUsuarioId(), "ACTIVO")) {
            log.warn("El usuario {} ya tiene una suscripción activa", dto.getUsuarioId());
            throw new ReglaNegocioException("El usuario ya tiene una suscripción activa");
        }

        Subscription subscription = new Subscription();
        subscription.setUsuarioId(dto.getUsuarioId());
        subscription.setPlan(dto.getPlan());
        subscription.setEstado(dto.getEstado());
        subscription.setFechaInicio(dto.getFechaInicio());
        subscription.setFechaVencimiento(dto.getFechaVencimiento());
        subscription.setPrecioMensual(dto.getPrecioMensual());

        Subscription guardado = subscriptionRepository.save(subscription);
        log.info("Suscripción creada exitosamente con id {}", guardado.getId());
        return mapearAResponse(guardado);
    }

    @Transactional
    public SubscriptionResponseDTO actualizar(Long id, SubscriptionRequestDTO dto) {
        log.info("Actualizando suscripción con id {}", id);
        Subscription subscription = buscarPorId(id);

        if (!dto.getFechaVencimiento().isAfter(dto.getFechaInicio())) {
            log.warn("Fecha de vencimiento {} no es posterior a fecha de inicio {}", dto.getFechaVencimiento(), dto.getFechaInicio());
            throw new ReglaNegocioException("La fecha de vencimiento debe ser posterior a la fecha de inicio");
        }

        subscription.setUsuarioId(dto.getUsuarioId());
        subscription.setPlan(dto.getPlan());
        subscription.setEstado(dto.getEstado());
        subscription.setFechaInicio(dto.getFechaInicio());
        subscription.setFechaVencimiento(dto.getFechaVencimiento());
        subscription.setPrecioMensual(dto.getPrecioMensual());

        Subscription actualizado = subscriptionRepository.save(subscription);
        log.info("Suscripción con id {} actualizada correctamente", id);
        return mapearAResponse(actualizado);
    }

    @Transactional
    public SubscriptionResponseDTO cancelarSuscripcion(Long id) {
        log.info("Cancelando suscripción con id {}", id);
        Subscription subscription = buscarPorId(id);
        subscription.setEstado("CANCELADO");
        Subscription cancelada = subscriptionRepository.save(subscription);
        log.info("Suscripción con id {} cancelada exitosamente", id);
        return mapearAResponse(cancelada);
    }

    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando suscripción con id {}", id);
        Subscription subscription = buscarPorId(id);
        subscriptionRepository.delete(subscription);
        log.info("Suscripción con id {} eliminada exitosamente", id);
    }

    private Subscription buscarPorId(Long id) {
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Suscripción", id));
    }

    private SubscriptionResponseDTO mapearAResponse(Subscription subscription) {
        SubscriptionResponseDTO dto = new SubscriptionResponseDTO();
        dto.setId(subscription.getId());
        dto.setUsuarioId(subscription.getUsuarioId());
        dto.setPlan(subscription.getPlan());
        dto.setEstado(subscription.getEstado());
        dto.setFechaInicio(subscription.getFechaInicio());
        dto.setFechaVencimiento(subscription.getFechaVencimiento());
        dto.setPrecioMensual(subscription.getPrecioMensual());
        return dto;
    }
}

package evaluacion3.subscription.controller;

import evaluacion3.subscription.dto.request.SubscriptionRequestDTO;
import evaluacion3.subscription.dto.response.SubscriptionResponseDTO;
import evaluacion3.subscription.exception.RecursoNoEncontradoException;
import evaluacion3.subscription.exception.ReglaNegocioException;
import evaluacion3.subscription.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionControllerTest {

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private SubscriptionController subscriptionController;

    private SubscriptionResponseDTO responseDTO;
    private SubscriptionRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new SubscriptionResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setUsuarioId(10L);
        responseDTO.setPlan("PREMIUM");
        responseDTO.setEstado("ACTIVO");
        responseDTO.setFechaInicio(LocalDate.of(2025, 1, 1));
        responseDTO.setFechaVencimiento(LocalDate.of(2026, 1, 1));
        responseDTO.setPrecioMensual(new BigDecimal("15.99"));

        requestDTO = new SubscriptionRequestDTO();
        requestDTO.setUsuarioId(10L);
        requestDTO.setPlan("PREMIUM");
        requestDTO.setEstado("ACTIVO");
        requestDTO.setFechaInicio(LocalDate.of(2025, 1, 1));
        requestDTO.setFechaVencimiento(LocalDate.of(2026, 1, 1));
        requestDTO.setPrecioMensual(new BigDecimal("15.99"));
    }

    @Test
    @DisplayName("GET / obtiene lista de suscripciones")
    void obtenerTodas() {
        when(subscriptionService.obtenerTodas()).thenReturn(List.of(responseDTO));

        ResponseEntity<List<SubscriptionResponseDTO>> respuesta = subscriptionController.obtenerTodas();

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(respuesta.getBody())).hasSize(1);
    }

    @Test
    @DisplayName("GET /{id} retorna suscripcion por id")
    void obtenerPorId() {
        when(subscriptionService.obtenerPorId(1L)).thenReturn(responseDTO);

        ResponseEntity<SubscriptionResponseDTO> respuesta = subscriptionController.obtenerPorId(1L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(respuesta.getBody()).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("POST / crea suscripcion y retorna 201")
    void crear() {
        when(subscriptionService.crear(requestDTO)).thenReturn(responseDTO);

        ResponseEntity<SubscriptionResponseDTO> respuesta = subscriptionController.crear(requestDTO);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(respuesta.getBody()).getPlan()).isEqualTo("PREMIUM");
    }

    @Test
    @DisplayName("PUT /{id} actualiza suscripcion y retorna 200")
    void actualizar() {
        when(subscriptionService.actualizar(1L, requestDTO)).thenReturn(responseDTO);

        ResponseEntity<SubscriptionResponseDTO> respuesta = subscriptionController.actualizar(1L, requestDTO);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(respuesta.getBody()).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("PATCH /{id}/cancelar cancela suscripcion y retorna 200")
    void cancelar() {
        when(subscriptionService.cancelarSuscripcion(1L)).thenReturn(responseDTO);

        ResponseEntity<SubscriptionResponseDTO> respuesta = subscriptionController.cancelar(1L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(respuesta.getBody()).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("DELETE /{id} elimina suscripcion y retorna 204")
    void eliminar() {
        doNothing().when(subscriptionService).eliminar(1L);

        ResponseEntity<Void> respuesta = subscriptionController.eliminar(1L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("GET /{id} retorna 404 cuando no existe")
    void obtenerPorId_NoExiste() {
        when(subscriptionService.obtenerPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("Suscripción", 99L));

        assertThatThrownBy(() -> subscriptionController.obtenerPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("POST / retorna 409 por regla de negocio")
    void crear_ReglaNegocio() {
        when(subscriptionService.crear(requestDTO))
                .thenThrow(new ReglaNegocioException("El usuario ya tiene una suscripción activa"));

        assertThatThrownBy(() -> subscriptionController.crear(requestDTO))
                .isInstanceOf(ReglaNegocioException.class);
    }
}

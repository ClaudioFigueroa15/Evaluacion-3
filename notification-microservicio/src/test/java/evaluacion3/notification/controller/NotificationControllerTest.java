package evaluacion3.notification.controller;

import evaluacion3.notification.dto.request.NotificationRequestDTO;
import evaluacion3.notification.dto.response.NotificationResponseDTO;
import evaluacion3.notification.exception.RecursoNoEncontradoException;
import evaluacion3.notification.exception.ReglaNegocioException;
import evaluacion3.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private NotificationResponseDTO responseDTO;
    private NotificationRequestDTO requestDTO;
    private final Long NOTIFICATION_ID = 1L;
    private final Long USUARIO_ID = 10L;

    @BeforeEach
    void setUp() {
        responseDTO = new NotificationResponseDTO();
        responseDTO.setId(NOTIFICATION_ID);
        responseDTO.setUsuarioId(USUARIO_ID);
        responseDTO.setTipo("BIENVENIDA");
        responseDTO.setTitulo("Bienvenido");
        responseDTO.setMensaje("Bienvenido a la plataforma");
        responseDTO.setLeida(false);
        responseDTO.setFechaCreacion(LocalDateTime.now());

        requestDTO = new NotificationRequestDTO();
        requestDTO.setUsuarioId(USUARIO_ID);
        requestDTO.setTipo("BIENVENIDA");
        requestDTO.setTitulo("Bienvenido");
        requestDTO.setMensaje("Bienvenido a la plataforma");
    }

    @Test
    @DisplayName("GET /usuario/{usuarioId} obtiene lista de notificaciones por usuario")
    void obtenerTodasPorUsuario() {
        when(notificationService.obtenerTodasPorUsuario(USUARIO_ID)).thenReturn(List.of(responseDTO));

        ResponseEntity<List<NotificationResponseDTO>> respuesta = notificationController.obtenerTodasPorUsuario(USUARIO_ID);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(respuesta.getBody())).hasSize(1);
    }

    @Test
    @DisplayName("GET /usuario/{usuarioId}/no-leidas obtiene no leídas")
    void obtenerNoLeidas() {
        when(notificationService.obtenerNoLeidas(USUARIO_ID)).thenReturn(List.of(responseDTO));

        ResponseEntity<List<NotificationResponseDTO>> respuesta = notificationController.obtenerNoLeidas(USUARIO_ID);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(respuesta.getBody())).hasSize(1);
    }

    @Test
    @DisplayName("GET /{id} retorna notificación por id")
    void obtenerPorId() {
        when(notificationService.obtenerPorId(NOTIFICATION_ID)).thenReturn(responseDTO);

        ResponseEntity<NotificationResponseDTO> respuesta = notificationController.obtenerPorId(NOTIFICATION_ID);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(respuesta.getBody()).getId()).isEqualTo(NOTIFICATION_ID);
    }

    @Test
    @DisplayName("POST / crea notificación y retorna 201")
    void crear() {
        when(notificationService.crear(requestDTO)).thenReturn(responseDTO);

        ResponseEntity<NotificationResponseDTO> respuesta = notificationController.crear(requestDTO);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(respuesta.getBody()).getTitulo()).isEqualTo("Bienvenido");
    }

    @Test
    @DisplayName("PATCH /{id}/leer marca notificación como leída")
    void marcarLeida() {
        when(notificationService.marcarLeida(NOTIFICATION_ID)).thenReturn(responseDTO);

        ResponseEntity<NotificationResponseDTO> respuesta = notificationController.marcarLeida(NOTIFICATION_ID);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("PATCH /usuario/{usuarioId}/leer-todas marca todas como leídas")
    void marcarTodasLeidas() {
        when(notificationService.marcarTodasLeidas(USUARIO_ID)).thenReturn(3);

        ResponseEntity<Map<String, Object>> respuesta = notificationController.marcarTodasLeidas(USUARIO_ID);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).containsEntry("marcadas", 3);
    }

    @Test
    @DisplayName("DELETE /{id} elimina notificación y retorna 204")
    void eliminar() {
        doNothing().when(notificationService).eliminar(NOTIFICATION_ID);

        ResponseEntity<Void> respuesta = notificationController.eliminar(NOTIFICATION_ID);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("GET /{id} retorna 404 cuando no existe")
    void obtenerPorId_NoExiste() {
        when(notificationService.obtenerPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("Notificación", 99L));

        assertThatThrownBy(() -> notificationController.obtenerPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("PATCH /{id}/leer retorna 409 si ya estaba leída")
    void marcarLeida_YaLeida() {
        when(notificationService.marcarLeida(NOTIFICATION_ID))
                .thenThrow(new ReglaNegocioException("La notificación ya fue marcada como leída"));

        assertThatThrownBy(() -> notificationController.marcarLeida(NOTIFICATION_ID))
                .isInstanceOf(ReglaNegocioException.class);
    }
}

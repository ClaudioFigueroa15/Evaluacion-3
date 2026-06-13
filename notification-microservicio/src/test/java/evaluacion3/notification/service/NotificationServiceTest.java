package evaluacion3.notification.service;

import evaluacion3.notification.dto.request.NotificationRequestDTO;
import evaluacion3.notification.dto.response.NotificationResponseDTO;
import evaluacion3.notification.exception.RecursoNoEncontradoException;
import evaluacion3.notification.exception.ReglaNegocioException;
import evaluacion3.notification.model.Notification;
import evaluacion3.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Notification notification;
    private NotificationRequestDTO requestDTO;
    private final Long NOTIFICATION_ID = 1L;
    private final Long USUARIO_ID = 10L;

    @BeforeEach
    void setUp() {
        notification = new Notification();
        notification.setId(NOTIFICATION_ID);
        notification.setUsuarioId(USUARIO_ID);
        notification.setTipo("BIENVENIDA");
        notification.setTitulo("Bienvenido");
        notification.setMensaje("Bienvenido a la plataforma");
        notification.setLeida(false);
        notification.setFechaCreacion(LocalDateTime.now());

        requestDTO = new NotificationRequestDTO();
        requestDTO.setUsuarioId(USUARIO_ID);
        requestDTO.setTipo("BIENVENIDA");
        requestDTO.setTitulo("Bienvenido");
        requestDTO.setMensaje("Bienvenido a la plataforma");
    }

    @Nested
    @DisplayName("Pruebas de consulta")
    class ConsultaTests {

        @Test
        @DisplayName("obtenerTodasPorUsuario retorna lista de notificaciones")
        void obtenerTodasPorUsuario() {
            when(notificationRepository.findByUsuarioId(USUARIO_ID)).thenReturn(List.of(notification));

            List<NotificationResponseDTO> resultado = notificationService.obtenerTodasPorUsuario(USUARIO_ID);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getTitulo()).isEqualTo("Bienvenido");
        }

        @Test
        @DisplayName("obtenerNoLeidas retorna solo notificaciones no leídas")
        void obtenerNoLeidas() {
            when(notificationRepository.findByUsuarioIdAndLeidaFalse(USUARIO_ID)).thenReturn(List.of(notification));

            List<NotificationResponseDTO> resultado = notificationService.obtenerNoLeidas(USUARIO_ID);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getLeida()).isFalse();
        }

        @Test
        @DisplayName("obtenerPorId retorna notificación cuando existe")
        void obtenerPorId_Existe() {
            when(notificationRepository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(notification));

            NotificationResponseDTO resultado = notificationService.obtenerPorId(NOTIFICATION_ID);

            assertThat(resultado.getId()).isEqualTo(NOTIFICATION_ID);
            assertThat(resultado.getTitulo()).isEqualTo("Bienvenido");
        }

        @Test
        @DisplayName("obtenerPorId lanza RecursoNoEncontradoException cuando no existe")
        void obtenerPorId_NoExiste() {
            when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> notificationService.obtenerPorId(99L))
                    .isInstanceOf(RecursoNoEncontradoException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("Pruebas de creación")
    class CreacionTests {

        @Test
        @DisplayName("crear crea notificación exitosamente")
        void crear_Exito() {
            when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

            NotificationResponseDTO resultado = notificationService.crear(requestDTO);

            assertThat(resultado.getTitulo()).isEqualTo("Bienvenido");
            assertThat(resultado.getUsuarioId()).isEqualTo(USUARIO_ID);
        }

        @Test
        @DisplayName("crear setea fechaCreacion y leida=false")
        void crear_SeteaFechaYLeidaFalse() {
            when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
                Notification n = invocation.getArgument(0);
                n.setId(NOTIFICATION_ID);
                return n;
            });

            NotificationResponseDTO resultado = notificationService.crear(requestDTO);

            assertThat(resultado.getLeida()).isFalse();
            assertThat(resultado.getFechaCreacion()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Pruebas de marcar")
    class MarcarTests {

        @Test
        @DisplayName("marcarLeida marca exitosamente")
        void marcarLeida_Exito() {
            when(notificationRepository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(notification));
            when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

            NotificationResponseDTO resultado = notificationService.marcarLeida(NOTIFICATION_ID);

            assertThat(resultado.getLeida()).isTrue();
            assertThat(resultado.getFechaLectura()).isNotNull();
        }

        @Test
        @DisplayName("marcarLeida lanza ReglaNegocioException si ya estaba leída")
        void marcarLeida_YaEstabaLeida() {
            notification.setLeida(true);
            notification.setFechaLectura(LocalDateTime.now());
            when(notificationRepository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(notification));

            assertThatThrownBy(() -> notificationService.marcarLeida(NOTIFICATION_ID))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("ya fue marcada como leída");
        }

        @Test
        @DisplayName("marcarTodasLeidas marca todas exitosamente")
        void marcarTodasLeidas_Exito() {
            when(notificationRepository.findByUsuarioIdAndLeidaFalse(USUARIO_ID)).thenReturn(List.of(notification));

            int resultado = notificationService.marcarTodasLeidas(USUARIO_ID);

            assertThat(resultado).isEqualTo(1);
            verify(notificationRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("marcarTodasLeidas retorna 0 si todas ya estaban leídas")
        void marcarTodasLeidas_TodasYaLeidas() {
            when(notificationRepository.findByUsuarioIdAndLeidaFalse(USUARIO_ID)).thenReturn(List.of());

            int resultado = notificationService.marcarTodasLeidas(USUARIO_ID);

            assertThat(resultado).isEqualTo(0);
            verify(notificationRepository, never()).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("Pruebas de eliminación")
    class EliminacionTests {

        @Test
        @DisplayName("eliminar elimina notificación exitosamente")
        void eliminar_Exito() {
            when(notificationRepository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(notification));

            notificationService.eliminar(NOTIFICATION_ID);

            verify(notificationRepository).delete(notification);
        }

        @Test
        @DisplayName("eliminar lanza RecursoNoEncontradoException cuando no existe")
        void eliminar_NoExiste() {
            when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> notificationService.eliminar(99L))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }
    }
}

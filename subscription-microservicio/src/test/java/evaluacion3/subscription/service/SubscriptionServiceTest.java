package evaluacion3.subscription.service;

import evaluacion3.subscription.dto.request.SubscriptionRequestDTO;
import evaluacion3.subscription.dto.response.SubscriptionResponseDTO;
import evaluacion3.subscription.exception.RecursoNoEncontradoException;
import evaluacion3.subscription.exception.ReglaNegocioException;
import evaluacion3.subscription.model.Subscription;
import evaluacion3.subscription.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private Subscription subscription;
    private SubscriptionRequestDTO requestDTO;
    private final Long SUBSCRIPTION_ID = 1L;
    private final Long USUARIO_ID = 10L;

    @BeforeEach
    void setUp() {
        subscription = new Subscription();
        subscription.setId(SUBSCRIPTION_ID);
        subscription.setUsuarioId(USUARIO_ID);
        subscription.setPlan("PREMIUM");
        subscription.setEstado("ACTIVO");
        subscription.setFechaInicio(LocalDate.of(2025, 1, 1));
        subscription.setFechaVencimiento(LocalDate.of(2026, 1, 1));
        subscription.setPrecioMensual(new BigDecimal("15.99"));

        requestDTO = new SubscriptionRequestDTO();
        requestDTO.setUsuarioId(USUARIO_ID);
        requestDTO.setPlan("PREMIUM");
        requestDTO.setEstado("ACTIVO");
        requestDTO.setFechaInicio(LocalDate.of(2025, 1, 1));
        requestDTO.setFechaVencimiento(LocalDate.of(2026, 1, 1));
        requestDTO.setPrecioMensual(new BigDecimal("15.99"));
    }

    @Nested
    @DisplayName("Pruebas de consulta")
    class ConsultaTests {

        @Test
        @DisplayName("obtenerTodas retorna lista")
        void obtenerTodos() {
            when(subscriptionRepository.findAll()).thenReturn(List.of(subscription));

            List<SubscriptionResponseDTO> resultado = subscriptionService.obtenerTodas();

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getPlan()).isEqualTo("PREMIUM");
        }

        @Test
        @DisplayName("obtenerPorId retorna suscripcion cuando existe")
        void obtenerPorId_Existe() {
            when(subscriptionRepository.findById(SUBSCRIPTION_ID)).thenReturn(Optional.of(subscription));

            SubscriptionResponseDTO resultado = subscriptionService.obtenerPorId(SUBSCRIPTION_ID);

            assertThat(resultado.getId()).isEqualTo(SUBSCRIPTION_ID);
            assertThat(resultado.getPlan()).isEqualTo("PREMIUM");
        }

        @Test
        @DisplayName("obtenerPorId lanza RecursoNoEncontradoException cuando no existe")
        void obtenerPorId_NoExiste() {
            when(subscriptionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> subscriptionService.obtenerPorId(99L))
                    .isInstanceOf(RecursoNoEncontradoException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("obtenerPorUsuario retorna suscripciones del usuario")
        void obtenerPorUsuario_Existe() {
            when(subscriptionRepository.findByUsuarioId(USUARIO_ID)).thenReturn(List.of(subscription));

            List<SubscriptionResponseDTO> resultado = subscriptionService.obtenerPorUsuario(USUARIO_ID);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getUsuarioId()).isEqualTo(USUARIO_ID);
        }
    }

    @Nested
    @DisplayName("Pruebas de creación")
    class CreacionTests {

        @Test
        @DisplayName("crear crea suscripcion exitosamente")
        void crear_Exito() {
            when(subscriptionRepository.existsByUsuarioIdAndEstado(USUARIO_ID, "ACTIVO")).thenReturn(false);
            when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);

            SubscriptionResponseDTO resultado = subscriptionService.crear(requestDTO);

            assertThat(resultado.getPlan()).isEqualTo("PREMIUM");
            assertThat(resultado.getEstado()).isEqualTo("ACTIVO");
        }

        @Test
        @DisplayName("crear lanza ReglaNegocioException por fecha invalida")
        void crear_FechaInvalida() {
            requestDTO.setFechaVencimiento(LocalDate.of(2024, 1, 1));

            assertThatThrownBy(() -> subscriptionService.crear(requestDTO))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("fecha de vencimiento debe ser posterior");
        }

        @Test
        @DisplayName("crear lanza ReglaNegocioException cuando usuario ya tiene suscripcion activa")
        void crear_UsuarioYaTieneActiva() {
            when(subscriptionRepository.existsByUsuarioIdAndEstado(USUARIO_ID, "ACTIVO")).thenReturn(true);

            assertThatThrownBy(() -> subscriptionService.crear(requestDTO))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("ya tiene una suscripción activa");
        }
    }

    @Nested
    @DisplayName("Pruebas de actualización")
    class ActualizacionTests {

        @Test
        @DisplayName("actualizar actualiza suscripcion exitosamente")
        void actualizar_Exito() {
            when(subscriptionRepository.findById(SUBSCRIPTION_ID)).thenReturn(Optional.of(subscription));
            when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);

            SubscriptionResponseDTO resultado = subscriptionService.actualizar(SUBSCRIPTION_ID, requestDTO);

            assertThat(resultado.getId()).isEqualTo(SUBSCRIPTION_ID);
        }

        @Test
        @DisplayName("actualizar lanza RecursoNoEncontradoException cuando no existe")
        void actualizar_NoExiste() {
            when(subscriptionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> subscriptionService.actualizar(99L, requestDTO))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }

        @Test
        @DisplayName("actualizar lanza ReglaNegocioException por fecha invalida")
        void actualizar_FechaInvalida() {
            requestDTO.setFechaVencimiento(LocalDate.of(2024, 1, 1));
            when(subscriptionRepository.findById(SUBSCRIPTION_ID)).thenReturn(Optional.of(subscription));

            assertThatThrownBy(() -> subscriptionService.actualizar(SUBSCRIPTION_ID, requestDTO))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("fecha de vencimiento debe ser posterior");
        }
    }

    @Nested
    @DisplayName("Pruebas de cancelación")
    class CancelacionTests {

        @Test
        @DisplayName("cancelarSuscripcion cambia estado a CANCELADO")
        void cancelar_Exito() {
            when(subscriptionRepository.findById(SUBSCRIPTION_ID)).thenReturn(Optional.of(subscription));
            when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(invocation -> invocation.getArgument(0));

            SubscriptionResponseDTO resultado = subscriptionService.cancelarSuscripcion(SUBSCRIPTION_ID);

            assertThat(resultado.getEstado()).isEqualTo("CANCELADO");
        }

        @Test
        @DisplayName("cancelarSuscripcion lanza RecursoNoEncontradoException cuando no existe")
        void cancelar_NoExiste() {
            when(subscriptionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> subscriptionService.cancelarSuscripcion(99L))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }
    }

    @Nested
    @DisplayName("Pruebas de eliminación")
    class EliminacionTests {

        @Test
        @DisplayName("eliminar elimina suscripcion exitosamente")
        void eliminar_Exito() {
            when(subscriptionRepository.findById(SUBSCRIPTION_ID)).thenReturn(Optional.of(subscription));

            subscriptionService.eliminar(SUBSCRIPTION_ID);

            verify(subscriptionRepository).delete(subscription);
        }

        @Test
        @DisplayName("eliminar lanza RecursoNoEncontradoException cuando no existe")
        void eliminar_NoExiste() {
            when(subscriptionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> subscriptionService.eliminar(99L))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }
    }
}

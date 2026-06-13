package evaluacion3.watchhistory.service;

import evaluacion3.watchhistory.dto.request.WatchHistoryRequestDTO;
import evaluacion3.watchhistory.dto.response.WatchHistoryResponseDTO;
import evaluacion3.watchhistory.exception.RecursoNoEncontradoException;
import evaluacion3.watchhistory.exception.ReglaNegocioException;
import evaluacion3.watchhistory.model.WatchHistory;
import evaluacion3.watchhistory.repository.WatchHistoryRepository;
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
class WatchHistoryServiceTest {

    @Mock
    private WatchHistoryRepository repository;

    @InjectMocks
    private WatchHistoryService watchHistoryService;

    private WatchHistory history;
    private WatchHistoryRequestDTO requestDTO;
    private final Long ID = 1L;
    private final Long USUARIO_ID = 10L;
    private final Long PELICULA_ID = 100L;
    private final LocalDateTime AHORA = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        history = new WatchHistory(ID, USUARIO_ID, PELICULA_ID, 50, false, AHORA);

        requestDTO = new WatchHistoryRequestDTO();
        requestDTO.setUsuarioId(USUARIO_ID);
        requestDTO.setPeliculaId(PELICULA_ID);
        requestDTO.setProgresoPorcentaje(50);
        requestDTO.setUltimaVezVisto(AHORA);
    }

    @Nested
    @DisplayName("Pruebas de consulta")
    class ConsultaTests {

        @Test
        @DisplayName("obtenerPorId retorna registro cuando existe")
        void obtenerPorId_Existe() {
            when(repository.findById(ID)).thenReturn(Optional.of(history));

            WatchHistoryResponseDTO resultado = watchHistoryService.obtenerPorId(ID);

            assertThat(resultado.getId()).isEqualTo(ID);
            assertThat(resultado.getUsuarioId()).isEqualTo(USUARIO_ID);
            assertThat(resultado.getPeliculaId()).isEqualTo(PELICULA_ID);
            assertThat(resultado.getProgresoPorcentaje()).isEqualTo(50);
            assertThat(resultado.getCompletado()).isFalse();
        }

        @Test
        @DisplayName("obtenerPorId lanza RecursoNoEncontradoException cuando no existe")
        void obtenerPorId_NoExiste() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> watchHistoryService.obtenerPorId(99L))
                    .isInstanceOf(RecursoNoEncontradoException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("obtenerHistorialPorUsuario retorna lista")
        void obtenerHistorialPorUsuario() {
            when(repository.findByUsuarioId(USUARIO_ID)).thenReturn(List.of(history));

            List<WatchHistoryResponseDTO> resultado = watchHistoryService.obtenerHistorialPorUsuario(USUARIO_ID);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getUsuarioId()).isEqualTo(USUARIO_ID);
        }

        @Test
        @DisplayName("obtenerContenidoEnProgreso retorna lista filtrada")
        void obtenerContenidoEnProgreso() {
            when(repository.findByUsuarioIdAndCompletadoFalseAndProgresoPorcentajeGreaterThan(USUARIO_ID, 0))
                    .thenReturn(List.of(history));

            List<WatchHistoryResponseDTO> resultado = watchHistoryService.obtenerContenidoEnProgreso(USUARIO_ID);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getCompletado()).isFalse();
            assertThat(resultado.get(0).getProgresoPorcentaje()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Pruebas de registro")
    class RegistroTests {

        @Test
        @DisplayName("registrarProgreso crea nuevo registro exitosamente")
        void registrar_NuevoExito() {
            when(repository.findByUsuarioIdAndPeliculaId(USUARIO_ID, PELICULA_ID)).thenReturn(Optional.empty());
            when(repository.save(any(WatchHistory.class))).thenReturn(history);

            WatchHistoryResponseDTO resultado = watchHistoryService.registrarProgreso(requestDTO);

            assertThat(resultado.getUsuarioId()).isEqualTo(USUARIO_ID);
            assertThat(resultado.getPeliculaId()).isEqualTo(PELICULA_ID);
            assertThat(resultado.getProgresoPorcentaje()).isEqualTo(50);
            assertThat(resultado.getCompletado()).isFalse();
        }

        @Test
        @DisplayName("registrarProgreso actualiza registro existente cuando ya existe")
        void registrar_ActualizaExistente_Exito() {
            WatchHistory existente = new WatchHistory(ID, USUARIO_ID, PELICULA_ID, 20, false, AHORA.minusDays(1));
            when(repository.findByUsuarioIdAndPeliculaId(USUARIO_ID, PELICULA_ID)).thenReturn(Optional.of(existente));
            when(repository.save(any(WatchHistory.class))).thenReturn(existente);

            WatchHistoryResponseDTO resultado = watchHistoryService.registrarProgreso(requestDTO);

            assertThat(resultado.getProgresoPorcentaje()).isEqualTo(50);
            assertThat(resultado.getUltimaVezVisto()).isEqualTo(AHORA);
            assertThat(resultado.getCompletado()).isFalse();
        }

        @Test
        @DisplayName("registrarProgreso auto-completa cuando progreso es 100")
        void registrar_AutoCompletaCuandoEs100() {
            requestDTO.setProgresoPorcentaje(100);
            WatchHistory completado = new WatchHistory(ID, USUARIO_ID, PELICULA_ID, 100, true, AHORA);

            when(repository.findByUsuarioIdAndPeliculaId(USUARIO_ID, PELICULA_ID)).thenReturn(Optional.empty());
            when(repository.save(any(WatchHistory.class))).thenReturn(completado);

            WatchHistoryResponseDTO resultado = watchHistoryService.registrarProgreso(requestDTO);

            assertThat(resultado.getProgresoPorcentaje()).isEqualTo(100);
            assertThat(resultado.getCompletado()).isTrue();
        }

        @Test
        @DisplayName("registrarProgreso lanza ReglaNegocioException por progreso invalido")
        void registrar_ProgresoInvalido() {
            requestDTO.setProgresoPorcentaje(150);

            assertThatThrownBy(() -> watchHistoryService.registrarProgreso(requestDTO))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("El progreso debe estar entre 0 y 100");
        }
    }

    @Nested
    @DisplayName("Pruebas de eliminación")
    class EliminacionTests {

        @Test
        @DisplayName("eliminar elimina exitosamente")
        void eliminar_Exito() {
            when(repository.findById(ID)).thenReturn(Optional.of(history));

            watchHistoryService.eliminar(ID);

            verify(repository).delete(history);
        }

        @Test
        @DisplayName("eliminar lanza RecursoNoEncontradoException cuando no existe")
        void eliminar_NoExiste() {
            when(repository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> watchHistoryService.eliminar(99L))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }
    }
}

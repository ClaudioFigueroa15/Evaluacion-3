package evaluacion3.watchhistory.controller;

import evaluacion3.watchhistory.dto.request.WatchHistoryRequestDTO;
import evaluacion3.watchhistory.dto.response.WatchHistoryResponseDTO;
import evaluacion3.watchhistory.exception.RecursoNoEncontradoException;
import evaluacion3.watchhistory.exception.ReglaNegocioException;
import evaluacion3.watchhistory.service.WatchHistoryService;
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
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WatchHistoryControllerTest {

    @Mock
    private WatchHistoryService watchHistoryService;

    @InjectMocks
    private WatchHistoryController watchHistoryController;

    private WatchHistoryResponseDTO responseDTO;
    private WatchHistoryRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new WatchHistoryResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setUsuarioId(10L);
        responseDTO.setPeliculaId(100L);
        responseDTO.setProgresoPorcentaje(50);
        responseDTO.setCompletado(false);
        responseDTO.setUltimaVezVisto(LocalDateTime.now());

        requestDTO = new WatchHistoryRequestDTO();
        requestDTO.setUsuarioId(10L);
        requestDTO.setPeliculaId(100L);
        requestDTO.setProgresoPorcentaje(50);
        requestDTO.setUltimaVezVisto(LocalDateTime.now());
    }

    @Test
    @DisplayName("GET /usuario/{usuarioId} obtiene historial por usuario")
    void obtenerHistorialPorUsuario() {
        when(watchHistoryService.obtenerHistorialPorUsuario(10L)).thenReturn(List.of(responseDTO));

        ResponseEntity<List<WatchHistoryResponseDTO>> respuesta = watchHistoryController.obtenerHistorialPorUsuario(10L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(respuesta.getBody())).hasSize(1);
    }

    @Test
    @DisplayName("GET /usuario/{usuarioId}/en-progreso obtiene contenido en progreso")
    void obtenerContenidoEnProgreso() {
        when(watchHistoryService.obtenerContenidoEnProgreso(10L)).thenReturn(List.of(responseDTO));

        ResponseEntity<List<WatchHistoryResponseDTO>> respuesta = watchHistoryController.obtenerContenidoEnProgreso(10L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(respuesta.getBody())).hasSize(1);
    }

    @Test
    @DisplayName("GET /{id} retorna registro por id")
    void obtenerPorId() {
        when(watchHistoryService.obtenerPorId(1L)).thenReturn(responseDTO);

        ResponseEntity<WatchHistoryResponseDTO> respuesta = watchHistoryController.obtenerPorId(1L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(respuesta.getBody()).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("POST / crea registro y retorna 201")
    void registrarProgreso() {
        when(watchHistoryService.registrarProgreso(requestDTO)).thenReturn(responseDTO);

        ResponseEntity<WatchHistoryResponseDTO> respuesta = watchHistoryController.registrarProgreso(requestDTO);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(respuesta.getBody()).getUsuarioId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("DELETE /{id} elimina registro y retorna 204")
    void eliminar() {
        doNothing().when(watchHistoryService).eliminar(1L);

        ResponseEntity<Void> respuesta = watchHistoryController.eliminar(1L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("GET /{id} retorna 404 cuando no existe")
    void obtenerPorId_NoExiste() {
        when(watchHistoryService.obtenerPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("WatchHistory", 99L));

        assertThatThrownBy(() -> watchHistoryController.obtenerPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("POST / retorna 409 por regla de negocio")
    void registrarProgreso_ReglaNegocio() {
        requestDTO.setProgresoPorcentaje(150);
        when(watchHistoryService.registrarProgreso(requestDTO))
                .thenThrow(new ReglaNegocioException("El progreso debe estar entre 0 y 100"));

        assertThatThrownBy(() -> watchHistoryController.registrarProgreso(requestDTO))
                .isInstanceOf(ReglaNegocioException.class);
    }
}

package evaluacion3.valoracion.controller;

import evaluacion3.valoracion.dto.request.ValoracionRequestDTO;
import evaluacion3.valoracion.dto.response.ValoracionResponseDTO;
import evaluacion3.valoracion.exception.RecursoNoEncontradoException;
import evaluacion3.valoracion.exception.ReglaNegocioException;
import evaluacion3.valoracion.service.ValoracionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValoracionControllerTest {

    @Mock
    private ValoracionService valoracionService;

    @InjectMocks
    private ValoracionController valoracionController;

    private ValoracionResponseDTO responseDTO;
    private ValoracionRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new ValoracionResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setPuntaje(8);
        responseDTO.setComentario("Buena película");
        responseDTO.setIdPelicula(1L);

        requestDTO = new ValoracionRequestDTO();
        requestDTO.setPuntaje(8);
        requestDTO.setComentario("Buena película");
        requestDTO.setIdPelicula(1L);
    }

    @Test
    @DisplayName("GET / obtiene lista de valoraciones")
    void obtenerTodas() {
        when(valoracionService.obtenerTodasLasValoraciones()).thenReturn(List.of(responseDTO));

        ResponseEntity<List<ValoracionResponseDTO>> respuesta = valoracionController.obtenerTodasLasValoraciones();

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(respuesta.getBody())).hasSize(1);
    }

    @Test
    @DisplayName("GET /{id} retorna valoracion por id")
    void obtenerPorId() {
        when(valoracionService.obtenerValoracionPorId(1L)).thenReturn(responseDTO);

        ResponseEntity<ValoracionResponseDTO> respuesta = valoracionController.obtenerValoracionPorId(1L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(respuesta.getBody()).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("GET /pelicula/{id} retorna valoraciones por pelicula")
    void obtenerPorPelicula() {
        when(valoracionService.obtenerValoracionPorPelicula(1L)).thenReturn(List.of(responseDTO));

        ResponseEntity<List<ValoracionResponseDTO>> respuesta = valoracionController.obtenerValoracionPorPelicula(1L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(respuesta.getBody())).hasSize(1);
    }

    @Test
    @DisplayName("POST / crea valoracion y retorna 201")
    void crear() {
        when(valoracionService.crearValoracion(requestDTO)).thenReturn(responseDTO);

        ResponseEntity<ValoracionResponseDTO> respuesta = valoracionController.crearValoracion(requestDTO);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(respuesta.getBody()).getPuntaje()).isEqualTo(8);
    }

    @Test
    @DisplayName("PUT /{id} actualiza valoracion y retorna 200")
    void actualizar() {
        when(valoracionService.actualizarValoracion(1L, requestDTO)).thenReturn(responseDTO);

        ResponseEntity<ValoracionResponseDTO> respuesta = valoracionController.actualizarValoracion(1L, requestDTO);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(respuesta.getBody()).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("DELETE /{id} elimina valoracion y retorna 204")
    void eliminar() {
        doNothing().when(valoracionService).eliminarValoracion(1L);

        ResponseEntity<Void> respuesta = valoracionController.eliminarValoracion(1L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("GET /{id} retorna 404 cuando no existe")
    void obtenerPorId_NoExiste() {
        when(valoracionService.obtenerValoracionPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("Valoración", 99L));

        assertThatThrownBy(() -> valoracionController.obtenerValoracionPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("POST / retorna 400 por regla de negocio")
    void crear_ReglaNegocio() {
        when(valoracionService.crearValoracion(requestDTO))
                .thenThrow(new ReglaNegocioException("El puntaje debe estar entre 1 y 10"));

        assertThatThrownBy(() -> valoracionController.crearValoracion(requestDTO))
                .isInstanceOf(ReglaNegocioException.class);
    }
}

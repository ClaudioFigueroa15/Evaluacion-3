package evaluacion3.genero.controller;

import evaluacion3.genero.dto.request.GeneroRequestDTO;
import evaluacion3.genero.dto.response.GeneroResponseDTO;
import evaluacion3.genero.exception.RecursoNoEncontradoException;
import evaluacion3.genero.exception.ReglaNegocioException;
import evaluacion3.genero.service.GeneroService;
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
class GeneroControllerTest {

    @Mock
    private GeneroService generoService;

    @InjectMocks
    private GeneroController generoController;

    private GeneroResponseDTO responseDTO;
    private GeneroRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new GeneroResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setNombre("Acción");
        responseDTO.setDescripcion("Películas de acción");

        requestDTO = new GeneroRequestDTO();
        requestDTO.setNombre("Acción");
        requestDTO.setDescripcion("Películas de acción");
    }

    @Test
    @DisplayName("GET / obtiene lista de generos")
    void obtenerTodos() {
        when(generoService.obtenerTodosLosGeneros()).thenReturn(List.of(responseDTO));

        ResponseEntity<List<GeneroResponseDTO>> respuesta = generoController.obtenerTodos();

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(respuesta.getBody())).hasSize(1);
    }

    @Test
    @DisplayName("GET /{id} retorna genero por id")
    void obtenerPorId() {
        when(generoService.obtenerGeneroPorId(1L)).thenReturn(responseDTO);

        ResponseEntity<GeneroResponseDTO> respuesta = generoController.obtenerPorId(1L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(respuesta.getBody()).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("POST / crea genero y retorna 201")
    void crear() {
        when(generoService.crearGenero(requestDTO)).thenReturn(responseDTO);

        ResponseEntity<GeneroResponseDTO> respuesta = generoController.crear(requestDTO);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(respuesta.getBody()).getNombre()).isEqualTo("Acción");
    }

    @Test
    @DisplayName("PUT /{id} actualiza genero y retorna 200")
    void actualizar() {
        when(generoService.actualizarGenero(1L, requestDTO)).thenReturn(responseDTO);

        ResponseEntity<GeneroResponseDTO> respuesta = generoController.actualizar(1L, requestDTO);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(respuesta.getBody()).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("DELETE /{id} elimina genero y retorna 204")
    void eliminar() {
        doNothing().when(generoService).eliminarGenero(1L);

        ResponseEntity<Void> respuesta = generoController.eliminar(1L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("GET /{id} retorna 404 cuando no existe")
    void obtenerPorId_NoExiste() {
        when(generoService.obtenerGeneroPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("Género", 99L));

        assertThatThrownBy(() -> generoController.obtenerPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("POST / retorna 400 por regla de negocio")
    void crear_ReglaNegocio() {
        when(generoService.crearGenero(requestDTO))
                .thenThrow(new ReglaNegocioException("Ya existe un género"));

        assertThatThrownBy(() -> generoController.crear(requestDTO))
                .isInstanceOf(ReglaNegocioException.class);
    }
}

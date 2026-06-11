package evaluacion3.pelicula.controller;

import evaluacion3.pelicula.dto.request.PeliculaRequestDTO;
import evaluacion3.pelicula.dto.response.PeliculaResponseDTO;
import evaluacion3.pelicula.exception.RecursoNoEncontradoException;
import evaluacion3.pelicula.exception.ReglaNegocioException;
import evaluacion3.pelicula.service.PeliculaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
class PeliculaControllerTest {

    @Mock
    private PeliculaService peliculaService;

    @InjectMocks
    private PeliculaController peliculaController;

    private PeliculaResponseDTO responseDTO;
    private PeliculaRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new PeliculaResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setTitulo("Inception");
        responseDTO.setAnioEstreno(2010);
        responseDTO.setDuracion(148);

        requestDTO = new PeliculaRequestDTO();
        requestDTO.setTitulo("Inception");
        requestDTO.setAnioEstreno(2010);
        requestDTO.setDuracion(148);
        requestDTO.setIdGenero(1L);
    }

    @Test
    @DisplayName("GET / obtiene lista de peliculas")
    void obtenerTodas() {
        when(peliculaService.obtenerTodasLasPeliculas()).thenReturn(List.of(responseDTO));

        ResponseEntity<List<PeliculaResponseDTO>> respuesta = peliculaController.obtenerTodasLasPeliculas();

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(respuesta.getBody())).hasSize(1);
    }

    @Test
    @DisplayName("GET /{id} retorna pelicula por id")
    void obtenerPorId() {
        when(peliculaService.obtenerPeliculaPorId(1L)).thenReturn(responseDTO);

        ResponseEntity<PeliculaResponseDTO> respuesta = peliculaController.obtenerPeliculaPorId(1L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(respuesta.getBody()).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("GET /genero/{id} retorna peliculas por genero")
    void obtenerPorGenero() {
        when(peliculaService.obtenerPeliculaPorGenero(1L)).thenReturn(List.of(responseDTO));

        ResponseEntity<List<PeliculaResponseDTO>> respuesta = peliculaController.obtenerPeliculaPorGenero(1L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(respuesta.getBody())).hasSize(1);
    }

    @Test
    @DisplayName("GET /desde/{anio} retorna peliculas desde año")
    void obtenerDesdeAnio() {
        when(peliculaService.obtenerDesdeAnio(2000)).thenReturn(List.of(responseDTO));

        ResponseEntity<List<PeliculaResponseDTO>> respuesta = peliculaController.obtenerDesdeAnio(2000);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(respuesta.getBody())).hasSize(1);
    }

    @Test
    @DisplayName("POST / crea pelicula y retorna 201")
    void crear() {
        when(peliculaService.crearPelicula(requestDTO)).thenReturn(responseDTO);

        ResponseEntity<PeliculaResponseDTO> respuesta = peliculaController.crearPelicula(requestDTO);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(respuesta.getBody()).getTitulo()).isEqualTo("Inception");
    }

    @Test
    @DisplayName("PUT /{id} actualiza pelicula y retorna 200")
    void actualizar() {
        when(peliculaService.actualizarPelicula(1L, requestDTO)).thenReturn(responseDTO);

        ResponseEntity<PeliculaResponseDTO> respuesta = peliculaController.actualizarPelicula(1L, requestDTO);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(respuesta.getBody()).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("DELETE /{id} elimina pelicula y retorna 204")
    void eliminar() {
        doNothing().when(peliculaService).eliminarPelicula(1L);

        ResponseEntity<Void> respuesta = peliculaController.eliminarPelicula(1L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("GET /{id} retorna 404 cuando no existe")
    void obtenerPorId_NoExiste() {
        when(peliculaService.obtenerPeliculaPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("Película", 99L));

        assertThatThrownBy(() -> peliculaController.obtenerPeliculaPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("POST / retorna 400 por regla de negocio")
    void crear_ReglaNegocio() {
        when(peliculaService.crearPelicula(requestDTO))
                .thenThrow(new ReglaNegocioException("Ya existe una película"));

        assertThatThrownBy(() -> peliculaController.crearPelicula(requestDTO))
                .isInstanceOf(ReglaNegocioException.class);
    }
}

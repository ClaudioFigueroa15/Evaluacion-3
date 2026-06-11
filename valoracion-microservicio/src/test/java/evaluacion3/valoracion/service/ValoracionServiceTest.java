package evaluacion3.valoracion.service;

import evaluacion3.valoracion.client.PeliculaClient;
import evaluacion3.valoracion.dto.request.ValoracionRequestDTO;
import evaluacion3.valoracion.dto.response.PeliculaResponseDTO;
import evaluacion3.valoracion.dto.response.ValoracionResponseDTO;
import evaluacion3.valoracion.exception.RecursoNoEncontradoException;
import evaluacion3.valoracion.exception.ReglaNegocioException;
import evaluacion3.valoracion.model.Valoracion;
import evaluacion3.valoracion.repository.ValoracionRepository;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValoracionServiceTest {

    @Mock
    private ValoracionRepository valoracionRepository;

    @Mock
    private PeliculaClient peliculaClient;

    @InjectMocks
    private ValoracionService valoracionService;

    private Valoracion valoracion;
    private ValoracionRequestDTO requestDTO;
    private PeliculaResponseDTO peliculaResponse;
    private final Long VALORACION_ID = 1L;
    private final Long PELICULA_ID = 1L;

    @BeforeEach
    void setUp() {
        valoracion = new Valoracion(VALORACION_ID, 8, "Buena película", PELICULA_ID);

        requestDTO = new ValoracionRequestDTO();
        requestDTO.setPuntaje(8);
        requestDTO.setComentario("  Buena película  ");
        requestDTO.setIdPelicula(PELICULA_ID);

        peliculaResponse = new PeliculaResponseDTO();
        peliculaResponse.setId(PELICULA_ID);
        peliculaResponse.setTitulo("Inception");
    }

    @Nested
    @DisplayName("Pruebas de consulta")
    class ConsultaTests {

        @Test
        @DisplayName("obtenerTodasLasValoraciones retorna lista")
        void obtenerTodas() {
            when(valoracionRepository.findAll()).thenReturn(List.of(valoracion));
            when(peliculaClient.obtenerPeliculaPorId(PELICULA_ID)).thenReturn(peliculaResponse);

            List<ValoracionResponseDTO> resultado = valoracionService.obtenerTodasLasValoraciones();

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getPuntaje()).isEqualTo(8);
        }

        @Test
        @DisplayName("obtenerValoracionPorId retorna valoracion cuando existe")
        void obtenerPorId_Existe() {
            when(valoracionRepository.findById(VALORACION_ID)).thenReturn(Optional.of(valoracion));
            when(peliculaClient.obtenerPeliculaPorId(PELICULA_ID)).thenReturn(peliculaResponse);

            ValoracionResponseDTO resultado = valoracionService.obtenerValoracionPorId(VALORACION_ID);

            assertThat(resultado.getId()).isEqualTo(VALORACION_ID);
            assertThat(resultado.getPuntaje()).isEqualTo(8);
        }

        @Test
        @DisplayName("obtenerValoracionPorId lanza RecursoNoEncontradoException cuando no existe")
        void obtenerPorId_NoExiste() {
            when(valoracionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> valoracionService.obtenerValoracionPorId(99L))
                    .isInstanceOf(RecursoNoEncontradoException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("obtenerValoracionPorPelicula retorna valoraciones")
        void obtenerPorPelicula() {
            when(valoracionRepository.findByIdPelicula(PELICULA_ID)).thenReturn(List.of(valoracion));
            when(peliculaClient.obtenerPeliculaPorId(PELICULA_ID)).thenReturn(peliculaResponse);

            List<ValoracionResponseDTO> resultado = valoracionService.obtenerValoracionPorPelicula(PELICULA_ID);

            assertThat(resultado).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Pruebas de creación")
    class CreacionTests {

        @Test
        @DisplayName("crearValoracion crea exitosamente")
        void crearValoracion_Exito() {
            when(peliculaClient.obtenerPeliculaPorId(PELICULA_ID)).thenReturn(peliculaResponse);
            when(valoracionRepository.save(any(Valoracion.class))).thenReturn(valoracion);
            when(peliculaClient.obtenerPeliculaPorId(PELICULA_ID)).thenReturn(peliculaResponse);

            ValoracionResponseDTO resultado = valoracionService.crearValoracion(requestDTO);

            assertThat(resultado.getPuntaje()).isEqualTo(8);
            assertThat(resultado.getComentario()).isEqualTo("Buena película");
        }

        @Test
        @DisplayName("crearValoracion lanza ReglaNegocioException por puntaje menor a 1")
        void crearValoracion_PuntajeMenor() {
            requestDTO.setPuntaje(0);

            assertThatThrownBy(() -> valoracionService.crearValoracion(requestDTO))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("El puntaje debe estar entre 1 y 10");
        }

        @Test
        @DisplayName("crearValoracion lanza ReglaNegocioException por puntaje mayor a 10")
        void crearValoracion_PuntajeMayor() {
            requestDTO.setPuntaje(11);

            assertThatThrownBy(() -> valoracionService.crearValoracion(requestDTO))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("El puntaje debe estar entre 1 y 10");
        }

        @Test
        @DisplayName("crearValoracion lanza RecursoNoEncontradoException cuando pelicula no existe")
        void crearValoracion_PeliculaNoExiste() {
            when(peliculaClient.obtenerPeliculaPorId(PELICULA_ID))
                    .thenThrow(new RecursoNoEncontradoException("Película", PELICULA_ID));

            assertThatThrownBy(() -> valoracionService.crearValoracion(requestDTO))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }

        @Test
        @DisplayName("crearValoracion lanza RuntimeException por error Feign")
        void crearValoracion_FeignException() {
            when(peliculaClient.obtenerPeliculaPorId(PELICULA_ID))
                    .thenThrow(mock(FeignException.class));

            assertThatThrownBy(() -> valoracionService.crearValoracion(requestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("No se pudo validar la existencia de la película");
        }

        @Test
        @DisplayName("crearValoracion asigna null a comentario vacio")
        void crearValoracion_ComentarioVacio() {
            requestDTO.setComentario("   ");
            when(peliculaClient.obtenerPeliculaPorId(PELICULA_ID)).thenReturn(peliculaResponse);
            Valoracion valoracionSinComentario = new Valoracion(VALORACION_ID, 8, null, PELICULA_ID);
            when(valoracionRepository.save(any(Valoracion.class))).thenReturn(valoracionSinComentario);
            when(peliculaClient.obtenerPeliculaPorId(PELICULA_ID)).thenReturn(peliculaResponse);

            ValoracionResponseDTO resultado = valoracionService.crearValoracion(requestDTO);

            assertThat(resultado.getComentario()).isNull();
        }
    }

    @Nested
    @DisplayName("Pruebas de actualización")
    class ActualizacionTests {

        @Test
        @DisplayName("actualizarValoracion actualiza exitosamente")
        void actualizarValoracion_Exito() {
            when(valoracionRepository.findById(VALORACION_ID)).thenReturn(Optional.of(valoracion));
            when(peliculaClient.obtenerPeliculaPorId(PELICULA_ID)).thenReturn(peliculaResponse);
            when(valoracionRepository.save(any(Valoracion.class))).thenReturn(valoracion);
            when(peliculaClient.obtenerPeliculaPorId(PELICULA_ID)).thenReturn(peliculaResponse);

            ValoracionResponseDTO resultado = valoracionService.actualizarValoracion(VALORACION_ID, requestDTO);

            assertThat(resultado.getId()).isEqualTo(VALORACION_ID);
        }

        @Test
        @DisplayName("actualizarValoracion lanza RecursoNoEncontradoException cuando no existe")
        void actualizarValoracion_NoExiste() {
            when(valoracionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> valoracionService.actualizarValoracion(99L, requestDTO))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }

        @Test
        @DisplayName("actualizarValoracion lanza ReglaNegocioException por puntaje invalido")
        void actualizarValoracion_PuntajeInvalido() {
            when(valoracionRepository.findById(VALORACION_ID)).thenReturn(Optional.of(valoracion));
            requestDTO.setPuntaje(15);

            assertThatThrownBy(() -> valoracionService.actualizarValoracion(VALORACION_ID, requestDTO))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("El puntaje debe estar entre 1 y 10");
        }
    }

    @Nested
    @DisplayName("Pruebas de eliminación")
    class EliminacionTests {

        @Test
        @DisplayName("eliminarValoracion elimina exitosamente")
        void eliminarValoracion_Exito() {
            when(valoracionRepository.findById(VALORACION_ID)).thenReturn(Optional.of(valoracion));

            valoracionService.eliminarValoracion(VALORACION_ID);

            verify(valoracionRepository).delete(valoracion);
        }

        @Test
        @DisplayName("eliminarValoracion lanza RecursoNoEncontradoException cuando no existe")
        void eliminarValoracion_NoExiste() {
            when(valoracionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> valoracionService.eliminarValoracion(99L))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }
    }
}

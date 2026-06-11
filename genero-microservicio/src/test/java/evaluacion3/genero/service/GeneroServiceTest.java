package evaluacion3.genero.service;

import evaluacion3.genero.client.PeliculaClient;
import evaluacion3.genero.client.PeliculaSimpleDTO;
import evaluacion3.genero.dto.request.GeneroRequestDTO;
import evaluacion3.genero.dto.response.GeneroResponseDTO;
import evaluacion3.genero.exception.RecursoNoEncontradoException;
import evaluacion3.genero.exception.ReglaNegocioException;
import evaluacion3.genero.model.Genero;
import evaluacion3.genero.repository.GeneroRepository;
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
class GeneroServiceTest {

    @Mock
    private GeneroRepository generoRepository;

    @Mock
    private PeliculaClient peliculaClient;

    @InjectMocks
    private GeneroService generoService;

    private Genero genero;
    private GeneroRequestDTO requestDTO;
    private final Long GENERO_ID = 1L;

    @BeforeEach
    void setUp() {
        genero = new Genero(GENERO_ID, "Acción", "Películas de acción");
        requestDTO = new GeneroRequestDTO();
        requestDTO.setNombre("  Acción  ");
        requestDTO.setDescripcion("  Películas de acción  ");
    }

    @Nested
    @DisplayName("Pruebas de consulta")
    class ConsultaTests {

        @Test
        @DisplayName("obtenerTodosLosGeneros retorna lista")
        void obtenerTodos() {
            when(generoRepository.findAll()).thenReturn(List.of(genero));

            List<GeneroResponseDTO> resultado = generoService.obtenerTodosLosGeneros();

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getNombre()).isEqualTo("Acción");
        }

        @Test
        @DisplayName("obtenerGeneroPorId retorna genero cuando existe")
        void obtenerPorId_Existe() {
            when(generoRepository.findById(GENERO_ID)).thenReturn(Optional.of(genero));

            GeneroResponseDTO resultado = generoService.obtenerGeneroPorId(GENERO_ID);

            assertThat(resultado.getId()).isEqualTo(GENERO_ID);
            assertThat(resultado.getNombre()).isEqualTo("Acción");
        }

        @Test
        @DisplayName("obtenerGeneroPorId lanza RecursoNoEncontradoException cuando no existe")
        void obtenerPorId_NoExiste() {
            when(generoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> generoService.obtenerGeneroPorId(99L))
                    .isInstanceOf(RecursoNoEncontradoException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("Pruebas de creación")
    class CreacionTests {

        @Test
        @DisplayName("crearGenero crea exitosamente")
        void crearGenero_Exito() {
            when(generoRepository.existsByNombre("Acción")).thenReturn(false);
            when(generoRepository.save(any(Genero.class))).thenReturn(genero);

            GeneroResponseDTO resultado = generoService.crearGenero(requestDTO);

            assertThat(resultado.getNombre()).isEqualTo("Acción");
            assertThat(resultado.getDescripcion()).isEqualTo("Películas de acción");
        }

        @Test
        @DisplayName("crearGenero lanza ReglaNegocioException por nombre con caracteres invalidos")
        void crearGenero_NombreInvalido() {
            requestDTO.setNombre("Acción 123");

            assertThatThrownBy(() -> generoService.crearGenero(requestDTO))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("solo puede contener letras");
        }

        @Test
        @DisplayName("crearGenero lanza ReglaNegocioException por nombre duplicado")
        void crearGenero_NombreDuplicado() {
            when(generoRepository.existsByNombre("Acción")).thenReturn(true);

            assertThatThrownBy(() -> generoService.crearGenero(requestDTO))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("Ya existe un género");
        }
    }

    @Nested
    @DisplayName("Pruebas de actualización")
    class ActualizacionTests {

        @Test
        @DisplayName("actualizarGenero actualiza exitosamente")
        void actualizarGenero_Exito() {
            when(generoRepository.findById(GENERO_ID)).thenReturn(Optional.of(genero));
            when(generoRepository.save(any(Genero.class))).thenReturn(genero);

            GeneroResponseDTO resultado = generoService.actualizarGenero(GENERO_ID, requestDTO);

            assertThat(resultado.getId()).isEqualTo(GENERO_ID);
        }

        @Test
        @DisplayName("actualizarGenero lanza RecursoNoEncontradoException cuando no existe")
        void actualizarGenero_NoExiste() {
            when(generoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> generoService.actualizarGenero(99L, requestDTO))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }

        @Test
        @DisplayName("actualizarGenero lanza ReglaNegocioException por nombre invalido")
        void actualizarGenero_NombreInvalido() {
            requestDTO.setNombre("Acción!!!");
            when(generoRepository.findById(GENERO_ID)).thenReturn(Optional.of(genero));

            assertThatThrownBy(() -> generoService.actualizarGenero(GENERO_ID, requestDTO))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("solo puede contener letras");
        }

        @Test
        @DisplayName("actualizarGenero lanza ReglaNegocioException por nombre duplicado")
        void actualizarGenero_NombreDuplicado() {
            requestDTO.setNombre("Comedia");
            when(generoRepository.findById(GENERO_ID)).thenReturn(Optional.of(genero));
            when(generoRepository.existsByNombre("Comedia")).thenReturn(true);

            assertThatThrownBy(() -> generoService.actualizarGenero(GENERO_ID, requestDTO))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("Ya existe un género");
        }
    }

    @Nested
    @DisplayName("Pruebas de eliminación")
    class EliminacionTests {

        @Test
        @DisplayName("eliminarGenero elimina cuando no tiene peliculas")
        void eliminarGenero_SinPeliculas() {
            when(generoRepository.findById(GENERO_ID)).thenReturn(Optional.of(genero));
            when(peliculaClient.obtenerPeliculasPorGenero(GENERO_ID)).thenReturn(List.of());

            generoService.eliminarGenero(GENERO_ID);

            verify(generoRepository).delete(genero);
        }

        @Test
        @DisplayName("eliminarGenero lanza ReglaNegocioException cuando tiene peliculas")
        void eliminarGenero_ConPeliculas() {
            PeliculaSimpleDTO peli = new PeliculaSimpleDTO();
            peli.setId(1L);
            peli.setTitulo("Test");
            when(generoRepository.findById(GENERO_ID)).thenReturn(Optional.of(genero));
            when(peliculaClient.obtenerPeliculasPorGenero(GENERO_ID)).thenReturn(List.of(peli));

            assertThatThrownBy(() -> generoService.eliminarGenero(GENERO_ID))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("No se puede eliminar");
        }

        @Test
        @DisplayName("eliminarGenero elimina cuando Feign lanza RecursoNoEncontradoException")
        void eliminarGenero_FeignRecursoNoEncontrado() {
            when(generoRepository.findById(GENERO_ID)).thenReturn(Optional.of(genero));
            when(peliculaClient.obtenerPeliculasPorGenero(GENERO_ID))
                    .thenThrow(new RecursoNoEncontradoException("Género", GENERO_ID));

            generoService.eliminarGenero(GENERO_ID);

            verify(generoRepository).delete(genero);
        }

        @Test
        @DisplayName("eliminarGenero elimina cuando Feign lanza FeignException")
        void eliminarGenero_FeignException() {
            when(generoRepository.findById(GENERO_ID)).thenReturn(Optional.of(genero));
            when(peliculaClient.obtenerPeliculasPorGenero(GENERO_ID))
                    .thenThrow(mock(FeignException.class));

            generoService.eliminarGenero(GENERO_ID);

            verify(generoRepository).delete(genero);
        }

        @Test
        @DisplayName("eliminarGenero lanza RecursoNoEncontradoException cuando no existe")
        void eliminarGenero_NoExiste() {
            when(generoRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> generoService.eliminarGenero(99L))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }
    }
}

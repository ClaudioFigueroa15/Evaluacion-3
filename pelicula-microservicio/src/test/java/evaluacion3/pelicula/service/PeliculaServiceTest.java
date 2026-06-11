package evaluacion3.pelicula.service;

import evaluacion3.pelicula.client.GeneroClient;
import evaluacion3.pelicula.client.ValoracionClient;
import evaluacion3.pelicula.dto.request.PeliculaRequestDTO;
import evaluacion3.pelicula.dto.response.GeneroResponseDTO;
import evaluacion3.pelicula.dto.response.PeliculaResponseDTO;
import evaluacion3.pelicula.dto.response.ValoracionResponseDTO;
import evaluacion3.pelicula.exception.RecursoNoEncontradoException;
import evaluacion3.pelicula.exception.ReglaNegocioException;
import evaluacion3.pelicula.model.Pelicula;
import evaluacion3.pelicula.repository.PeliculaRepository;
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
class PeliculaServiceTest {

    @Mock
    private PeliculaRepository peliculaRepository;

    @Mock
    private GeneroClient generoClient;

    @Mock
    private ValoracionClient valoracionClient;

    @InjectMocks
    private PeliculaService peliculaService;

    private Pelicula pelicula;
    private PeliculaRequestDTO requestDTO;
    private GeneroResponseDTO generoResponse;
    private final Long GENERO_ID = 1L;
    private final Long PELICULA_ID = 1L;

    @BeforeEach
    void setUp() {
        pelicula = new Pelicula(PELICULA_ID, "Inception", 2010, 148, GENERO_ID);

        requestDTO = new PeliculaRequestDTO();
        requestDTO.setTitulo("  Inception  ");
        requestDTO.setAnioEstreno(2010);
        requestDTO.setDuracion(148);
        requestDTO.setIdGenero(GENERO_ID);

        generoResponse = new GeneroResponseDTO();
        generoResponse.setId(GENERO_ID);
        generoResponse.setNombre("Ciencia Ficción");
    }

    @Nested
    @DisplayName("Pruebas de consulta")
    class ConsultaTests {

        @Test
        @DisplayName("obtenerTodasLasPeliculas retorna lista")
        void obtenerTodas() {
            when(peliculaRepository.findAll()).thenReturn(List.of(pelicula));
            when(generoClient.obtenerGeneroPorId(GENERO_ID)).thenReturn(generoResponse);
            when(valoracionClient.obtenerPorPelicula(PELICULA_ID)).thenReturn(List.of());

            List<PeliculaResponseDTO> resultado = peliculaService.obtenerTodasLasPeliculas();

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getTitulo()).isEqualTo("Inception");
        }

        @Test
        @DisplayName("obtenerPeliculaPorId retorna pelicula cuando existe")
        void obtenerPorId_Existe() {
            when(peliculaRepository.findById(PELICULA_ID)).thenReturn(Optional.of(pelicula));
            when(generoClient.obtenerGeneroPorId(GENERO_ID)).thenReturn(generoResponse);
            when(valoracionClient.obtenerPorPelicula(PELICULA_ID)).thenReturn(List.of());

            PeliculaResponseDTO resultado = peliculaService.obtenerPeliculaPorId(PELICULA_ID);

            assertThat(resultado.getId()).isEqualTo(PELICULA_ID);
            assertThat(resultado.getTitulo()).isEqualTo("Inception");
        }

        @Test
        @DisplayName("obtenerPeliculaPorId lanza RecursoNoEncontradoException cuando no existe")
        void obtenerPorId_NoExiste() {
            when(peliculaRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> peliculaService.obtenerPeliculaPorId(99L))
                    .isInstanceOf(RecursoNoEncontradoException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("obtenerPeliculaPorGenero retorna lista cuando genero existe")
        void obtenerPorGenero_Existe() {
            when(generoClient.obtenerGeneroPorId(GENERO_ID)).thenReturn(generoResponse);
            when(peliculaRepository.findByIdGenero(GENERO_ID)).thenReturn(List.of(pelicula));
            when(generoClient.obtenerGeneroPorId(GENERO_ID)).thenReturn(generoResponse);
            when(valoracionClient.obtenerPorPelicula(PELICULA_ID)).thenReturn(List.of());

            List<PeliculaResponseDTO> resultado = peliculaService.obtenerPeliculaPorGenero(GENERO_ID);

            assertThat(resultado).hasSize(1);
        }

        @Test
        @DisplayName("obtenerPeliculaPorGenero lanza RecursoNoEncontradoException cuando genero Feign falla")
        void obtenerPorGenero_GeneroNoExiste() {
            when(generoClient.obtenerGeneroPorId(GENERO_ID))
                    .thenThrow(new RecursoNoEncontradoException("Género", GENERO_ID));

            assertThatThrownBy(() -> peliculaService.obtenerPeliculaPorGenero(GENERO_ID))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }

        @Test
        @DisplayName("obtenerPeliculaPorGenero lanza RuntimeException cuando Feign falla por comunicación")
        void obtenerPorGenero_FeignException() {
            when(generoClient.obtenerGeneroPorId(GENERO_ID))
                    .thenThrow(mock(FeignException.class));

            assertThatThrownBy(() -> peliculaService.obtenerPeliculaPorGenero(GENERO_ID))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("No se pudo validar el género");
        }

        @Test
        @DisplayName("obtenerDesdeAnio retorna peliculas filtradas")
        void obtenerDesdeAnio() {
            when(peliculaRepository.findByAnioEstrenoGreaterThanEqual(2000)).thenReturn(List.of(pelicula));
            when(generoClient.obtenerGeneroPorId(GENERO_ID)).thenReturn(generoResponse);
            when(valoracionClient.obtenerPorPelicula(PELICULA_ID)).thenReturn(List.of());

            List<PeliculaResponseDTO> resultado = peliculaService.obtenerDesdeAnio(2000);

            assertThat(resultado).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Pruebas de creación")
    class CreacionTests {

        @Test
        @DisplayName("crearPelicula crea exitosamente")
        void crearPelicula_Exito() {
            when(generoClient.obtenerGeneroPorId(GENERO_ID)).thenReturn(generoResponse);
            when(peliculaRepository.findByTituloAndAnioEstreno("Inception", 2010)).thenReturn(Optional.empty());
            when(peliculaRepository.save(any(Pelicula.class))).thenReturn(pelicula);
            when(generoClient.obtenerGeneroPorId(GENERO_ID)).thenReturn(generoResponse);
            when(valoracionClient.obtenerPorPelicula(PELICULA_ID)).thenReturn(List.of());

            PeliculaResponseDTO resultado = peliculaService.crearPelicula(requestDTO);

            assertThat(resultado.getTitulo()).isEqualTo("Inception");
            assertThat(resultado.getAnioEstreno()).isEqualTo(2010);
        }

        @Test
        @DisplayName("crearPelicula lanza ReglaNegocioException por titulo duplicado")
        void crearPelicula_Duplicado() {
            when(generoClient.obtenerGeneroPorId(GENERO_ID)).thenReturn(generoResponse);
            when(peliculaRepository.findByTituloAndAnioEstreno("Inception", 2010))
                    .thenReturn(Optional.of(pelicula));

            assertThatThrownBy(() -> peliculaService.crearPelicula(requestDTO))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("Ya existe una película");
        }

        @Test
        @DisplayName("crearPelicula lanza ReglaNegocioException por año muy futuro")
        void crearPelicula_AnioFuturo() {
            requestDTO.setAnioEstreno(java.time.Year.now().getValue() + 10);
            when(generoClient.obtenerGeneroPorId(GENERO_ID)).thenReturn(generoResponse);
            when(peliculaRepository.findByTituloAndAnioEstreno("Inception", requestDTO.getAnioEstreno()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> peliculaService.crearPelicula(requestDTO))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("año de estreno");
        }

        @Test
        @DisplayName("crearPelicula lanza RecursoNoEncontradoException cuando genero no existe")
        void crearPelicula_GeneroNoExiste() {
            when(generoClient.obtenerGeneroPorId(GENERO_ID))
                    .thenThrow(new RecursoNoEncontradoException("Género", GENERO_ID));

            assertThatThrownBy(() -> peliculaService.crearPelicula(requestDTO))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }

        @Test
        @DisplayName("crearPelicula lanza RuntimeException por error Feign")
        void crearPelicula_FeignException() {
            when(generoClient.obtenerGeneroPorId(GENERO_ID))
                    .thenThrow(mock(FeignException.class));

            assertThatThrownBy(() -> peliculaService.crearPelicula(requestDTO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("No se pudo validar el género");
        }
    }

    @Nested
    @DisplayName("Pruebas de actualización")
    class ActualizacionTests {

        @Test
        @DisplayName("actualizarPelicula actualiza exitosamente")
        void actualizarPelicula_Exito() {
            when(peliculaRepository.findById(PELICULA_ID)).thenReturn(Optional.of(pelicula));
            when(generoClient.obtenerGeneroPorId(GENERO_ID)).thenReturn(generoResponse);
            when(peliculaRepository.save(any(Pelicula.class))).thenReturn(pelicula);
            when(generoClient.obtenerGeneroPorId(GENERO_ID)).thenReturn(generoResponse);
            when(valoracionClient.obtenerPorPelicula(PELICULA_ID)).thenReturn(List.of());

            PeliculaResponseDTO resultado = peliculaService.actualizarPelicula(PELICULA_ID, requestDTO);

            assertThat(resultado.getId()).isEqualTo(PELICULA_ID);
        }

        @Test
        @DisplayName("actualizarPelicula lanza RecursoNoEncontradoException cuando no existe")
        void actualizarPelicula_NoExiste() {
            when(peliculaRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> peliculaService.actualizarPelicula(99L, requestDTO))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }
    }

    @Nested
    @DisplayName("Pruebas de eliminación")
    class EliminacionTests {

        @Test
        @DisplayName("eliminarPelicula elimina exitosamente")
        void eliminarPelicula_Exito() {
            when(peliculaRepository.findById(PELICULA_ID)).thenReturn(Optional.of(pelicula));

            peliculaService.eliminarPelicula(PELICULA_ID);

            verify(peliculaRepository).delete(pelicula);
        }

        @Test
        @DisplayName("eliminarPelicula lanza RecursoNoEncontradoException cuando no existe")
        void eliminarPelicula_NoExiste() {
            when(peliculaRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> peliculaService.eliminarPelicula(99L))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }
    }
}

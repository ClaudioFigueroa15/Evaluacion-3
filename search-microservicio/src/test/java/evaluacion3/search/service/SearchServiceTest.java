package evaluacion3.search.service;

import evaluacion3.search.client.PeliculaClient;
import evaluacion3.search.client.PeliculaSimpleDTO;
import evaluacion3.search.dto.request.SearchRequestDTO;
import evaluacion3.search.dto.response.SearchResponseDTO;
import evaluacion3.search.exception.RecursoNoEncontradoException;
import evaluacion3.search.exception.ReglaNegocioException;
import evaluacion3.search.model.SearchIndex;
import evaluacion3.search.repository.SearchRepository;
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
class SearchServiceTest {

    @Mock
    private SearchRepository searchRepository;

    @Mock
    private PeliculaClient peliculaClient;

    @InjectMocks
    private SearchService searchService;

    private SearchIndex searchIndex;
    private SearchRequestDTO requestDTO;
    private final Long INDEX_ID = 1L;
    private final Long PELICULA_ID = 10L;

    @BeforeEach
    void setUp() {
        searchIndex = new SearchIndex(INDEX_ID, PELICULA_ID, "Test Película", "Director Test", "Actor1, Actor2", "acción, aventura", 2024);

        requestDTO = new SearchRequestDTO();
        requestDTO.setPeliculaId(PELICULA_ID);
        requestDTO.setTitulo("Test Película");
        requestDTO.setDirector("Director Test");
        requestDTO.setActores("Actor1, Actor2");
        requestDTO.setEtiquetas("acción, aventura");
        requestDTO.setAnio(2024);
    }

    @Nested
    @DisplayName("Pruebas de consulta")
    class ConsultaTests {

        @Test
        @DisplayName("buscarPorTermino retorna resultados cuando encuentra coincidencias")
        void buscarPorTermino_Exito() {
            when(searchRepository.buscarPorTermino("Test")).thenReturn(List.of(searchIndex));

            List<SearchResponseDTO> resultado = searchService.buscarPorTermino("Test");

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getTitulo()).isEqualTo("Test Película");
        }

        @Test
        @DisplayName("buscarPorTermino lanza ReglaNegocioException cuando el término está vacío")
        void buscarPorTermino_TerminoVacio() {
            assertThatThrownBy(() -> searchService.buscarPorTermino(""))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("no puede estar vacío");

            assertThatThrownBy(() -> searchService.buscarPorTermino("   "))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("no puede estar vacío");

            assertThatThrownBy(() -> searchService.buscarPorTermino(null))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("no puede estar vacío");
        }

        @Test
        @DisplayName("buscarPorAnio retorna resultados del año especificado")
        void buscarPorAnio_Exito() {
            when(searchRepository.findByAnio(2024)).thenReturn(List.of(searchIndex));

            List<SearchResponseDTO> resultado = searchService.buscarPorAnio(2024);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getAnio()).isEqualTo(2024);
        }

        @Test
        @DisplayName("obtenerPorId retorna índice cuando existe")
        void obtenerPorId_Existe() {
            when(searchRepository.findById(INDEX_ID)).thenReturn(Optional.of(searchIndex));

            SearchResponseDTO resultado = searchService.obtenerPorId(INDEX_ID);

            assertThat(resultado.getId()).isEqualTo(INDEX_ID);
            assertThat(resultado.getTitulo()).isEqualTo("Test Película");
        }

        @Test
        @DisplayName("obtenerPorId lanza RecursoNoEncontradoException cuando no existe")
        void obtenerPorId_NoExiste() {
            when(searchRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> searchService.obtenerPorId(99L))
                    .isInstanceOf(RecursoNoEncontradoException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("Pruebas de indexación")
    class IndexarTests {

        @Test
        @DisplayName("indexar crea un nuevo índice cuando no existe")
        void indexar_NuevoExito() {
            when(searchRepository.findByPeliculaId(PELICULA_ID)).thenReturn(Optional.empty());
            when(searchRepository.save(any(SearchIndex.class))).thenReturn(searchIndex);

            SearchResponseDTO resultado = searchService.indexar(requestDTO);

            assertThat(resultado.getPeliculaId()).isEqualTo(PELICULA_ID);
            assertThat(resultado.getTitulo()).isEqualTo("Test Película");
            verify(searchRepository).save(any(SearchIndex.class));
        }

        @Test
        @DisplayName("indexar actualiza un índice existente")
        void indexar_ActualizaExistente() {
            SearchIndex existente = new SearchIndex(INDEX_ID, PELICULA_ID, "Viejo Título", "Viejo Director", "Viejo Actor", "vieja", 2020);
            when(searchRepository.findByPeliculaId(PELICULA_ID)).thenReturn(Optional.of(existente));
            when(searchRepository.save(any(SearchIndex.class))).thenReturn(searchIndex);

            SearchResponseDTO resultado = searchService.indexar(requestDTO);

            assertThat(resultado.getTitulo()).isEqualTo("Test Película");
            verify(searchRepository).save(any(SearchIndex.class));
        }
    }

    @Nested
    @DisplayName("Pruebas de sincronización")
    class SincronizarTests {

        @Test
        @DisplayName("sincronizarDesdePelicula obtiene datos y crea índice exitosamente")
        void sincronizar_Exito() {
            PeliculaSimpleDTO pelicula = new PeliculaSimpleDTO();
            pelicula.setId(PELICULA_ID);
            pelicula.setTitulo("Película Sincronizada");
            pelicula.setDirector("Director Sincronizado");
            pelicula.setAnio(2023);
            pelicula.setGeneroId(1L);

            when(peliculaClient.obtenerPeliculaPorId(PELICULA_ID)).thenReturn(pelicula);
            when(searchRepository.findByPeliculaId(PELICULA_ID)).thenReturn(Optional.empty());
            when(searchRepository.save(any(SearchIndex.class))).thenAnswer(invocation -> {
                SearchIndex saved = invocation.getArgument(0);
                saved.setId(2L);
                return saved;
            });

            SearchResponseDTO resultado = searchService.sincronizarDesdePelicula(PELICULA_ID);

            assertThat(resultado.getTitulo()).isEqualTo("Película Sincronizada");
            assertThat(resultado.getDirector()).isEqualTo("Director Sincronizado");
            assertThat(resultado.getAnio()).isEqualTo(2023);
            verify(peliculaClient).obtenerPeliculaPorId(PELICULA_ID);
        }

        @Test
        @DisplayName("sincronizarDesdePelicula lanza ReglaNegocioException cuando Feign falla")
        void sincronizar_FeignFalla() {
            when(peliculaClient.obtenerPeliculaPorId(PELICULA_ID))
                    .thenThrow(mock(FeignException.class));

            assertThatThrownBy(() -> searchService.sincronizarDesdePelicula(PELICULA_ID))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("No se pudo obtener");
        }
    }

    @Nested
    @DisplayName("Pruebas de eliminación")
    class EliminacionTests {

        @Test
        @DisplayName("eliminar elimina índice existente")
        void eliminar_Exito() {
            when(searchRepository.findById(INDEX_ID)).thenReturn(Optional.of(searchIndex));

            searchService.eliminar(INDEX_ID);

            verify(searchRepository).delete(searchIndex);
        }

        @Test
        @DisplayName("eliminar lanza RecursoNoEncontradoException cuando no existe")
        void eliminar_NoExiste() {
            when(searchRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> searchService.eliminar(99L))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }
    }
}

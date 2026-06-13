package evaluacion3.search.controller;

import evaluacion3.search.dto.request.SearchRequestDTO;
import evaluacion3.search.dto.response.SearchResponseDTO;
import evaluacion3.search.exception.RecursoNoEncontradoException;
import evaluacion3.search.exception.ReglaNegocioException;
import evaluacion3.search.service.SearchService;
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
class SearchControllerTest {

    @Mock
    private SearchService searchService;

    @InjectMocks
    private SearchController searchController;

    private SearchResponseDTO responseDTO;
    private SearchRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new SearchResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setPeliculaId(10L);
        responseDTO.setTitulo("Test Película");
        responseDTO.setDirector("Director Test");
        responseDTO.setActores("Actor1, Actor2");
        responseDTO.setEtiquetas("acción, aventura");
        responseDTO.setAnio(2024);

        requestDTO = new SearchRequestDTO();
        requestDTO.setPeliculaId(10L);
        requestDTO.setTitulo("Test Película");
        requestDTO.setDirector("Director Test");
        requestDTO.setActores("Actor1, Actor2");
        requestDTO.setEtiquetas("acción, aventura");
        requestDTO.setAnio(2024);
    }

    @Test
    @DisplayName("GET /api/v1/search?termino= busca por término y retorna 200")
    void buscarPorTermino() {
        when(searchService.buscarPorTermino("Test")).thenReturn(List.of(responseDTO));

        ResponseEntity<List<SearchResponseDTO>> respuesta = searchController.buscarPorTermino("Test");

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(respuesta.getBody())).hasSize(1);
    }

    @Test
    @DisplayName("GET /api/v1/search?termino= retorna 404 cuando el término está vacío")
    void buscarPorTermino_Vacio() {
        when(searchService.buscarPorTermino("")).thenThrow(new ReglaNegocioException("El término de búsqueda no puede estar vacío"));

        assertThatThrownBy(() -> searchController.buscarPorTermino(""))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    @DisplayName("GET /api/v1/search/anio/{anio} busca por año y retorna 200")
    void buscarPorAnio() {
        when(searchService.buscarPorAnio(2024)).thenReturn(List.of(responseDTO));

        ResponseEntity<List<SearchResponseDTO>> respuesta = searchController.buscarPorAnio(2024);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(respuesta.getBody())).hasSize(1);
    }

    @Test
    @DisplayName("GET /api/v1/search/{id} retorna índice por id")
    void obtenerPorId() {
        when(searchService.obtenerPorId(1L)).thenReturn(responseDTO);

        ResponseEntity<SearchResponseDTO> respuesta = searchController.obtenerPorId(1L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(respuesta.getBody()).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("GET /api/v1/search/{id} retorna 404 cuando no existe")
    void obtenerPorId_NoExiste() {
        when(searchService.obtenerPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("SearchIndex", 99L));

        assertThatThrownBy(() -> searchController.obtenerPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("POST /api/v1/search/indexar crea índice y retorna 201")
    void indexar() {
        when(searchService.indexar(requestDTO)).thenReturn(responseDTO);

        ResponseEntity<SearchResponseDTO> respuesta = searchController.indexar(requestDTO);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(respuesta.getBody()).getTitulo()).isEqualTo("Test Película");
    }

    @Test
    @DisplayName("POST /api/v1/search/sincronizar/{peliculaId} sincroniza y retorna 200")
    void sincronizar() {
        when(searchService.sincronizarDesdePelicula(10L)).thenReturn(responseDTO);

        ResponseEntity<SearchResponseDTO> respuesta = searchController.sincronizar(10L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(respuesta.getBody()).getPeliculaId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("DELETE /api/v1/search/{id} elimina y retorna 204")
    void eliminar() {
        doNothing().when(searchService).eliminar(1L);

        ResponseEntity<Void> respuesta = searchController.eliminar(1L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}

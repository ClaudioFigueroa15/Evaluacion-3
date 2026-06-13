package evaluacion3.favorites.controller;

import evaluacion3.favorites.dto.request.FavoriteRequestDTO;
import evaluacion3.favorites.dto.response.FavoriteResponseDTO;
import evaluacion3.favorites.exception.RecursoNoEncontradoException;
import evaluacion3.favorites.exception.ReglaNegocioException;
import evaluacion3.favorites.service.FavoriteService;
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
class FavoriteControllerTest {

    @Mock
    private FavoriteService favoriteService;

    @InjectMocks
    private FavoriteController favoriteController;

    private FavoriteResponseDTO responseDTO;
    private FavoriteRequestDTO requestDTO;
    private final Long USUARIO_ID = 10L;
    private final Long PELICULA_ID = 100L;
    private final Long FAVORITE_ID = 1L;

    @BeforeEach
    void setUp() {
        responseDTO = new FavoriteResponseDTO();
        responseDTO.setId(FAVORITE_ID);
        responseDTO.setUsuarioId(USUARIO_ID);
        responseDTO.setPeliculaId(PELICULA_ID);
        responseDTO.setFechaAgregado(LocalDateTime.now());

        requestDTO = new FavoriteRequestDTO();
        requestDTO.setUsuarioId(USUARIO_ID);
        requestDTO.setPeliculaId(PELICULA_ID);
    }

    @Test
    @DisplayName("GET /usuario/{usuarioId} retorna lista de favoritos")
    void obtenerFavoritosPorUsuario() {
        when(favoriteService.obtenerFavoritosPorUsuario(USUARIO_ID)).thenReturn(List.of(responseDTO));

        ResponseEntity<List<FavoriteResponseDTO>> respuesta = favoriteController.obtenerFavoritosPorUsuario(USUARIO_ID);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(respuesta.getBody())).hasSize(1);
    }

    @Test
    @DisplayName("GET /usuario/{usuarioId}/pelicula/{peliculaId}/es-favorito retorna true")
    void esFavorito() {
        when(favoriteService.esFavorito(USUARIO_ID, PELICULA_ID)).thenReturn(true);

        ResponseEntity<Boolean> respuesta = favoriteController.esFavorito(USUARIO_ID, PELICULA_ID);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isTrue();
    }

    @Test
    @DisplayName("GET /{id} retorna favorito por id")
    void obtenerPorId() {
        when(favoriteService.obtenerFavoritoPorId(FAVORITE_ID)).thenReturn(responseDTO);

        ResponseEntity<FavoriteResponseDTO> respuesta = favoriteController.obtenerPorId(FAVORITE_ID);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(respuesta.getBody()).getId()).isEqualTo(FAVORITE_ID);
    }

    @Test
    @DisplayName("POST / crea favorito y retorna 201")
    void agregar() {
        when(favoriteService.agregarFavorito(requestDTO)).thenReturn(responseDTO);

        ResponseEntity<FavoriteResponseDTO> respuesta = favoriteController.agregar(requestDTO);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(respuesta.getBody()).getPeliculaId()).isEqualTo(PELICULA_ID);
    }

    @Test
    @DisplayName("DELETE /{id} elimina favorito y retorna 204")
    void eliminarPorId() {
        doNothing().when(favoriteService).eliminarFavoritoPorId(FAVORITE_ID);

        ResponseEntity<Void> respuesta = favoriteController.eliminarPorId(FAVORITE_ID);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("DELETE /usuario/{usuarioId}/pelicula/{peliculaId} elimina favorito y retorna 204")
    void eliminarPorUsuarioYPelicula() {
        doNothing().when(favoriteService).eliminarPorUsuarioYPelicula(USUARIO_ID, PELICULA_ID);

        ResponseEntity<Void> respuesta = favoriteController.eliminarPorUsuarioYPelicula(USUARIO_ID, PELICULA_ID);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("GET /{id} retorna 404 cuando no existe")
    void obtenerPorId_NoExiste() {
        when(favoriteService.obtenerFavoritoPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("Favorito", 99L));

        assertThatThrownBy(() -> favoriteController.obtenerPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("POST / retorna 409 por regla de negocio")
    void agregar_ReglaNegocio() {
        when(favoriteService.agregarFavorito(requestDTO))
                .thenThrow(new ReglaNegocioException("La película ya está en la lista de favoritos del usuario"));

        assertThatThrownBy(() -> favoriteController.agregar(requestDTO))
                .isInstanceOf(ReglaNegocioException.class);
    }
}

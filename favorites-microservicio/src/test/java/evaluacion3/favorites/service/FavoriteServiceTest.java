package evaluacion3.favorites.service;

import evaluacion3.favorites.dto.request.FavoriteRequestDTO;
import evaluacion3.favorites.dto.response.FavoriteResponseDTO;
import evaluacion3.favorites.exception.RecursoNoEncontradoException;
import evaluacion3.favorites.exception.ReglaNegocioException;
import evaluacion3.favorites.model.Favorite;
import evaluacion3.favorites.repository.FavoriteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @InjectMocks
    private FavoriteService favoriteService;

    private Favorite favorite;
    private FavoriteRequestDTO requestDTO;
    private final Long FAVORITE_ID = 1L;
    private final Long USUARIO_ID = 10L;
    private final Long PELICULA_ID = 100L;

    @BeforeEach
    void setUp() {
        favorite = new Favorite(FAVORITE_ID, USUARIO_ID, PELICULA_ID, LocalDateTime.now());
        requestDTO = new FavoriteRequestDTO();
        requestDTO.setUsuarioId(USUARIO_ID);
        requestDTO.setPeliculaId(PELICULA_ID);
    }

    @Nested
    @DisplayName("Pruebas de consulta")
    class ConsultaTests {

        @Test
        @DisplayName("obtenerFavoritoPorId retorna favorito cuando existe")
        void obtenerPorId_Existe() {
            when(favoriteRepository.findById(FAVORITE_ID)).thenReturn(Optional.of(favorite));

            FavoriteResponseDTO resultado = favoriteService.obtenerFavoritoPorId(FAVORITE_ID);

            assertThat(resultado.getId()).isEqualTo(FAVORITE_ID);
            assertThat(resultado.getUsuarioId()).isEqualTo(USUARIO_ID);
            assertThat(resultado.getPeliculaId()).isEqualTo(PELICULA_ID);
        }

        @Test
        @DisplayName("obtenerFavoritoPorId lanza RecursoNoEncontradoException cuando no existe")
        void obtenerPorId_NoExiste() {
            when(favoriteRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> favoriteService.obtenerFavoritoPorId(99L))
                    .isInstanceOf(RecursoNoEncontradoException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("obtenerFavoritosPorUsuario retorna lista de favoritos")
        void obtenerFavoritosPorUsuario() {
            when(favoriteRepository.findByUsuarioId(USUARIO_ID)).thenReturn(List.of(favorite));

            List<FavoriteResponseDTO> resultado = favoriteService.obtenerFavoritosPorUsuario(USUARIO_ID);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getPeliculaId()).isEqualTo(PELICULA_ID);
        }

        @Test
        @DisplayName("esFavorito retorna true cuando existe")
        void esFavorito_True() {
            when(favoriteRepository.existsByUsuarioIdAndPeliculaId(USUARIO_ID, PELICULA_ID)).thenReturn(true);

            boolean resultado = favoriteService.esFavorito(USUARIO_ID, PELICULA_ID);

            assertThat(resultado).isTrue();
        }

        @Test
        @DisplayName("esFavorito retorna false cuando no existe")
        void esFavorito_False() {
            when(favoriteRepository.existsByUsuarioIdAndPeliculaId(USUARIO_ID, PELICULA_ID)).thenReturn(false);

            boolean resultado = favoriteService.esFavorito(USUARIO_ID, PELICULA_ID);

            assertThat(resultado).isFalse();
        }
    }

    @Nested
    @DisplayName("Pruebas de agregar")
    class AgregarTests {

        @Test
        @DisplayName("agregarFavorito agrega exitosamente")
        void agregar_Exito() {
            when(favoriteRepository.existsByUsuarioIdAndPeliculaId(USUARIO_ID, PELICULA_ID)).thenReturn(false);
            when(favoriteRepository.save(any(Favorite.class))).thenReturn(favorite);

            FavoriteResponseDTO resultado = favoriteService.agregarFavorito(requestDTO);

            assertThat(resultado.getUsuarioId()).isEqualTo(USUARIO_ID);
            assertThat(resultado.getPeliculaId()).isEqualTo(PELICULA_ID);
            assertThat(resultado.getFechaAgregado()).isNotNull();
        }

        @Test
        @DisplayName("agregarFavorito lanza ReglaNegocioException cuando ya existe")
        void agregar_YaExiste() {
            when(favoriteRepository.existsByUsuarioIdAndPeliculaId(USUARIO_ID, PELICULA_ID)).thenReturn(true);

            assertThatThrownBy(() -> favoriteService.agregarFavorito(requestDTO))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("ya está en la lista de favoritos");
        }
    }

    @Nested
    @DisplayName("Pruebas de eliminación")
    class EliminacionTests {

        @Test
        @DisplayName("eliminarFavoritoPorId elimina exitosamente")
        void eliminarPorId_Exito() {
            when(favoriteRepository.findById(FAVORITE_ID)).thenReturn(Optional.of(favorite));

            favoriteService.eliminarFavoritoPorId(FAVORITE_ID);

            verify(favoriteRepository).delete(favorite);
        }

        @Test
        @DisplayName("eliminarFavoritoPorId lanza RecursoNoEncontradoException cuando no existe")
        void eliminarPorId_NoExiste() {
            when(favoriteRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> favoriteService.eliminarFavoritoPorId(99L))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }

        @Test
        @DisplayName("eliminarPorUsuarioYPelicula elimina exitosamente")
        void eliminarPorUsuarioYPelicula_Exito() {
            when(favoriteRepository.findByUsuarioIdAndPeliculaId(USUARIO_ID, PELICULA_ID)).thenReturn(Optional.of(favorite));

            favoriteService.eliminarPorUsuarioYPelicula(USUARIO_ID, PELICULA_ID);

            verify(favoriteRepository).delete(favorite);
        }

        @Test
        @DisplayName("eliminarPorUsuarioYPelicula lanza ReglaNegocioException cuando no existe")
        void eliminarPorUsuarioYPelicula_NoExiste() {
            when(favoriteRepository.findByUsuarioIdAndPeliculaId(USUARIO_ID, PELICULA_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> favoriteService.eliminarPorUsuarioYPelicula(USUARIO_ID, PELICULA_ID))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("no está en la lista de favoritos");
        }
    }
}

package evaluacion3.favorites.controller;

import evaluacion3.favorites.dto.request.FavoriteRequestDTO;
import evaluacion3.favorites.dto.response.FavoriteResponseDTO;
import evaluacion3.favorites.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @Operation(summary = "Obtener favoritos por usuario", description = "Retorna la lista de favoritos de un usuario")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Lista de favoritos obtenida exitosamente")})
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<FavoriteResponseDTO>> obtenerFavoritosPorUsuario(
            @Parameter(description = "ID del usuario") @PathVariable Long usuarioId) {
        return ResponseEntity.ok(favoriteService.obtenerFavoritosPorUsuario(usuarioId));
    }

    @Operation(summary = "Verificar si una película es favorita", description = "Retorna true si la película es favorita del usuario")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Verificación exitosa")})
    @GetMapping("/usuario/{usuarioId}/pelicula/{peliculaId}/es-favorito")
    public ResponseEntity<Boolean> esFavorito(
            @Parameter(description = "ID del usuario") @PathVariable Long usuarioId,
            @Parameter(description = "ID de la película") @PathVariable Long peliculaId) {
        return ResponseEntity.ok(favoriteService.esFavorito(usuarioId, peliculaId));
    }

    @Operation(summary = "Obtener favorito por ID", description = "Retorna un favorito según su identificador único")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Favorito encontrado"), @ApiResponse(responseCode = "404", description = "Favorito no encontrado")})
    @GetMapping("/{id}")
    public ResponseEntity<FavoriteResponseDTO> obtenerPorId(
            @Parameter(description = "ID del favorito") @PathVariable Long id) {
        return ResponseEntity.ok(favoriteService.obtenerFavoritoPorId(id));
    }

    @Operation(summary = "Agregar un favorito", description = "Registra una película como favorita de un usuario")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Favorito agregado exitosamente"), @ApiResponse(responseCode = "400", description = "Datos inválidos"), @ApiResponse(responseCode = "409", description = "La película ya está en favoritos")})
    @PostMapping
    public ResponseEntity<FavoriteResponseDTO> agregar(@Valid @RequestBody FavoriteRequestDTO dto) {
        FavoriteResponseDTO nuevoFavorito = favoriteService.agregarFavorito(dto);
        return new ResponseEntity<>(nuevoFavorito, HttpStatus.CREATED);
    }

    @Operation(summary = "Eliminar un favorito por ID", description = "Elimina un favorito del sistema")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Favorito eliminado"), @ApiResponse(responseCode = "404", description = "Favorito no encontrado")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPorId(
            @Parameter(description = "ID del favorito") @PathVariable Long id) {
        favoriteService.eliminarFavoritoPorId(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Eliminar un favorito por usuario y película", description = "Elimina un favorito según usuario y película")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Favorito eliminado"), @ApiResponse(responseCode = "409", description = "La película no está en favoritos")})
    @DeleteMapping("/usuario/{usuarioId}/pelicula/{peliculaId}")
    public ResponseEntity<Void> eliminarPorUsuarioYPelicula(
            @Parameter(description = "ID del usuario") @PathVariable Long usuarioId,
            @Parameter(description = "ID de la película") @PathVariable Long peliculaId) {
        favoriteService.eliminarPorUsuarioYPelicula(usuarioId, peliculaId);
        return ResponseEntity.noContent().build();
    }
}

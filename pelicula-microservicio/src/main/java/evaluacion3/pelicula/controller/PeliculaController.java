package evaluacion3.pelicula.controller;

import evaluacion3.pelicula.dto.request.PeliculaRequestDTO;
import evaluacion3.pelicula.dto.response.PeliculaResponseDTO;
import evaluacion3.pelicula.service.PeliculaService;
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
@RequestMapping("/api/v1/peliculas")
public class PeliculaController {

    private final PeliculaService peliculaService;

    public PeliculaController(PeliculaService peliculaService) {
        this.peliculaService = peliculaService;
    }

    @Operation(summary = "Obtener todas las películas", description = "Retorna una lista de todas las películas registradas")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Lista de películas obtenida exitosamente")})
    @GetMapping
    public ResponseEntity<List<PeliculaResponseDTO>> obtenerTodasLasPeliculas() {
        return ResponseEntity.ok(peliculaService.obtenerTodasLasPeliculas());
    }

    @Operation(summary = "Obtener película por ID", description = "Retorna una película según su identificador único")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Película encontrada"), @ApiResponse(responseCode = "404", description = "Película no encontrada")})
    @GetMapping("/{id}")
    public ResponseEntity<PeliculaResponseDTO> obtenerPeliculaPorId(@Parameter(description = "ID de la película") @PathVariable Long id) {
        return ResponseEntity.ok(peliculaService.obtenerPeliculaPorId(id));
    }

    @Operation(summary = "Obtener películas por género", description = "Retorna todas las películas que pertenecen a un género específico")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Lista de películas por género obtenida"), @ApiResponse(responseCode = "404", description = "Género no encontrado")})
    @GetMapping("/genero/{idGenero}")
    public ResponseEntity<List<PeliculaResponseDTO>> obtenerPeliculaPorGenero(@Parameter(description = "ID del género") @PathVariable Long idGenero) {
        return ResponseEntity.ok(peliculaService.obtenerPeliculaPorGenero(idGenero));
    }

    @Operation(summary = "Obtener películas desde un año", description = "Retorna todas las películas publicadas a partir de un año específico")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Lista de películas obtenida")})
    @GetMapping("/desde/{anio}")
    public ResponseEntity<List<PeliculaResponseDTO>> obtenerDesdeAnio(@Parameter(description = "Año de publicación") @PathVariable Integer anio) {
        return ResponseEntity.ok(peliculaService.obtenerDesdeAnio(anio));
    }

    @Operation(summary = "Crear una nueva película", description = "Registra una nueva película en el sistema")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Película creada exitosamente"), @ApiResponse(responseCode = "400", description = "Datos inválidos")})
    @PostMapping
    public ResponseEntity<PeliculaResponseDTO> crearPelicula(@Valid @RequestBody PeliculaRequestDTO dto) {
        PeliculaResponseDTO nuevaPelicula = peliculaService.crearPelicula(dto);
        return new ResponseEntity<>(nuevaPelicula, HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar una película", description = "Actualiza los datos de una película existente")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Película actualizada"), @ApiResponse(responseCode = "404", description = "Película no encontrada"), @ApiResponse(responseCode = "400", description = "Datos inválidos")})
    @PutMapping("/{id}")
    public ResponseEntity<PeliculaResponseDTO> actualizarPelicula(
            @Parameter(description = "ID de la película") @PathVariable Long id,
            @Valid @RequestBody PeliculaRequestDTO dto) {
        PeliculaResponseDTO peliculaActualizada = peliculaService.actualizarPelicula(id, dto);
        return ResponseEntity.ok(peliculaActualizada);
    }

    @Operation(summary = "Eliminar una película", description = "Elimina una película del sistema")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Película eliminada"), @ApiResponse(responseCode = "404", description = "Película no encontrada")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPelicula(@Parameter(description = "ID de la película") @PathVariable Long id) {
        peliculaService.eliminarPelicula(id);
        return ResponseEntity.noContent().build();
    }
}

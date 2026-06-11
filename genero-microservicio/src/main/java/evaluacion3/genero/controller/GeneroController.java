package evaluacion3.genero.controller;

import evaluacion3.genero.dto.request.GeneroRequestDTO;
import evaluacion3.genero.dto.response.GeneroResponseDTO;
import evaluacion3.genero.service.GeneroService;
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
@RequestMapping("/api/v1/generos")
public class GeneroController {

    private final GeneroService generoService;

    public GeneroController(GeneroService generoService) {
        this.generoService = generoService;
    }

    @Operation(summary = "Obtener todos los géneros", description = "Retorna una lista de todos los géneros registrados")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Lista de géneros obtenida exitosamente")})
    @GetMapping
    public ResponseEntity<List<GeneroResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(generoService.obtenerTodosLosGeneros());
    }

    @Operation(summary = "Obtener género por ID", description = "Retorna un género según su identificador único")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Género encontrado"), @ApiResponse(responseCode = "404", description = "Género no encontrado")})
    @GetMapping("/{id}")
    public ResponseEntity<GeneroResponseDTO> obtenerPorId(@Parameter(description = "ID del género") @PathVariable Long id) {
        return ResponseEntity.ok(generoService.obtenerGeneroPorId(id));
    }

    @Operation(summary = "Crear un nuevo género", description = "Registra un nuevo género en el sistema")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Género creado exitosamente"), @ApiResponse(responseCode = "400", description = "Datos inválidos")})
    @PostMapping
    public ResponseEntity<GeneroResponseDTO> crear(@Valid @RequestBody GeneroRequestDTO dto) {
        GeneroResponseDTO nuevoGenero = generoService.crearGenero(dto);
        return new ResponseEntity<>(nuevoGenero, HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar un género", description = "Actualiza los datos de un género existente")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Género actualizado"), @ApiResponse(responseCode = "404", description = "Género no encontrado"), @ApiResponse(responseCode = "400", description = "Datos inválidos")})
    @PutMapping("/{id}")
    public ResponseEntity<GeneroResponseDTO> actualizar(
            @Parameter(description = "ID del género") @PathVariable Long id,
            @Valid @RequestBody GeneroRequestDTO dto) {
        GeneroResponseDTO generoActualizado = generoService.actualizarGenero(id, dto);
        return ResponseEntity.ok(generoActualizado);
    }

    @Operation(summary = "Eliminar un género", description = "Elimina un género del sistema")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Género eliminado"), @ApiResponse(responseCode = "404", description = "Género no encontrado")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@Parameter(description = "ID del género") @PathVariable Long id) {
        generoService.eliminarGenero(id);
        return ResponseEntity.noContent().build();
    }
}

package evaluacion3.valoracion.controller;

import evaluacion3.valoracion.dto.request.ValoracionRequestDTO;
import evaluacion3.valoracion.dto.response.ValoracionResponseDTO;
import evaluacion3.valoracion.service.ValoracionService;
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
@RequestMapping("/api/v1/valoraciones")
public class ValoracionController {

    private final ValoracionService valoracionService;

    public ValoracionController(ValoracionService valoracionService) {
        this.valoracionService = valoracionService;
    }

    @Operation(summary = "Obtener todas las valoraciones", description = "Retorna una lista de todas las valoraciones registradas")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Lista de valoraciones obtenida exitosamente")})
    @GetMapping
    public ResponseEntity<List<ValoracionResponseDTO>> obtenerTodasLasValoraciones() {
        return ResponseEntity.ok(valoracionService.obtenerTodasLasValoraciones());
    }

    @Operation(summary = "Obtener valoración por ID", description = "Retorna una valoración según su identificador único")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Valoración encontrada"), @ApiResponse(responseCode = "404", description = "Valoración no encontrada")})
    @GetMapping("/{id}")
    public ResponseEntity<ValoracionResponseDTO> obtenerValoracionPorId(@Parameter(description = "ID de la valoración") @PathVariable Long id) {
        return ResponseEntity.ok(valoracionService.obtenerValoracionPorId(id));
    }

    @Operation(summary = "Obtener valoraciones por película", description = "Retorna todas las valoraciones asociadas a una película")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Lista de valoraciones obtenida"), @ApiResponse(responseCode = "404", description = "Película no encontrada")})
    @GetMapping("/pelicula/{idPelicula}")
    public ResponseEntity<List<ValoracionResponseDTO>> obtenerValoracionPorPelicula(@Parameter(description = "ID de la película") @PathVariable Long idPelicula) {
        return ResponseEntity.ok(valoracionService.obtenerValoracionPorPelicula(idPelicula));
    }

    @Operation(summary = "Crear una nueva valoración", description = "Registra una nueva valoración en el sistema")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Valoración creada exitosamente"), @ApiResponse(responseCode = "400", description = "Datos inválidos")})
    @PostMapping
    public ResponseEntity<ValoracionResponseDTO> crearValoracion(@Valid @RequestBody ValoracionRequestDTO dto) {
        ValoracionResponseDTO nuevaValoracion = valoracionService.crearValoracion(dto);
        return new ResponseEntity<>(nuevaValoracion, HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar una valoración", description = "Actualiza los datos de una valoración existente")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Valoración actualizada"), @ApiResponse(responseCode = "404", description = "Valoración no encontrada"), @ApiResponse(responseCode = "400", description = "Datos inválidos")})
    @PutMapping("/{id}")
    public ResponseEntity<ValoracionResponseDTO> actualizarValoracion(
            @Parameter(description = "ID de la valoración") @PathVariable Long id,
            @Valid @RequestBody ValoracionRequestDTO dto) {
        ValoracionResponseDTO valoracionActualizada = valoracionService.actualizarValoracion(id, dto);
        return ResponseEntity.ok(valoracionActualizada);
    }

    @Operation(summary = "Eliminar una valoración", description = "Elimina una valoración del sistema")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Valoración eliminada"), @ApiResponse(responseCode = "404", description = "Valoración no encontrada")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarValoracion(@Parameter(description = "ID de la valoración") @PathVariable Long id) {
        valoracionService.eliminarValoracion(id);
        return ResponseEntity.noContent().build();
    }
}

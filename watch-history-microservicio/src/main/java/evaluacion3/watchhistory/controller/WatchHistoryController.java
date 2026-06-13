package evaluacion3.watchhistory.controller;

import evaluacion3.watchhistory.dto.request.WatchHistoryRequestDTO;
import evaluacion3.watchhistory.dto.response.WatchHistoryResponseDTO;
import evaluacion3.watchhistory.service.WatchHistoryService;
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
@RequestMapping("/api/v1/watch-history")
public class WatchHistoryController {

    private final WatchHistoryService watchHistoryService;

    public WatchHistoryController(WatchHistoryService watchHistoryService) {
        this.watchHistoryService = watchHistoryService;
    }

    @Operation(summary = "Obtener historial por usuario", description = "Retorna el historial completo de visualización de un usuario")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Historial obtenido exitosamente")})
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<WatchHistoryResponseDTO>> obtenerHistorialPorUsuario(
            @Parameter(description = "ID del usuario") @PathVariable Long usuarioId) {
        return ResponseEntity.ok(watchHistoryService.obtenerHistorialPorUsuario(usuarioId));
    }

    @Operation(summary = "Obtener contenido en progreso", description = "Retorna el contenido que el usuario está viendo pero no ha completado")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Contenido en progreso obtenido exitosamente")})
    @GetMapping("/usuario/{usuarioId}/en-progreso")
    public ResponseEntity<List<WatchHistoryResponseDTO>> obtenerContenidoEnProgreso(
            @Parameter(description = "ID del usuario") @PathVariable Long usuarioId) {
        return ResponseEntity.ok(watchHistoryService.obtenerContenidoEnProgreso(usuarioId));
    }

    @Operation(summary = "Obtener registro por ID", description = "Retorna un registro de historial según su identificador único")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Registro encontrado"), @ApiResponse(responseCode = "404", description = "Registro no encontrado")})
    @GetMapping("/{id}")
    public ResponseEntity<WatchHistoryResponseDTO> obtenerPorId(
            @Parameter(description = "ID del registro") @PathVariable Long id) {
        return ResponseEntity.ok(watchHistoryService.obtenerPorId(id));
    }

    @Operation(summary = "Registrar progreso", description = "Registra o actualiza el progreso de visualización de una película")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Progreso registrado exitosamente"), @ApiResponse(responseCode = "400", description = "Datos inválidos")})
    @PostMapping
    public ResponseEntity<WatchHistoryResponseDTO> registrarProgreso(@Valid @RequestBody WatchHistoryRequestDTO dto) {
        WatchHistoryResponseDTO response = watchHistoryService.registrarProgreso(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Eliminar registro", description = "Elimina un registro de historial del sistema")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Registro eliminado"), @ApiResponse(responseCode = "404", description = "Registro no encontrado")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@Parameter(description = "ID del registro") @PathVariable Long id) {
        watchHistoryService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

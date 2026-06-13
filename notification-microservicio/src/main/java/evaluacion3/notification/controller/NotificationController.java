package evaluacion3.notification.controller;

import evaluacion3.notification.dto.request.NotificationRequestDTO;
import evaluacion3.notification.dto.response.NotificationResponseDTO;
import evaluacion3.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(summary = "Obtener todas las notificaciones de un usuario")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Lista de notificaciones obtenida exitosamente")})
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<NotificationResponseDTO>> obtenerTodasPorUsuario(
            @Parameter(description = "ID del usuario") @PathVariable Long usuarioId) {
        return ResponseEntity.ok(notificationService.obtenerTodasPorUsuario(usuarioId));
    }

    @Operation(summary = "Obtener notificaciones no leídas de un usuario")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Lista de notificaciones no leídas obtenida")})
    @GetMapping("/usuario/{usuarioId}/no-leidas")
    public ResponseEntity<List<NotificationResponseDTO>> obtenerNoLeidas(
            @Parameter(description = "ID del usuario") @PathVariable Long usuarioId) {
        return ResponseEntity.ok(notificationService.obtenerNoLeidas(usuarioId));
    }

    @Operation(summary = "Obtener notificación por ID")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Notificación encontrada"),
            @ApiResponse(responseCode = "404", description = "Notificación no encontrada")})
    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponseDTO> obtenerPorId(
            @Parameter(description = "ID de la notificación") @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.obtenerPorId(id));
    }

    @Operation(summary = "Crear una nueva notificación")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Notificación creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")})
    @PostMapping
    public ResponseEntity<NotificationResponseDTO> crear(@Valid @RequestBody NotificationRequestDTO dto) {
        NotificationResponseDTO nuevaNotificacion = notificationService.crear(dto);
        return new ResponseEntity<>(nuevaNotificacion, HttpStatus.CREATED);
    }

    @Operation(summary = "Marcar notificación como leída")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Notificación marcada como leída"),
            @ApiResponse(responseCode = "404", description = "Notificación no encontrada"),
            @ApiResponse(responseCode = "409", description = "Ya estaba marcada como leída")})
    @PatchMapping("/{id}/leer")
    public ResponseEntity<NotificationResponseDTO> marcarLeida(
            @Parameter(description = "ID de la notificación") @PathVariable Long id) {
        return ResponseEntity.ok(notificationService.marcarLeida(id));
    }

    @Operation(summary = "Marcar todas las notificaciones de un usuario como leídas")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Notificaciones marcadas como leídas")})
    @PatchMapping("/usuario/{usuarioId}/leer-todas")
    public ResponseEntity<Map<String, Object>> marcarTodasLeidas(
            @Parameter(description = "ID del usuario") @PathVariable Long usuarioId) {
        int marcadas = notificationService.marcarTodasLeidas(usuarioId);
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("marcadas", marcadas);
        return ResponseEntity.ok(respuesta);
    }

    @Operation(summary = "Eliminar una notificación")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Notificación eliminada"),
            @ApiResponse(responseCode = "404", description = "Notificación no encontrada")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID de la notificación") @PathVariable Long id) {
        notificationService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

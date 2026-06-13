package evaluacion3.subscription.controller;

import evaluacion3.subscription.dto.request.SubscriptionRequestDTO;
import evaluacion3.subscription.dto.response.SubscriptionResponseDTO;
import evaluacion3.subscription.service.SubscriptionService;
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
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Operation(summary = "Obtener todas las suscripciones", description = "Retorna una lista de todas las suscripciones registradas")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Lista de suscripciones obtenida exitosamente")})
    @GetMapping
    public ResponseEntity<List<SubscriptionResponseDTO>> obtenerTodas() {
        return ResponseEntity.ok(subscriptionService.obtenerTodas());
    }

    @Operation(summary = "Obtener suscripción por ID", description = "Retorna una suscripción según su identificador único")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Suscripción encontrada"), @ApiResponse(responseCode = "404", description = "Suscripción no encontrada")})
    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionResponseDTO> obtenerPorId(@Parameter(description = "ID de la suscripción") @PathVariable Long id) {
        return ResponseEntity.ok(subscriptionService.obtenerPorId(id));
    }

    @Operation(summary = "Obtener suscripciones por usuario", description = "Retorna las suscripciones de un usuario específico")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Suscripciones del usuario obtenidas exitosamente")})
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<SubscriptionResponseDTO>> obtenerPorUsuario(@Parameter(description = "ID del usuario") @PathVariable Long usuarioId) {
        return ResponseEntity.ok(subscriptionService.obtenerPorUsuario(usuarioId));
    }

    @Operation(summary = "Crear una nueva suscripción", description = "Registra una nueva suscripción en el sistema")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Suscripción creada exitosamente"), @ApiResponse(responseCode = "400", description = "Datos inválidos"), @ApiResponse(responseCode = "409", description = "Conflicto - regla de negocio")})
    @PostMapping
    public ResponseEntity<SubscriptionResponseDTO> crear(@Valid @RequestBody SubscriptionRequestDTO dto) {
        SubscriptionResponseDTO nueva = subscriptionService.crear(dto);
        return new ResponseEntity<>(nueva, HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar una suscripción", description = "Actualiza los datos de una suscripción existente")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Suscripción actualizada"), @ApiResponse(responseCode = "404", description = "Suscripción no encontrada"), @ApiResponse(responseCode = "400", description = "Datos inválidos")})
    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionResponseDTO> actualizar(
            @Parameter(description = "ID de la suscripción") @PathVariable Long id,
            @Valid @RequestBody SubscriptionRequestDTO dto) {
        SubscriptionResponseDTO actualizada = subscriptionService.actualizar(id, dto);
        return ResponseEntity.ok(actualizada);
    }

    @Operation(summary = "Cancelar una suscripción", description = "Cambia el estado de una suscripción a CANCELADO")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Suscripción cancelada"), @ApiResponse(responseCode = "404", description = "Suscripción no encontrada")})
    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<SubscriptionResponseDTO> cancelar(@Parameter(description = "ID de la suscripción") @PathVariable Long id) {
        SubscriptionResponseDTO cancelada = subscriptionService.cancelarSuscripcion(id);
        return ResponseEntity.ok(cancelada);
    }

    @Operation(summary = "Eliminar una suscripción", description = "Elimina una suscripción del sistema")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Suscripción eliminada"), @ApiResponse(responseCode = "404", description = "Suscripción no encontrada")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@Parameter(description = "ID de la suscripción") @PathVariable Long id) {
        subscriptionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

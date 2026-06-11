package evaluacion3.usuario.controller;

import evaluacion3.usuario.dto.request.UsuarioRequestDTO;
import evaluacion3.usuario.dto.response.UsuarioResponseDTO;
import evaluacion3.usuario.service.UsuarioService;
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
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Obtener todos los usuarios", description = "Retorna una lista de todos los usuarios registrados")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente")})
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(usuarioService.obtenerTodosLosUsuarios());
    }

    @Operation(summary = "Obtener usuario por ID", description = "Retorna un usuario según su identificador único")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Usuario encontrado"), @ApiResponse(responseCode = "404", description = "Usuario no encontrado")})
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> obtenerPorId(@Parameter(description = "ID del usuario") @PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerUsuarioPorId(id));
    }

    @Operation(summary = "Obtener usuario por email", description = "Retorna un usuario según su dirección de correo electrónico")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Usuario encontrado"), @ApiResponse(responseCode = "404", description = "Usuario no encontrado")})
    @GetMapping("/email/{email}")
    public ResponseEntity<UsuarioResponseDTO> obtenerPorEmail(@Parameter(description = "Email del usuario") @PathVariable String email) {
        return ResponseEntity.ok(usuarioService.obtenerUsuarioPorEmail(email));
    }

    @Operation(summary = "Crear un nuevo usuario", description = "Registra un nuevo usuario en el sistema")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"), @ApiResponse(responseCode = "400", description = "Datos inválidos")})
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> crear(@Valid @RequestBody UsuarioRequestDTO dto) {
        UsuarioResponseDTO nuevoUsuario = usuarioService.crearUsuario(dto);
        return new ResponseEntity<>(nuevoUsuario, HttpStatus.CREATED);
    }

    @Operation(summary = "Actualizar un usuario", description = "Actualiza los datos de un usuario existente")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Usuario actualizado"), @ApiResponse(responseCode = "404", description = "Usuario no encontrado"), @ApiResponse(responseCode = "400", description = "Datos inválidos")})
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizar(
            @Parameter(description = "ID del usuario") @PathVariable Long id,
            @Valid @RequestBody UsuarioRequestDTO dto) {
        UsuarioResponseDTO usuarioActualizado = usuarioService.actualizarUsuario(id, dto);
        return ResponseEntity.ok(usuarioActualizado);
    }

    @Operation(summary = "Eliminar un usuario", description = "Elimina un usuario del sistema")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Usuario eliminado"), @ApiResponse(responseCode = "404", description = "Usuario no encontrado")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@Parameter(description = "ID del usuario") @PathVariable Long id) {
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}

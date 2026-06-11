package evaluacion2.streamflow.controller;

import evaluacion2.streamflow.dto.request.GeneroRequestDTO;
import evaluacion2.streamflow.dto.response.GeneroResponseDTO;
import evaluacion2.streamflow.service.GeneroService;
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

    @GetMapping
    public ResponseEntity<List<GeneroResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(generoService.obtenerTodosLosGeneros());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneroResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(generoService.obtenerGeneroPorId(id));
    }

    @PostMapping
    public ResponseEntity<GeneroResponseDTO> crear(@Valid @RequestBody GeneroRequestDTO dto) {
        GeneroResponseDTO nuevoGenero = generoService.crearGenero(dto);
        return new ResponseEntity<>(nuevoGenero, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GeneroResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody GeneroRequestDTO dto) {
        GeneroResponseDTO generoActualizado = generoService.actualizarGenero(id, dto);
        return ResponseEntity.ok(generoActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        generoService.eliminarGenero(id);
        return ResponseEntity.noContent().build();
    }
}

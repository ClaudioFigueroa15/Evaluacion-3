package evaluacion2.streamflow.controller;

import evaluacion2.streamflow.dto.request.ValoracionRequestDTO;
import evaluacion2.streamflow.dto.response.ValoracionResponseDTO;
import evaluacion2.streamflow.service.ValoracionService;
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

    @GetMapping
    public ResponseEntity<List<ValoracionResponseDTO>> obtenerTodasLasValoraciones() {
        return ResponseEntity.ok(valoracionService.obtenerTodasLasValoraciones());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ValoracionResponseDTO> obtenerValoracionPorId(@PathVariable Long id) {
        return ResponseEntity.ok(valoracionService.obtenerValoracionPorId(id));
    }

    @GetMapping("/pelicula/{idPelicula}")
    public ResponseEntity<List<ValoracionResponseDTO>> obtenerValoracionPorPelicula(@PathVariable Long idPelicula) {
        return ResponseEntity.ok(valoracionService.obtenerValoracionPorPelicula(idPelicula));
    }

    @PostMapping
    public ResponseEntity<ValoracionResponseDTO> crearValoracion(@Valid @RequestBody ValoracionRequestDTO dto) {
        ValoracionResponseDTO nuevaValoracion = valoracionService.crearValoracion(dto);
        return new ResponseEntity<>(nuevaValoracion, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ValoracionResponseDTO> actualizarValoracion(
            @PathVariable Long id,
            @Valid @RequestBody ValoracionRequestDTO dto) {
        ValoracionResponseDTO valoracionActualizada = valoracionService.actualizarValoracion(id, dto);
        return ResponseEntity.ok(valoracionActualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarValoracion(@PathVariable Long id) {
        valoracionService.eliminarValoracion(id);
        return ResponseEntity.noContent().build();
    }
}
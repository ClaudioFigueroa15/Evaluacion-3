package evaluacion2.streamflow.controller;

import evaluacion2.streamflow.dto.request.PeliculaRequestDTO;
import evaluacion2.streamflow.dto.response.PeliculaResponseDTO;
import evaluacion2.streamflow.service.PeliculaService;
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

    @GetMapping
    public ResponseEntity<List<PeliculaResponseDTO>> obtenerTodasLasPeliculas() {
        return ResponseEntity.ok(peliculaService.obtenerTodasLasPeliculas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PeliculaResponseDTO> obtenerPeliculaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(peliculaService.obtenerPeliculaPorId(id));
    }

    @GetMapping("/genero/{idGenero}")
    public ResponseEntity<List<PeliculaResponseDTO>> obtenerPeliculaPorGenero(@PathVariable Long idGenero) {
        return ResponseEntity.ok(peliculaService.obtenerPeliculaPorGenero(idGenero));
    }

    @GetMapping("/desde/{anio}")
    public ResponseEntity<List<PeliculaResponseDTO>> obtenerDesdeAnio(@PathVariable Integer anio) {
        return ResponseEntity.ok(peliculaService.obtenerDesdeAnio(anio));
    }

    @PostMapping
    public ResponseEntity<PeliculaResponseDTO> crearPelicula(@Valid @RequestBody PeliculaRequestDTO dto) {
        PeliculaResponseDTO nuevaPelicula = peliculaService.crearPelicula(dto);
        return new ResponseEntity<>(nuevaPelicula, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PeliculaResponseDTO> actualizarPelicula(
            @PathVariable Long id,
            @Valid @RequestBody PeliculaRequestDTO dto) {
        PeliculaResponseDTO peliculaActualizada = peliculaService.actualizarPelicula(id, dto);
        return ResponseEntity.ok(peliculaActualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPelicula(@PathVariable Long id) {
        peliculaService.eliminarPelicula(id);
        return ResponseEntity.noContent().build();
    }
}
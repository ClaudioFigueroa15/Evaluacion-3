package evaluacion3.search.controller;

import evaluacion3.search.dto.request.SearchRequestDTO;
import evaluacion3.search.dto.response.SearchResponseDTO;
import evaluacion3.search.service.SearchService;
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
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @Operation(summary = "Buscar por término", description = "Busca películas indexadas por título, director, actores o etiquetas")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Resultados de búsqueda obtenidos exitosamente")})
    @GetMapping
    public ResponseEntity<List<SearchResponseDTO>> buscarPorTermino(
            @Parameter(description = "Término de búsqueda") @RequestParam String termino) {
        return ResponseEntity.ok(searchService.buscarPorTermino(termino));
    }

    @Operation(summary = "Buscar por año", description = "Busca películas indexadas por año")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Resultados obtenidos exitosamente")})
    @GetMapping("/anio/{anio}")
    public ResponseEntity<List<SearchResponseDTO>> buscarPorAnio(
            @Parameter(description = "Año de la película") @PathVariable Integer anio) {
        return ResponseEntity.ok(searchService.buscarPorAnio(anio));
    }

    @Operation(summary = "Obtener índice por ID", description = "Retorna un índice de búsqueda según su identificador único")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Índice encontrado"), @ApiResponse(responseCode = "404", description = "Índice no encontrado")})
    @GetMapping("/{id}")
    public ResponseEntity<SearchResponseDTO> obtenerPorId(
            @Parameter(description = "ID del índice") @PathVariable Long id) {
        return ResponseEntity.ok(searchService.obtenerPorId(id));
    }

    @Operation(summary = "Indexar película", description = "Crea o actualiza un índice de búsqueda para una película")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Índice creado o actualizado exitosamente"), @ApiResponse(responseCode = "400", description = "Datos inválidos")})
    @PostMapping("/indexar")
    public ResponseEntity<SearchResponseDTO> indexar(@Valid @RequestBody SearchRequestDTO dto) {
        SearchResponseDTO resultado = searchService.indexar(dto);
        return new ResponseEntity<>(resultado, HttpStatus.CREATED);
    }

    @Operation(summary = "Sincronizar desde película", description = "Obtiene datos de una película desde pelicula-microservicio y los indexa")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Sincronización exitosa"), @ApiResponse(responseCode = "409", description = "Error al obtener datos de la película")})
    @PostMapping("/sincronizar/{peliculaId}")
    public ResponseEntity<SearchResponseDTO> sincronizar(
            @Parameter(description = "ID de la película a sincronizar") @PathVariable Long peliculaId) {
        SearchResponseDTO resultado = searchService.sincronizarDesdePelicula(peliculaId);
        return ResponseEntity.ok(resultado);
    }

    @Operation(summary = "Eliminar índice", description = "Elimina un índice de búsqueda del sistema")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Índice eliminado"), @ApiResponse(responseCode = "404", description = "Índice no encontrado")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del índice") @PathVariable Long id) {
        searchService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

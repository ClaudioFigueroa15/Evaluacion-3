package evaluacion3.search.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SearchRequestDTO {

    @NotNull(message = "El ID de la película no puede ser nulo")
    private Long peliculaId;

    @NotBlank(message = "El título no puede estar vacío")
    @Size(max = 200, message = "El título no puede superar los 200 caracteres")
    private String titulo;

    @Size(max = 100, message = "El director no puede superar los 100 caracteres")
    private String director;

    @Size(max = 500, message = "Los actores no pueden superar los 500 caracteres")
    private String actores;

    @Size(max = 300, message = "Las etiquetas no pueden superar los 300 caracteres")
    private String etiquetas;

    @Min(value = 1888, message = "El año debe ser mayor o igual a 1888")
    private Integer anio;
}

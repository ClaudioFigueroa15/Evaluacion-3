package evaluacion2.streamflow.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PeliculaRequestDTO {

    @NotBlank(message = "El título no puede estar vacío")
    @Size(min = 1, max = 100, message = "El título debe tener entre 1 y 100 caracteres")
    private String titulo;

    @NotNull(message = "El año de estreno es obligatorio")
    @Min(value = 1888, message = "El año de estreno no puede ser anterior a 1888")
    @Max(value = 2100, message = "El año de estreno no parece válido")
    private Integer anioEstreno;

    @NotNull(message = "La duración es obligatoria")
    @Min(value = 1, message = "La duración debe ser de al menos 1 minuto")
    @Max(value = 600, message = "La duración no puede superar los 600 minutos")
    private Integer duracion;

    @NotNull(message = "El id del género es obligatorio")
    private Long idGenero;
}

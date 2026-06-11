package evaluacion2.streamflow.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ValoracionRequestDTO {

    @NotNull(message = "El puntaje es obligatorio")
    @Min(value = 1, message = "El puntaje mínimo es 1")
    @Max(value = 10, message = "El puntaje máximo es 10")
    private Integer puntaje;

    @Size(max = 500, message = "El comentario no puede superar los 500 caracteres")
    private String comentario;

    @NotNull(message = "El id de la película es obligatorio")
    private Long idPelicula;
}

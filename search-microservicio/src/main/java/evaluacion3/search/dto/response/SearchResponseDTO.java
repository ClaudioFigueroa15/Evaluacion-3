package evaluacion3.search.dto.response;

import lombok.Data;

@Data
public class SearchResponseDTO {

    private Long id;
    private Long peliculaId;
    private String titulo;
    private String director;
    private String actores;
    private String etiquetas;
    private Integer anio;
}

package evaluacion3.search.client;

import lombok.Data;

@Data
public class PeliculaSimpleDTO {
    private Long id;
    private String titulo;
    private String director;
    private Integer anio;
    private Long generoId;
}

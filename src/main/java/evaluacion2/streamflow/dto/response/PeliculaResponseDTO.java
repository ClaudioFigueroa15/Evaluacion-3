package evaluacion2.streamflow.dto.response;

import lombok.Data;

@Data
public class PeliculaResponseDTO {

    private Long id;
    private String titulo;
    private Integer anioEstreno;
    private Integer duracion;
    private String nombreGenero;
    private Double valoracion;
}

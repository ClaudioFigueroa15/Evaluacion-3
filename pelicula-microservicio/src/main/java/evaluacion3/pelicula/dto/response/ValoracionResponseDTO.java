package evaluacion3.pelicula.dto.response;

import lombok.Data;

@Data
public class ValoracionResponseDTO {

    private Long id;
    private Integer puntaje;
    private String comentario;
    private Long idPelicula;
    private String tituloPelicula;
}

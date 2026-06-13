package evaluacion3.favorites.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FavoriteResponseDTO {

    private Long id;
    private Long usuarioId;
    private Long peliculaId;
    private LocalDateTime fechaAgregado;
}

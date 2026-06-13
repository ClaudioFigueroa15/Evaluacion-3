package evaluacion3.watchhistory.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WatchHistoryResponseDTO {

    private Long id;
    private Long usuarioId;
    private Long peliculaId;
    private Integer progresoPorcentaje;
    private Boolean completado;
    private LocalDateTime ultimaVezVisto;
}

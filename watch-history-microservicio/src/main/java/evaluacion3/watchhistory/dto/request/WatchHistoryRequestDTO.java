package evaluacion3.watchhistory.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WatchHistoryRequestDTO {

    @NotNull
    private Long usuarioId;

    @NotNull
    private Long peliculaId;

    @NotNull
    @Min(0)
    @Max(100)
    private Integer progresoPorcentaje;

    @NotNull
    private LocalDateTime ultimaVezVisto;
}

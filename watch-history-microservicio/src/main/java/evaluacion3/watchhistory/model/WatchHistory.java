package evaluacion3.watchhistory.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "watch_history", uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "pelicula_id"}))
public class WatchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "pelicula_id", nullable = false)
    private Long peliculaId;

    @Column(name = "progreso_porcentaje", nullable = false)
    private Integer progresoPorcentaje;

    @Column(nullable = false)
    private Boolean completado = false;

    @Column(name = "ultima_vez_visto", nullable = false)
    private LocalDateTime ultimaVezVisto;
}

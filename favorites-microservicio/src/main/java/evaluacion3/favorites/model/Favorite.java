package evaluacion3.favorites.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "favorites", uniqueConstraints = @UniqueConstraint(columnNames = {"usuario_id", "pelicula_id"}))
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "usuario_id")
    private Long usuarioId;

    @Column(nullable = false, name = "pelicula_id")
    private Long peliculaId;

    @Column(nullable = false, name = "fecha_agregado")
    private LocalDateTime fechaAgregado;
}

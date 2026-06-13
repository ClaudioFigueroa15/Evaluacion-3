package evaluacion3.search.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "search_index")
public class SearchIndex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pelicula_id", nullable = false)
    private Long peliculaId;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(length = 100)
    private String director;

    @Column(length = 500)
    private String actores;

    @Column(length = 300)
    private String etiquetas;

    private Integer anio;
}

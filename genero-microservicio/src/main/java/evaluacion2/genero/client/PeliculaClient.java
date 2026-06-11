package evaluacion2.genero.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "pelicula-microservicio", url = "http://localhost:8081/api/v1/peliculas")
public interface PeliculaClient {

    @GetMapping("/genero/{idGenero}")
    List<?> obtenerPeliculasPorGenero(@PathVariable("idGenero") Long idGenero);
}

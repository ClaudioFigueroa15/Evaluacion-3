package evaluacion3.genero.client;

import evaluacion3.genero.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "pelicula-microservicio", url = "http://localhost:8081/api/v1/peliculas", configuration = FeignConfig.class)
public interface PeliculaClient {

    @GetMapping("/genero/{idGenero}")
    List<PeliculaSimpleDTO> obtenerPeliculasPorGenero(@PathVariable("idGenero") Long idGenero);
}

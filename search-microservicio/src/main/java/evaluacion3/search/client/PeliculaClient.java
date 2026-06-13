package evaluacion3.search.client;

import evaluacion3.search.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "pelicula-microservicio", url = "${services.pelicula.url}", configuration = FeignConfig.class)
public interface PeliculaClient {

    @GetMapping("/api/v1/peliculas/{id}")
    PeliculaSimpleDTO obtenerPeliculaPorId(@PathVariable("id") Long id);
}

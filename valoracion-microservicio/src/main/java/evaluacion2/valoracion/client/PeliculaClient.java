package evaluacion2.valoracion.client;

import evaluacion2.valoracion.config.FeignConfig;
import evaluacion2.valoracion.dto.response.PeliculaResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "pelicula-microservicio", url = "http://localhost:8081/api/v1/peliculas", configuration = FeignConfig.class)
public interface PeliculaClient {

    @GetMapping("/{id}")
    PeliculaResponseDTO obtenerPeliculaPorId(@PathVariable("id") Long id);
}

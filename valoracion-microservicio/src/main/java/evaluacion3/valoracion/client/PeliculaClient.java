package evaluacion3.valoracion.client;

import evaluacion3.valoracion.config.FeignConfig;
import evaluacion3.valoracion.dto.response.PeliculaResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "pelicula-microservicio", url = "${pelicula.service.url}", configuration = FeignConfig.class)
public interface PeliculaClient {

    @GetMapping("/{id}")
    PeliculaResponseDTO obtenerPeliculaPorId(@PathVariable("id") Long id);
}

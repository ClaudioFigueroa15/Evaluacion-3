package evaluacion3.pelicula.client;

import evaluacion3.pelicula.config.FeignConfig;
import evaluacion3.pelicula.dto.response.GeneroResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "genero-microservicio", url = "${genero.service.url}", configuration = FeignConfig.class)
public interface GeneroClient {

    @GetMapping("/{id}")
    GeneroResponseDTO obtenerGeneroPorId(@PathVariable("id") Long id);
}

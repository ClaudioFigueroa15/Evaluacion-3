package evaluacion2.pelicula.client;

import evaluacion2.pelicula.config.FeignConfig;
import evaluacion2.pelicula.dto.response.GeneroResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "genero-microservicio", url = "http://localhost:8082/api/v1/generos", configuration = FeignConfig.class)
public interface GeneroClient {

    @GetMapping("/{id}")
    GeneroResponseDTO obtenerGeneroPorId(@PathVariable("id") Long id);
}

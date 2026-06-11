package evaluacion3.pelicula.client;

import evaluacion3.pelicula.config.FeignConfig;
import evaluacion3.pelicula.dto.response.ValoracionResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "valoracion-microservicio", url = "http://localhost:8083/api/v1/valoraciones", configuration = FeignConfig.class)
public interface ValoracionClient {

    @GetMapping("/pelicula/{idPelicula}")
    List<ValoracionResponseDTO> obtenerPorPelicula(@PathVariable("idPelicula") Long idPelicula);
}

package evaluacion2.valoracion.config;

import evaluacion2.valoracion.exception.RecursoNoEncontradoException;
import evaluacion2.valoracion.exception.ReglaNegocioException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            int status = response.status();
            String url = response.request().url();

            switch (status) {
                case 404:
                    return new RecursoNoEncontradoException(
                            "Recurso remoto no encontrado en " + url + " (HTTP " + status + ")");
                case 409:
                    return new ReglaNegocioException(
                            "Conflicto con recurso remoto en " + url + " (HTTP " + status + ")");
                default:
                    if (status >= 400 && status < 500) {
                        return new ReglaNegocioException(
                                "Error en solicitud remota hacia " + url + " (HTTP " + status + ")");
                    }
                    return new RuntimeException(
                            "Error interno en servicio remoto " + url + " (HTTP " + status + ")");
            }
        };
    }
}

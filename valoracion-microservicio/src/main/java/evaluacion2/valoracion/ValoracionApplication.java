package evaluacion2.valoracion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ValoracionApplication {

    public static void main(String[] args) {
        SpringApplication.run(ValoracionApplication.class, args);
    }
}

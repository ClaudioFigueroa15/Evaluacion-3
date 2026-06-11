package evaluacion3.genero;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class GeneroApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeneroApplication.class, args);
    }
}

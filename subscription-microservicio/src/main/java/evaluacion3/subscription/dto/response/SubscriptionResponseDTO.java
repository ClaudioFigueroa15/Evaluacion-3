package evaluacion3.subscription.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SubscriptionResponseDTO {

    private Long id;
    private Long usuarioId;
    private String plan;
    private String estado;
    private LocalDate fechaInicio;
    private LocalDate fechaVencimiento;
    private BigDecimal precioMensual;
}

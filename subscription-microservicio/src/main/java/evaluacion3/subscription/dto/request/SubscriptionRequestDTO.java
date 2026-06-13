package evaluacion3.subscription.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SubscriptionRequestDTO {

    @NotNull(message = "El ID de usuario es obligatorio")
    private Long usuarioId;

    @NotBlank(message = "El plan es obligatorio")
    @Pattern(regexp = "BASICO|ESTANDAR|PREMIUM", message = "Plan inválido. Use: BASICO, ESTANDAR, PREMIUM")
    private String plan;

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "ACTIVO|CANCELADO|SUSPENDIDO", message = "Estado inválido. Use: ACTIVO, CANCELADO, SUSPENDIDO")
    private String estado;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    private LocalDate fechaVencimiento;

    @NotNull(message = "El precio mensual es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio mensual debe ser mayor a 0")
    private BigDecimal precioMensual;
}

package evaluacion3.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NotificationRequestDTO {

    @NotNull(message = "El usuarioId no puede ser nulo")
    private Long usuarioId;

    @NotBlank(message = "El tipo no puede estar vacío")
    @Pattern(regexp = "NUEVO_ESTRENO|VENCIMIENTO_PAGO|BIENVENIDA|RECORDATORIO",
            message = "Tipo inválido. Use: NUEVO_ESTRENO, VENCIMIENTO_PAGO, BIENVENIDA, RECORDATORIO")
    private String tipo;

    @NotBlank(message = "El título no puede estar vacío")
    @Size(max = 100, message = "El título no puede superar los 100 caracteres")
    private String titulo;

    @NotBlank(message = "El mensaje no puede estar vacío")
    @Size(max = 500, message = "El mensaje no puede superar los 500 caracteres")
    private String mensaje;
}

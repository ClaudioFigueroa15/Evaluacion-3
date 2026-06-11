package evaluacion3.usuario.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UsuarioResponseDTO {

    private Long id;
    private String nombre;
    private String email;
    private LocalDateTime fechaRegistro;
}

package evaluacion3.usuario.controller;

import evaluacion3.usuario.dto.request.UsuarioRequestDTO;
import evaluacion3.usuario.dto.response.UsuarioResponseDTO;
import evaluacion3.usuario.exception.RecursoNoEncontradoException;
import evaluacion3.usuario.exception.ReglaNegocioException;
import evaluacion3.usuario.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    private UsuarioResponseDTO responseDTO;
    private UsuarioRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new UsuarioResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setNombre("Juan Pérez");
        responseDTO.setEmail("juan@example.com");
        responseDTO.setFechaRegistro(LocalDateTime.now());

        requestDTO = new UsuarioRequestDTO();
        requestDTO.setNombre("Juan Pérez");
        requestDTO.setEmail("juan@example.com");
        requestDTO.setPassword("Pass1234!");
    }

    @Test
    @DisplayName("GET / obtiene lista de usuarios")
    void obtenerTodos() {
        when(usuarioService.obtenerTodosLosUsuarios()).thenReturn(List.of(responseDTO));

        ResponseEntity<List<UsuarioResponseDTO>> respuesta = usuarioController.obtenerTodos();

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(respuesta.getBody())).hasSize(1);
    }

    @Test
    @DisplayName("GET /{id} retorna usuario por id")
    void obtenerPorId() {
        when(usuarioService.obtenerUsuarioPorId(1L)).thenReturn(responseDTO);

        ResponseEntity<UsuarioResponseDTO> respuesta = usuarioController.obtenerPorId(1L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(respuesta.getBody()).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("GET /email/{email} retorna usuario por email")
    void obtenerPorEmail() {
        when(usuarioService.obtenerUsuarioPorEmail("juan@example.com")).thenReturn(responseDTO);

        ResponseEntity<UsuarioResponseDTO> respuesta = usuarioController.obtenerPorEmail("juan@example.com");

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(respuesta.getBody()).getEmail()).isEqualTo("juan@example.com");
    }

    @Test
    @DisplayName("POST / crea usuario y retorna 201")
    void crear() {
        when(usuarioService.crearUsuario(requestDTO)).thenReturn(responseDTO);

        ResponseEntity<UsuarioResponseDTO> respuesta = usuarioController.crear(requestDTO);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(respuesta.getBody()).getNombre()).isEqualTo("Juan Pérez");
    }

    @Test
    @DisplayName("PUT /{id} actualiza usuario y retorna 200")
    void actualizar() {
        when(usuarioService.actualizarUsuario(1L, requestDTO)).thenReturn(responseDTO);

        ResponseEntity<UsuarioResponseDTO> respuesta = usuarioController.actualizar(1L, requestDTO);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(respuesta.getBody()).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("DELETE /{id} elimina usuario y retorna 204")
    void eliminar() {
        doNothing().when(usuarioService).eliminarUsuario(1L);

        ResponseEntity<Void> respuesta = usuarioController.eliminar(1L);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("GET /{id} retorna 404 cuando no existe")
    void obtenerPorId_NoExiste() {
        when(usuarioService.obtenerUsuarioPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("Usuario", 99L));

        assertThatThrownBy(() -> usuarioController.obtenerPorId(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("POST / retorna 400 por regla de negocio")
    void crear_ReglaNegocio() {
        when(usuarioService.crearUsuario(requestDTO))
                .thenThrow(new ReglaNegocioException("El formato del email no es válido"));

        assertThatThrownBy(() -> usuarioController.crear(requestDTO))
                .isInstanceOf(ReglaNegocioException.class);
    }
}

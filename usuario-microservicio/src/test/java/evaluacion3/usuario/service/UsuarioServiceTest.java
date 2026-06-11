package evaluacion3.usuario.service;

import evaluacion3.usuario.dto.request.UsuarioRequestDTO;
import evaluacion3.usuario.dto.response.UsuarioResponseDTO;
import evaluacion3.usuario.exception.RecursoNoEncontradoException;
import evaluacion3.usuario.exception.ReglaNegocioException;
import evaluacion3.usuario.model.Usuario;
import evaluacion3.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;
    private UsuarioRequestDTO requestDTO;
    private final Long USUARIO_ID = 1L;

    @BeforeEach
    void setUp() {
        usuario = new Usuario(USUARIO_ID, "Juan Pérez", "juan@example.com", "Pass1234!", LocalDateTime.now());

        requestDTO = new UsuarioRequestDTO();
        requestDTO.setNombre("  Juan Pérez  ");
        requestDTO.setEmail("  JUAN@EXAMPLE.COM  ");
        requestDTO.setPassword("Pass1234!");
    }

    @Nested
    @DisplayName("Pruebas de consulta")
    class ConsultaTests {

        @Test
        @DisplayName("obtenerTodosLosUsuarios retorna lista")
        void obtenerTodos() {
            when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

            List<UsuarioResponseDTO> resultado = usuarioService.obtenerTodosLosUsuarios();

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getNombre()).isEqualTo("Juan Pérez");
        }

        @Test
        @DisplayName("obtenerUsuarioPorId retorna usuario cuando existe")
        void obtenerPorId_Existe() {
            when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));

            UsuarioResponseDTO resultado = usuarioService.obtenerUsuarioPorId(USUARIO_ID);

            assertThat(resultado.getId()).isEqualTo(USUARIO_ID);
            assertThat(resultado.getEmail()).isEqualTo("juan@example.com");
        }

        @Test
        @DisplayName("obtenerUsuarioPorId lanza RecursoNoEncontradoException cuando no existe")
        void obtenerPorId_NoExiste() {
            when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> usuarioService.obtenerUsuarioPorId(99L))
                    .isInstanceOf(RecursoNoEncontradoException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("obtenerUsuarioPorEmail retorna usuario cuando existe")
        void obtenerPorEmail_Existe() {
            when(usuarioRepository.findByEmail("juan@example.com")).thenReturn(Optional.of(usuario));

            UsuarioResponseDTO resultado = usuarioService.obtenerUsuarioPorEmail("juan@example.com");

            assertThat(resultado.getEmail()).isEqualTo("juan@example.com");
        }

        @Test
        @DisplayName("obtenerUsuarioPorEmail lanza RecursoNoEncontradoException cuando no existe")
        void obtenerPorEmail_NoExiste() {
            when(usuarioRepository.findByEmail("no@existe.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> usuarioService.obtenerUsuarioPorEmail("no@existe.com"))
                    .isInstanceOf(RecursoNoEncontradoException.class)
                    .hasMessageContaining("no@existe.com");
        }
    }

    @Nested
    @DisplayName("Pruebas de creación")
    class CreacionTests {

        @Test
        @DisplayName("crearUsuario crea exitosamente")
        void crearUsuario_Exito() {
            when(usuarioRepository.existsByEmail("juan@example.com")).thenReturn(false);
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

            UsuarioResponseDTO resultado = usuarioService.crearUsuario(requestDTO);

            assertThat(resultado.getNombre()).isEqualTo("Juan Pérez");
            assertThat(resultado.getEmail()).isEqualTo("juan@example.com");
        }

        @Test
        @DisplayName("crearUsuario lanza ReglaNegocioException por email invalido")
        void crearUsuario_EmailInvalido() {
            requestDTO.setEmail("invalido");

            assertThatThrownBy(() -> usuarioService.crearUsuario(requestDTO))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("formato del email");
        }

        @Test
        @DisplayName("crearUsuario lanza ReglaNegocioException por email duplicado")
        void crearUsuario_EmailDuplicado() {
            when(usuarioRepository.existsByEmail("juan@example.com")).thenReturn(true);

            assertThatThrownBy(() -> usuarioService.crearUsuario(requestDTO))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("Ya existe un usuario");
        }

        @Test
        @DisplayName("crearUsuario lanza ReglaNegocioException por password corta")
        void crearUsuario_PasswordCorta() {
            requestDTO.setPassword("Ab1!");
            when(usuarioRepository.existsByEmail("juan@example.com")).thenReturn(false);

            assertThatThrownBy(() -> usuarioService.crearUsuario(requestDTO))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("al menos 8 caracteres");
        }

        @Test
        @DisplayName("crearUsuario lanza ReglaNegocioException por password sin mayuscula")
        void crearUsuario_PasswordSinMayuscula() {
            requestDTO.setPassword("password1234!");
            when(usuarioRepository.existsByEmail("juan@example.com")).thenReturn(false);

            assertThatThrownBy(() -> usuarioService.crearUsuario(requestDTO))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("mayúscula");
        }

        @Test
        @DisplayName("crearUsuario lanza ReglaNegocioException por password sin minuscula")
        void crearUsuario_PasswordSinMinuscula() {
            requestDTO.setPassword("PASSWORD1234!");
            when(usuarioRepository.existsByEmail("juan@example.com")).thenReturn(false);

            assertThatThrownBy(() -> usuarioService.crearUsuario(requestDTO))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("minúscula");
        }

        @Test
        @DisplayName("crearUsuario lanza ReglaNegocioException por password sin digito")
        void crearUsuario_PasswordSinDigito() {
            requestDTO.setPassword("Password!!");
            when(usuarioRepository.existsByEmail("juan@example.com")).thenReturn(false);

            assertThatThrownBy(() -> usuarioService.crearUsuario(requestDTO))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("dígito");
        }

        @Test
        @DisplayName("crearUsuario lanza ReglaNegocioException por password sin caracter especial")
        void crearUsuario_PasswordSinEspecial() {
            requestDTO.setPassword("Password1234");
            when(usuarioRepository.existsByEmail("juan@example.com")).thenReturn(false);

            assertThatThrownBy(() -> usuarioService.crearUsuario(requestDTO))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("carácter especial");
        }
    }

    @Nested
    @DisplayName("Pruebas de actualización")
    class ActualizacionTests {

        @Test
        @DisplayName("actualizarUsuario actualiza exitosamente")
        void actualizarUsuario_Exito() {
            when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

            UsuarioResponseDTO resultado = usuarioService.actualizarUsuario(USUARIO_ID, requestDTO);

            assertThat(resultado.getId()).isEqualTo(USUARIO_ID);
        }

        @Test
        @DisplayName("actualizarUsuario lanza RecursoNoEncontradoException cuando no existe")
        void actualizarUsuario_NoExiste() {
            when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> usuarioService.actualizarUsuario(99L, requestDTO))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }

        @Test
        @DisplayName("actualizarUsuario lanza ReglaNegocioException por email invalido")
        void actualizarUsuario_EmailInvalido() {
            requestDTO.setEmail("invalido");
            when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));

            assertThatThrownBy(() -> usuarioService.actualizarUsuario(USUARIO_ID, requestDTO))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("formato del email");
        }

        @Test
        @DisplayName("actualizarUsuario lanza ReglaNegocioException por email duplicado")
        void actualizarUsuario_EmailDuplicado() {
            requestDTO.setEmail("otro@example.com");
            when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.existsByEmail("otro@example.com")).thenReturn(true);

            assertThatThrownBy(() -> usuarioService.actualizarUsuario(USUARIO_ID, requestDTO))
                    .isInstanceOf(ReglaNegocioException.class)
                    .hasMessageContaining("Ya existe un usuario");
        }
    }

    @Nested
    @DisplayName("Pruebas de eliminación")
    class EliminacionTests {

        @Test
        @DisplayName("eliminarUsuario elimina exitosamente")
        void eliminarUsuario_Exito() {
            when(usuarioRepository.findById(USUARIO_ID)).thenReturn(Optional.of(usuario));

            usuarioService.eliminarUsuario(USUARIO_ID);

            verify(usuarioRepository).delete(usuario);
        }

        @Test
        @DisplayName("eliminarUsuario lanza RecursoNoEncontradoException cuando no existe")
        void eliminarUsuario_NoExiste() {
            when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> usuarioService.eliminarUsuario(99L))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }
    }
}

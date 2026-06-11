package evaluacion2.usuario.service;

import evaluacion2.usuario.dto.request.UsuarioRequestDTO;
import evaluacion2.usuario.dto.response.UsuarioResponseDTO;
import evaluacion2.usuario.exception.RecursoNoEncontradoException;
import evaluacion2.usuario.exception.ReglaNegocioException;
import evaluacion2.usuario.model.Usuario;
import evaluacion2.usuario.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> obtenerTodosLosUsuarios() {
        log.info("Consultando todos los usuarios");
        List<Usuario> usuarios = usuarioRepository.findAll();
        log.info("Se encontraron {} usuarios", usuarios.size());
        return usuarios.stream()
                .map(this::mapearAResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerUsuarioPorId(Long id) {
        log.info("Buscando usuario con id {}", id);
        Usuario usuario = buscarUsuarioPorId(id);
        log.info("Usuario '{}' encontrado", usuario.getNombre());
        return mapearAResponse(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerUsuarioPorEmail(String email) {
        log.info("Buscando usuario con email {}", email);
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario con email " + email));
        log.info("Usuario '{}' encontrado", usuario.getNombre());
        return mapearAResponse(usuario);
    }

    @Transactional
    public UsuarioResponseDTO crearUsuario(UsuarioRequestDTO dto) {
        log.info("Intentando crear usuario con email '{}'", dto.getEmail());

        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            log.warn("Creación fallida: ya existe un usuario con el email '{}'", dto.getEmail());
            throw new ReglaNegocioException("Ya existe un usuario con el email: " + dto.getEmail());
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(dto.getPassword());

        Usuario guardado = usuarioRepository.save(usuario);
        log.info("Usuario '{}' creado con id {}", guardado.getNombre(), guardado.getId());
        return mapearAResponse(guardado);
    }

    @Transactional
    public UsuarioResponseDTO actualizarUsuario(Long id, UsuarioRequestDTO dto) {
        log.info("Actualizando usuario con id {}", id);
        Usuario usuario = buscarUsuarioPorId(id);

        if (!usuario.getEmail().equals(dto.getEmail()) && usuarioRepository.existsByEmail(dto.getEmail())) {
            log.warn("Actualización fallida: el email '{}' ya está en uso", dto.getEmail());
            throw new ReglaNegocioException("Ya existe un usuario con el email: " + dto.getEmail());
        }

        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(dto.getPassword());

        Usuario actualizado = usuarioRepository.save(usuario);
        log.info("Usuario con id {} actualizado correctamente", id);
        return mapearAResponse(actualizado);
    }

    @Transactional
    public void eliminarUsuario(Long id) {
        log.info("Eliminando usuario con id {}", id);
        Usuario usuario = buscarUsuarioPorId(id);
        usuarioRepository.delete(usuario);
        log.info("Usuario con id {} eliminado exitosamente", id);
    }

    private Usuario buscarUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
    }

    private UsuarioResponseDTO mapearAResponse(Usuario usuario) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setEmail(usuario.getEmail());
        dto.setFechaRegistro(usuario.getFechaRegistro());
        return dto;
    }
}

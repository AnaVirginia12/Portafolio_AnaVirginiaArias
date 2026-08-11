package TechShop.Virginia.service;

import TechShop.Virginia.domain.Rol;
import TechShop.Virginia.domain.Usuario;
import TechShop.Virginia.repository.RolRepository;
import TechShop.Virginia.repository.UsuarioRepository;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final FirebaseStorageService firebaseStorageService;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            FirebaseStorageService firebaseStorageService,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.firebaseStorageService = firebaseStorageService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> getUsuarios(boolean activo) {
        if (activo) {
            return usuarioRepository.findByActivoTrue();
        }
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuario(Integer idUsuario) {
        return usuarioRepository.findById(idUsuario);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuarioPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuarioPorUsernameYPassword(String username, String password) {
        return usuarioRepository.findByUsernameAndPassword(username, password);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> getUsuarioPorUsernameOCorreo(String username, String correo) {
        return usuarioRepository.findByUsernameOrCorreo(username, correo);
    }

    @Transactional(readOnly = true)
    public boolean existeUsuarioPorUsernameOCorreo(String username, String correo) {
        return usuarioRepository.existsByUsernameOrCorreo(username, correo);
    }

    @Transactional
    public void save(Usuario usuario, MultipartFile imagenFile, boolean encriptaClave) {
        // Verificar si el correo ya existe, excluyendo el usuario actual
        final Integer idUser = usuario.getIdUsuario();
        Optional<Usuario> usuarioDuplicado = usuarioRepository.findByUsernameOrCorreo(null, usuario.getCorreo());
        if (usuarioDuplicado.isPresent()) {
            Usuario encontrado = usuarioDuplicado.get();
            // Verifica si estamos en modo CREACIÓN (idUser == null) O si el ID encontrado NO es el mismo que estamos actualizando
            if (idUser == null || !encontrado.getIdUsuario().equals(idUser)) {
                throw new DataIntegrityViolationException("El correo ya está en uso por otro usuario.");
            }
        }

        // Se valida si la clave se va actualizar o si es un usuario nuevo se debe actualizar...
        var asignarRol = false;
        if (usuario.getIdUsuario() == null) {
            if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
                throw new IllegalArgumentException("La contraseña es obligatoria para nuevos usuarios.");
            }
            // La primera vez como es activación no se encripta...
            usuario.setPassword(encriptaClave ? passwordEncoder.encode(usuario.getPassword()) : usuario.getPassword());
            asignarRol = true;
        } else {
            if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
                // El campo de password viene vacío en el formulario (no se desea cambiar)
                // Recuperamos la contraseña ya encriptada existente en la base de datos.
                Usuario usuarioExistente = usuarioRepository.findById(usuario.getIdUsuario())
                        .orElseThrow(() -> new IllegalArgumentException("Usuario a modificar no encontrado."));
                usuario.setPassword(usuarioExistente.getPassword());
            } else {
                // El campo de password NO está vacío (se desea cambiar)
                usuario.setPassword(encriptaClave ? passwordEncoder.encode(usuario.getPassword()) : usuario.getPassword());
            }
        }

        usuario = usuarioRepository.save(usuario);
        if (imagenFile != null && !imagenFile.isEmpty()) { // Si pasaron una imagen...
            try {
                String rutaImagen = firebaseStorageService.uploadImage(
                        imagenFile, "usuario", usuario.getIdUsuario());
                usuario.setRutaImagen(rutaImagen);
                usuarioRepository.save(usuario);
            } catch (IOException e) {
            }
        }
        if (asignarRol) {
            // Si se está creando el usuario, se le asigna el rol por defecto "USER"
            asignarRolPorUsername(usuario.getUsername(), "USER");
        }
    }

    @Transactional
    public void delete(Integer idUsuario) {
        if (!usuarioRepository.existsById(idUsuario)) {
            throw new IllegalArgumentException("El usuario con ID " + idUsuario + " no existe.");
        }
        try {
            usuarioRepository.deleteById(idUsuario);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("No se puede eliminar el usuario. Tiene datos asociados.", e);
        }
    }

    @Transactional
    public Usuario asignarRolPorUsername(String username, String rolStr) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
        Rol rol = rolRepository.findByRol(rolStr)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado."));
        usuario.getRoles().add(rol);
        return usuarioRepository.save(usuario);
    }
}

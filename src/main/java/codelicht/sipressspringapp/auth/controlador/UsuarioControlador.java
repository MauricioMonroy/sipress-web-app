package codelicht.sipressspringapp.auth.controlador;

import codelicht.sipressspringapp.auth.dtos.ActualizarUsuarioDto;
import codelicht.sipressspringapp.auth.entidad.Role;
import codelicht.sipressspringapp.auth.entidad.Usuario;
import codelicht.sipressspringapp.auth.repositorio.RoleRepositorio;
import codelicht.sipressspringapp.auth.servicio.UserService;
import codelicht.sipressspringapp.excepcion.RecursoNoEncontradoExcepcion;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador para la gestión de usuarios
 * Proporciona métodos para la obtención de información de usuarios autenticados y de todos los usuarios
 */
@RequestMapping("/sipress-app/usuarios")
@RestController
@CrossOrigin(value = "http://localhost:3000")
public class UsuarioControlador {
    private final UserService userService;
    private final RoleRepositorio roleRepositorio;

    public UsuarioControlador(UserService userService, RoleRepositorio roleRepositorio) {
        this.userService = userService;
        this.roleRepositorio = roleRepositorio;
    }

    @GetMapping("/perfil")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Usuario> authenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Usuario usuarioActual = (Usuario) authentication.getPrincipal();

        return ResponseEntity.ok(usuarioActual);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN')")
    public ResponseEntity<Usuario> obtenerUsuarioPorId(@PathVariable Integer id) {
        Usuario usuario = userService.findById(id);
        if (usuario == null)
            throw new RecursoNoEncontradoExcepcion("Usuario no encontrado con el id: " + id);
        return ResponseEntity.ok(usuario);
    }

    @GetMapping("/todos")
    @PreAuthorize("hasAnyRole('SUPERADMIN')")
    public ResponseEntity<List<Usuario>> allUsers() {
        List<Usuario> usuarios = userService.allUsers();

        return ResponseEntity.ok(usuarios);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN')")
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Integer id, @RequestBody ActualizarUsuarioDto usuarioActualizado) {
        Usuario usuarioExistente = userService.findById(id);

        if (usuarioExistente == null) {
            return ResponseEntity.notFound().build();
        }

        usuarioExistente.setNombreCompleto(usuarioActualizado.getNombreCompleto());
        usuarioExistente.setEmail(usuarioActualizado.getEmail());

        // Buscar y asignar el rol
        Optional<Role> nuevoRol = roleRepositorio.findByNombre(usuarioActualizado.getRole().getNombre());
        if (nuevoRol.isPresent()) {
            usuarioExistente.setRole(nuevoRol.get());
        } else {
            return ResponseEntity.badRequest().body(null);
        }

        if (usuarioActualizado.getPassword() != null && !usuarioActualizado.getPassword().isEmpty()) {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            usuarioExistente.setPassword(passwordEncoder.encode(usuarioActualizado.getPassword()));
        }

        Usuario usuarioGuardado = userService.saveUsuario(usuarioExistente);

        return ResponseEntity.ok(usuarioGuardado);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Boolean>> eliminarUsuario(@PathVariable Integer id) {
        Usuario usuario = userService.findById(id);
        if (usuario == null)
            throw new RecursoNoEncontradoExcepcion("Usuario no encontrado con el id: " + id);
        userService.eliminarUsuario(usuario);
        Map<String, Boolean> respuesta = new HashMap<>();
        respuesta.put("eliminado", Boolean.TRUE);
        return ResponseEntity.ok(respuesta);
    }

}

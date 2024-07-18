package codelicht.sipressspringapp.controlador;

import codelicht.sipressspringapp.modelo.Institucion;
import codelicht.sipressspringapp.servicio.interfaces.IInstitucionServicio;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
//http://localhost:8080/sipress-app/
@RequestMapping("sipress-app")
@CrossOrigin(value = "http://localhost:3000")
public class InstitucionControlador {

    private static final Logger logger = LoggerFactory.getLogger(InstitucionControlador.class);

    @Autowired
    private IInstitucionServicio institucionServicio;

    // http://localhost:8080/sipress-app/instituciones
    @GetMapping("/instituciones")
    public List<Institucion> obtenerInstituciones() {
        var instituciones = institucionServicio.listarInstituciones();
        instituciones.forEach((institucion -> logger.info(institucion.toString())));
        return instituciones;
    }

    @GetMapping("/instituciones/{id}")
    public Institucion buscarInstitucionPorId(@PathVariable Integer id) {
        return institucionServicio.buscarInstitucionPorId(id);
    }

    @PostMapping("/instituciones")
    public ResponseEntity<?> agregarInstitucion(@Valid @RequestBody Institucion institucion, BindingResult result) {
        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors().stream().map(FieldError::getDefaultMessage).collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }
        logger.info("Institucion a agregar: " + institucion);
        Institucion nuevaInstitucion = institucionServicio.guardarInstitucion(institucion);
        return ResponseEntity.ok(nuevaInstitucion);
    }

    @DeleteMapping("/instituciones/{id}")
    public ResponseEntity<Void> eliminarInstitucion(@PathVariable("id") Integer id) {
        Institucion institucion = institucionServicio.buscarInstitucionPorId(id);
        if (institucion != null) {
            institucionServicio.eliminarInstitucion(institucion);
            return ResponseEntity.noContent().build(); // Elimina y responde con 204 No Content
        } else {
            return ResponseEntity.notFound().build(); // Responde con 404 Not Found si no existe
        }
    }
}

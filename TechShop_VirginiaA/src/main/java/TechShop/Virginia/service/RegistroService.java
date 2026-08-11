/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TechShop.Virginia.service;


import TechShop.Virginia.domain.Usuario;
import jakarta.mail.MessagingException;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

@Service
public class RegistroService {

    private final CorreoService correoService;
    private final UsuarioService usuarioService;
    private final MessageSource messageSource;

    @Value("${servidor.http}")
    private String servidorHttp;

    public RegistroService(CorreoService correoService, UsuarioService usuarioService, MessageSource messageSource) {
        this.correoService = correoService;
        this.usuarioService = usuarioService;
        this.messageSource = messageSource;
    }

    // Genera una clave temporal aleatoria que se usará como "token" de activación
    private String demeClave() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    // Este método se usa en el enlace del correo enviado...
    public Model activar(Model model, String username, String clave) {
        Optional<Usuario> usuario = usuarioService.getUsuarioPorUsernameYPassword(username, clave);
        if (!usuario.isEmpty()) {  // Si estaba...
            model.addAttribute("usuario", usuario.get());
        } else { // hay que devolver error
            model.addAttribute("titulo", messageSource.getMessage("registro.activar", null, Locale.getDefault()));
            model.addAttribute("mensaje", messageSource.getMessage("registro.activar.error", null, Locale.getDefault()));
        }
        return model;
    }

    // Este método es el que finalmente crea el usuario en el sistema
    public void activar(Usuario usuario, MultipartFile imagenFile) {
        usuario.setActivo(true);
        usuarioService.save(usuario, imagenFile, true);
    }

    // Crea el usuario inactivo con una clave temporal y envía el correo de activación
    public Model crearUsuario(Model model, Usuario usuario) throws MessagingException {
        String mensaje;
        try {
            String clave = demeClave();
            usuario.setPassword(clave);
            usuario.setActivo(false);

            // Se guarda sin encriptar la clave, porque es temporal (solo sirve para el link)
            usuarioService.save(usuario, null, false);

            // Se arma el enlace de activación
            String enlace = servidorHttp + "/registro/activacion/" + usuario.getUsername() + "/" + clave;
            String contenidoCorreo = "<p>Hola " + usuario.getNombre() + ",</p>"
                    + "<p>Para activar tu cuenta haz clic en el siguiente enlace:</p>"
                    + "<a href='" + enlace + "'>Activar cuenta</a>";

            correoService.enviarCorreoHtml(usuario.getCorreo(), "Activación de cuenta", contenidoCorreo);
            mensaje = messageSource.getMessage("registro.exitoso", null, Locale.getDefault());
        } catch (NoSuchMessageException e) {
            mensaje = "Ocurrió un error al registrar el usuario.";
        }
        model.addAttribute("mensaje", mensaje);
        return model;
    }
}
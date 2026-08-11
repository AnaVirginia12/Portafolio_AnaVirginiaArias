/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package TechShop.Virginia.controller;

import TechShop.Virginia.domain.Usuario;
import TechShop.Virginia.service.RegistroService;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/registro")
public class RegistroController {

    private final RegistroService registroService;

    public RegistroController(RegistroService registroService) {
        this.registroService = registroService;
    }

    // Muestra el formulario de registro
    @GetMapping("/nuevo")
    public String nuevo(Model model, Usuario usuario) {
        return "/registro/nuevo";
    }

    // Muestra el formulario de "recordar contraseña"
    @GetMapping("/recordar")
    public String recordar(Model model, Usuario usuario) {
        return "/registro/recordar";
    }

    // Recibe el formulario de registro, crea el usuario inactivo y envía el correo
    @PostMapping("/crearUsuario")
    public String crearUsuario(Model model, Usuario usuario)
            throws MessagingException {
        model = registroService.crearUsuario(model, usuario);
        return "/registro/salida";
    }

    // Se activa desde el enlace recibido por correo: /registro/activacion/{usuario}/{id}
    @GetMapping("/activacion/{usuario}/{id}")
    public String activar(
            Model model,
            @PathVariable(value = "usuario") String usuario,
            @PathVariable(value = "id") String id) {
        model = registroService.activar(model, usuario, id);
        if (model.containsAttribute("usuario")) {
            return "/registro/activa";
        }
        return "/registro/salida";
    }

    // Recibe el formulario final donde el usuario define su clave definitiva
    @PostMapping("/activar")
    public String activarUsuario(Model model, Usuario usuario,
            @RequestParam MultipartFile imagenFile) {
        registroService.activar(usuario, imagenFile);
        model.addAttribute("mensaje", "Cuenta activada correctamente.");
        return "/registro/salida";
    }
}
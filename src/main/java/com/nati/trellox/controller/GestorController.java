package com.nati.trellox.controller;

import com.nati.trellox.model.Project;
import com.nati.trellox.model.User;
import com.nati.trellox.service.ProjectService;
import com.nati.trellox.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dashboard")
public class GestorController {

    private final ProjectService projectService;
    private final UserService userService;

    public GestorController(ProjectService projectService, UserService userService) {
        this.projectService = projectService;
        this.userService = userService;
    }

    // Método privado para obtener el usuario autenticado
    private User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userService.findByEmail(email).orElse(null);  // Devuelve null si no se encuentra el usuario
    }

    @GetMapping
    public String showDashboard(Model model) {
        User user = getAuthenticatedUser();

        // Verificar si el usuario está autenticado
        if (user == null) {
            return "redirect:/login";  // Redirigir a la página de login si el usuario no está autenticado
        }
// Filtrar los colaboradores
        List<User> collaborators = userService.getAllUsers().stream()
                .filter(u -> u.getRole().equals("COLLABORATOR")) // Filtrar por rol "COLLABORATOR"
                .toList();

        // Obtener los proyectos y colaboradores del usuario autenticado
        model.addAttribute("user", user.getFirst_name());
        model.addAttribute("projects", projectService.findByUserId(user.getId()));
        model.addAttribute("collaborators", collaborators);  // Suponiendo que tienes la relación configurada

        return "dashboard"; // Cargará dashboard.html desde /templates
    }

    @GetMapping("/new_project")
    public String mostrarFormularioProyecto(Model model) {
        model.addAttribute("project", new Project());
        return "new_project"; // Formulario para crear proyecto
    }

    @PostMapping("/new_project")
    public String guardarProyecto(@ModelAttribute Project project) {
        User user = getAuthenticatedUser();

        // Verificar si el usuario está autenticado
        if (user == null) {
            return "redirect:/login"; // Redirigir a la página de inicio de sesión si el usuario no está autenticado
        }

        // Asignar el usuario al proyecto antes de guardarlo
        project.setUser(user);
        projectService.save(project); // Guardar el proyecto

        return "redirect:/dashboard"; // Redirigir al dashboard
    }
}

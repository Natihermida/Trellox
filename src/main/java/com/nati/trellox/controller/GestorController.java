package com.nati.trellox.controller;

import com.nati.trellox.model.Project;
import com.nati.trellox.model.Task;
import com.nati.trellox.model.User;
import com.nati.trellox.service.ProjectService;
import com.nati.trellox.service.TaskService;
import com.nati.trellox.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/dashboard")
public class GestorController {

    private static final Logger log = LoggerFactory.getLogger(GestorController.class);
    private final ProjectService projectService;
    private final UserService userService;

    private PasswordEncoder passwordEncoder;
    @Autowired
    public GestorController(ProjectService projectService, UserService userService,PasswordEncoder passwordEncoder) {
        this.projectService = projectService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
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
                .filter(u -> u.getRole().equals("ROLE_COLABORATOR"))
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
    @GetMapping("/detail_project/{id}")
    public String verDetalleProyecto(@PathVariable Long id, Model model) {
        Project project = projectService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado"));
        List<User> colaboradores = userService.findByRole("ROLE_COLABORATOR");        model.addAttribute("project", project);
        model.addAttribute("task", new Task());
        model.addAttribute("colaborators", colaboradores);
        return "detail_project";
    }

    @Autowired
    private TaskService taskService;

    @PostMapping("/project/{id}/task")
    public String crearTarea(@PathVariable Long id, @ModelAttribute Task task, @RequestParam("user_id") Long userId) {
        Project project = projectService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado"));
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        task.setProject(project);
        task.setId(0); // ← fuerza que la trate como nueva
        task.setUser(user);  // asignar colaborador
        task.setStartdate(new java.util.Date());
        task.setStatus("Pendiente");

        taskService.save(task);
        return "redirect:/dashboard/detail_project/" + id;
    }

    @PostMapping("/delete_project/{id}")
    public String eliminarProyecto(@PathVariable Long id) {
        projectService.deleteById(id);
        return "redirect:/dashboard";
    }
    @GetMapping("/edit_project/{id}")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        Project proyecto = projectService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado"));
        model.addAttribute("project", proyecto);
        return "edit_project";
    }

    @PostMapping("/edit_project/{id}")
    public String actualizarProyecto(@PathVariable Long id, @ModelAttribute Project actualizado) {
        Project proyecto = projectService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado"));

        proyecto.setTitle(actualizado.getTitle());
        proyecto.setDescription(actualizado.getDescription());
        projectService.save(proyecto);

        return "redirect:/dashboard/detail_project/" + id;
    }
    @GetMapping("/task/edit/{id}")
    public String mostrarFormularioEdicionTarea(@PathVariable long id, Model model) {
        Task task = taskService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tarea no encontrada"));

        List<User> colaboradores = userService.getAllUsers().stream()
                .filter(u -> u.getRole().equals("ROLE_COLABORATOR"))
                .toList();

        model.addAttribute("task", task);
        model.addAttribute("colaboradores", colaboradores);
        return "edit_task";
    }
    @PostMapping("/task/edit/{id}")
    public String actualizarTarea(@PathVariable long id, @ModelAttribute Task updatedTask) {
        Task task = taskService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tarea no encontrada"));

        task.setTitle(updatedTask.getTitle());
        task.setDescription(updatedTask.getDescription());
        task.setDuedate(updatedTask.getDuedate());
        task.setStatus(updatedTask.getStatus());
        task.setUser(updatedTask.getUser());

        taskService.save(task);
        return "redirect:/dashboard/detail_project/" + task.getProject().getId();
    }
    @PostMapping("/task/delete/{id}")
    public String eliminarTarea(@PathVariable long id) {
        Task task = taskService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tarea no encontrada"));
        Long projectId = task.getProject().getId();
        taskService.deleteById(id);
        return "redirect:/dashboard/detail_project/" + projectId;
    }


    @GetMapping("/invite_collaborator")
    public String mostrarFormularioInvitacion(Model model) {
        model.addAttribute("colaborador", new User());
        return "invite_collaborator";
    }
    @PostMapping("/invite_collaborator")
    public String procesarInvitacion(@ModelAttribute("colaborador") User colaborador) {
        colaborador.setPassword(passwordEncoder.encode("1234"));
        colaborador.setRole("ROLE_COLABORATOR");
        userService.createUser(colaborador);
        return "redirect:/dashboard";
    }

    @GetMapping("/collaborator/{id}")
    public String verTareasDeColaborador(@PathVariable Long id, Model model) {
        User colaborador = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("Colaborador no encontrado"));

        List<Task> tareas = taskService.findByUserId(colaborador.getId());

        List<Task> tareasPendientes = tareas.stream()
                .filter(t -> !"Completada".equalsIgnoreCase(t.getStatus()))
                .toList();

        List<Task> tareasCompletadas = tareas.stream()
                .filter(t -> "Completada".equalsIgnoreCase(t.getStatus()))
                .toList();

        model.addAttribute("colaborador", colaborador);
        model.addAttribute("tareasPendientes", tareasPendientes);
        model.addAttribute("tareasCompletadas", tareasCompletadas);

        return "collaborator_por_gestor"; // Nombre de la nueva vista
    }


}

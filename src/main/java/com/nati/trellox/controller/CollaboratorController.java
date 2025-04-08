package com.nati.trellox.controller;

import com.nati.trellox.model.Comment;
import com.nati.trellox.model.Task;
import com.nati.trellox.model.User;
import com.nati.trellox.service.CommentService;
import com.nati.trellox.service.TaskService;
import com.nati.trellox.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class CollaboratorController {

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;
    @Autowired
    private CommentService commentService;

    @GetMapping("/collaborator")
    public String showCollaboratorDashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Task> tareasAsignadas = taskService.findByUserId(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("tasks", tareasAsignadas);
        return "collaborator";
    }

    @PostMapping("/task/{taskId}/comment")
    public String addComment(@PathVariable Long taskId,
                             @RequestParam String content) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.findByEmail(email).orElseThrow();

        Task task = taskService.findById(taskId).orElseThrow(); // ✅ sin conversión

        Comment comment = new Comment();
        comment.setContent(content);
        comment.setAuthor(user);
        comment.setTask(task);

        commentService.saveComment(comment); // ✅ ya no está null

        return "redirect:/collaborator";
    }
    @PostMapping("/task/{id}/status")
    public String marcarComoCompletada(@PathVariable Long id) {
        Task task = taskService.findById(id).orElseThrow();
        task.setStatus("Completada");
        taskService.save(task);
        return "redirect:/collaborator";
    }
    @GetMapping("/collaborator/{id}")
    public String viewCollaboratorTasks(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("Colaborador no encontrado"));

        List<Task> tareasAsignadas = taskService.findByUserId(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("tasks", tareasAsignadas);

        return "collaborator"; // Carga la vista con las tareas del colaborador
    }

    @PostMapping("/comment/edit")
    public String editarComentario(@RequestParam Long commentId,
                                   @RequestParam String newContent) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.findByEmail(email).orElseThrow();

        Comment comment = commentService.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));

        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new SecurityException("No tienes permiso para editar este comentario");
        }

        comment.setContent(newContent);
        commentService.saveComment(comment);

        return "redirect:/collaborator";
    }
    @PostMapping("/comment/delete")
    public String eliminarComentario(@RequestParam Long commentId) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.findByEmail(email).orElseThrow();

        Comment comment = commentService.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));

        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new SecurityException("No tienes permiso para eliminar este comentario");
        }

        commentService.deleteComment(commentId);

        return "redirect:/collaborator";
    }


}





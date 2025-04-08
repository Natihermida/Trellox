package com.nati.trellox.service;

import com.nati.trellox.model.Task;
import com.nati.trellox.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // 💾 Guardar o actualizar una tarea
    public void save(Task task) {
        taskRepository.save(task);
    }

    public Optional<Task> findById(Long id) {
        return taskRepository.findById(id);
    }
    // 🗑 Eliminar una tarea
    public void deleteById(long id) {
        taskRepository.deleteById(id);
    }

    // 🔍 Buscar tareas asignadas a un usuario
    public List<Task> findByUserId(Long userId) {
        return taskRepository.findByUserId(userId);
    }
}

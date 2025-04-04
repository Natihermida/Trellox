package com.nati.trellox.service;

import com.nati.trellox.model.Project;
import com.nati.trellox.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    // Crear o actualizar un proyecto
    public Project save(Project project) {
        return projectRepository.save(project);
    }

    // Obtener un proyecto por su ID
    public Optional<Project> findById(Long id) {
        return projectRepository.findById(id);
    }

    // Obtener todos los proyectos
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    // Obtener proyectos por usuario (gestor)
    public List<Project> findByUserId(Long userId) {
        return projectRepository.findByUserId(userId);  // Filtra los proyectos por userId
    }

    // Eliminar un proyecto
    public void deleteById(Long id) {
        projectRepository.deleteById(id);
    }
}


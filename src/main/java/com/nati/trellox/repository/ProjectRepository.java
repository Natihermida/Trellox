package com.nati.trellox.repository;

import com.nati.trellox.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    // Añadir este método para buscar proyectos por el id del usuario
    List<Project> findByUserId(Long userId);
}

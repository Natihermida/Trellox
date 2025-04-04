package com.nati.trellox.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CollaboratorController {
    @GetMapping("/colaborator")
    public String showCollaboratorDashboard() {
        return "colaborador"; // se buscará colaborador.html en /templates/
    }
}

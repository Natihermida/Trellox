package com.nati.trellox.controller;

import com.nati.trellox.model.User;
import com.nati.trellox.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MainController {
    private final UserService userService;
    private final PasswordEncoder encoder;
    @Autowired
    public MainController(UserService userService, PasswordEncoder encoder) {
        this.userService = userService;
        this.encoder = encoder;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }
@PostMapping("/register")
    public String register(User user) {
        // Aquí puedes guardar el usuario en la base de datos
        // Por ejemplo, usando un UserRepository
        user.setPassword(encoder.encode(user.getPassword()));
        user.setRole("ROLE_GESTOR");
        userService.createUser(user);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }



}

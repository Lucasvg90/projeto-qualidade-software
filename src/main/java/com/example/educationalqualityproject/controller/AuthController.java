package com.example.educationalqualityproject.controller;

import com.example.educationalqualityproject.service.UserAccountService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserAccountService userAccountService;

    public AuthController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String createAccount(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            Model model) {
        try {
            userAccountService.registerRequester(name, email, password);
            return "redirect:/login?registered";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("error", exception.getMessage());
            return "auth/register";
        }
    }
}

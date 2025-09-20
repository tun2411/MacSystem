package com.example.maschat.controller;

import com.example.maschat.domain.User;
import com.example.maschat.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String loginPage() { return "login"; }

    @PostMapping("/login")
    public String doLogin(@RequestParam String email,
                          @RequestParam String password,
                          HttpSession session,
                          Model model) {
        return authService.login(email, password)
                .map(u -> {
                    session.setAttribute("uid", u.getId());
                    session.setAttribute("uname", u.getDisplayName());
                    session.setAttribute("isStaff", u.isStaff());
                    return "redirect:/";
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "Sai email hoặc mật khẩu");
                    return "login";
                });
    }

    @GetMapping("/register")
    public String registerPage() { return "register"; }

    @PostMapping("/register")
    public String doRegister(@RequestParam String email,
                             @RequestParam String displayName,
                             @RequestParam String password,
                             Model model) {
        try {
            User u = authService.register(email, displayName, password);
            model.addAttribute("message", "Đăng ký thành công. Vui lòng đăng nhập.");
            return "login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}



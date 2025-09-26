package com.example.maschat.controller;

import com.example.maschat.domain.User;
import com.example.maschat.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    @Autowired
    private AuthService authService;


    @GetMapping("/login")
    public String loginPage(HttpSession session, 
                           jakarta.servlet.http.HttpServletRequest request,
                           Model model) {
        // Kiểm tra nếu đã đăng nhập
        String uid = (String) session.getAttribute("uid");
        if (uid != null) {
            return "redirect:/";
        }
        
        // Kiểm tra Remember Me cookie
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if ("rememberMe".equals(cookie.getName())) {
                    String userId = cookie.getValue();
                    return authService.findById(userId)
                            .map(u -> {
                                session.setAttribute("uid", u.getId());
                                session.setAttribute("uname", u.getDisplayName());
                                session.setAttribute("isStaff", u.isStaff());
                                session.setAttribute("isAdmin", u.isAdmin());
                                return "redirect:/";
                            })
                            .orElse("login");
                }
            }
        }
        
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String email,
                          @RequestParam String password,
                          @RequestParam(required = false) String rememberMe,
                          HttpSession session,
                          Model model,
                          jakarta.servlet.http.HttpServletResponse response) {
        return authService.login(email, password)
                .map(u -> {
                    session.setAttribute("uid", u.getId());
                    session.setAttribute("uname", u.getDisplayName());
                    session.setAttribute("isStaff", u.isStaff());
                    session.setAttribute("isAdmin", u.isAdmin());
                    
                    // Nếu user chọn Remember Me, tạo cookie
                    if ("true".equals(rememberMe)) {
                        jakarta.servlet.http.Cookie rememberCookie = new jakarta.servlet.http.Cookie("rememberMe", u.getId());
                        rememberCookie.setMaxAge(30 * 24 * 60 * 60); // 30 ngày
                        rememberCookie.setPath("/");
                        rememberCookie.setHttpOnly(true);
                        response.addCookie(rememberCookie);
                    }
                    
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
    public String logoutPost(HttpSession session, jakarta.servlet.http.HttpServletResponse response) {
        return performLogout(session, response);
    }

    @GetMapping("/logout")
    public String logoutGet(HttpSession session, jakarta.servlet.http.HttpServletResponse response) {
        return performLogout(session, response);
    }

    @GetMapping("/logout-simple")
    public String logoutSimple(HttpSession session, jakarta.servlet.http.HttpServletResponse response) {
        System.out.println("DEBUG: Simple logout requested");
        
        // Xóa session đơn giản
        if (session != null) {
            session.invalidate();
            System.out.println("DEBUG: Session invalidated");
        }
        
        // Xóa cookie
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("rememberMe", "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        
        return "redirect:/login";
    }

    private String performLogout(HttpSession session, jakarta.servlet.http.HttpServletResponse response) {
        System.out.println("DEBUG: Logout requested");
        System.out.println("DEBUG: Session ID before logout: " + (session != null ? session.getId() : "null"));
        
        // Xóa tất cả session attributes trước
        if (session != null) {
            session.removeAttribute("uid");
            session.removeAttribute("uname");
            session.removeAttribute("isStaff");
            session.removeAttribute("isAdmin");
            System.out.println("DEBUG: Session attributes cleared");
        }
        
        // Xóa Remember Me cookie với nhiều cách
        jakarta.servlet.http.Cookie rememberCookie = new jakarta.servlet.http.Cookie("rememberMe", "");
        rememberCookie.setMaxAge(0);
        rememberCookie.setPath("/");
        rememberCookie.setHttpOnly(true);
        response.addCookie(rememberCookie);
        
        // Tạo cookie khác với domain khác để đảm bảo xóa
        jakarta.servlet.http.Cookie rememberCookie2 = new jakarta.servlet.http.Cookie("rememberMe", "");
        rememberCookie2.setMaxAge(0);
        rememberCookie2.setPath("/");
        rememberCookie2.setHttpOnly(false);
        response.addCookie(rememberCookie2);
        
        System.out.println("DEBUG: Cookies cleared, invalidating session");
        if (session != null) {
            session.invalidate();
            System.out.println("DEBUG: Session invalidated successfully");
        } else {
            System.out.println("DEBUG: Session was null, nothing to invalidate");
        }
        
        System.out.println("DEBUG: Redirecting to login");
        return "redirect:/login";
    }
}



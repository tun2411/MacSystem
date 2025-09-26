package com.example.maschat.config;

import com.example.maschat.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AutoLoginInterceptor implements HandlerInterceptor {

    @Autowired
    private AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        
        // Không chạy auto-login trên trang logout và login
        if (requestURI.contains("/logout") || requestURI.contains("/login")) {
            return true;
        }
        
        HttpSession session = request.getSession(false);
        
        // Nếu đã đăng nhập, không cần kiểm tra
        if (session != null && session.getAttribute("uid") != null) {
            return true;
        }
        
        // Kiểm tra Remember Me cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("rememberMe".equals(cookie.getName())) {
                    String userId = cookie.getValue();
                    System.out.println("DEBUG: AutoLoginInterceptor found rememberMe cookie for user: " + userId);
                    authService.findById(userId).ifPresent(user -> {
                        // Tạo session mới và lưu thông tin user
                        HttpSession newSession = request.getSession(true);
                        newSession.setAttribute("uid", user.getId());
                        newSession.setAttribute("uname", user.getDisplayName());
                        newSession.setAttribute("isStaff", user.isStaff());
                        newSession.setAttribute("isAdmin", user.isAdmin());
                        System.out.println("DEBUG: AutoLoginInterceptor created new session for user: " + user.getId());
                    });
                    break;
                }
            }
        }
        
        return true;
    }
}

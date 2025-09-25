package com.example.maschat.controller;

import com.example.maschat.domain.Agent;
import com.example.maschat.domain.Conversation;
import com.example.maschat.domain.User;
import com.example.maschat.service.ConversationService;
import com.example.maschat.repo.AgentRepository;
import com.example.maschat.repo.ConversationRepository;
import com.example.maschat.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/conversations")
    public String conversations(Model model, jakarta.servlet.http.HttpSession session) {
        String uid = (String) session.getAttribute("uid");
        if (uid == null) {
            return "redirect:/login";
        }
        
        Boolean isStaff = (Boolean) session.getAttribute("isStaff");
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        
        if (isAdmin != null && isAdmin) {
            // Admin thấy tất cả cuộc trò chuyện
            model.addAttribute("conversations", conversationRepository.findAll());
        } else if (isStaff != null && isStaff) {
            // Staff chỉ thấy các cuộc trò chuyện có staff participant
            model.addAttribute("conversations", conversationRepository.findAllHavingStaffParticipants());
        } else {
            // User thông thường chỉ thấy cuộc trò chuyện do chính họ tạo ra
            model.addAttribute("conversations", conversationRepository.findByCreatedByUserOrderByCreatedAtDesc(uid));
        }
        
        // Thêm thông tin user vào model
        User user = userRepository.findById(uid).orElse(null);
        if (user != null) {
            model.addAttribute("currentUser", user);
        }
        
        return "conversations";
    }

    @GetMapping("/conversations/new")
    public String newConversation(Model model, jakarta.servlet.http.HttpSession session) {
        // Kiểm tra nếu là staff thì chuyển hướng về trang conversations
        Boolean isStaff = (Boolean) session.getAttribute("isStaff");
        if (isStaff != null && isStaff) {
            return "redirect:/conversations";
        }
        
        List<Agent> agents = agentRepository.findByActiveTrue();
        model.addAttribute("agents", agents);
        String uid = (String) session.getAttribute("uid");
        if (uid == null) {
            return "redirect:/login";
        }
        return "conversation_new";
    }

    @PostMapping("/conversations/start")
    public String startConversation(@RequestParam String title,
                                    @RequestParam(required = false) List<String> agentIds,
                                    Model model,
                                    jakarta.servlet.http.HttpSession session) {
        // Kiểm tra nếu là staff thì chuyển hướng về trang conversations
        Boolean isStaff = (Boolean) session.getAttribute("isStaff");
        if (isStaff != null && isStaff) {
            return "redirect:/conversations";
        }
        
        String uid = (String) session.getAttribute("uid");
        if (uid == null) return "redirect:/login";
        Conversation c = conversationService.startConversation(title, uid, agentIds == null ? List.of() : agentIds);
        return "redirect:/chat/" + c.getId();
    }
}



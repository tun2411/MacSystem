package com.example.maschat.controller;

import com.example.maschat.domain.Conversation;
import com.example.maschat.domain.Message;
import com.example.maschat.repo.ConversationRepository;
import com.example.maschat.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/chat")
public class ChatViewController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private ConversationRepository conversationRepository;

    @GetMapping("/{conversationId}")
    public String chat(@PathVariable String conversationId, Model model, jakarta.servlet.http.HttpSession session) {
        String uid = (String) session.getAttribute("uid");
        if (uid == null) return "redirect:/login";
        
        // Kiểm tra quyền truy cập cuộc trò chuyện
        Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
        if (conversation == null) {
            return "redirect:/login"; // Cuộc trò chuyện không tồn tại
        }
        
        Boolean isStaff = (Boolean) session.getAttribute("isStaff");
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        
        // Kiểm tra quyền truy cập
        boolean hasAccess = false;
        if (isAdmin != null && isAdmin) {
            // Admin có thể xem tất cả cuộc trò chuyện
            hasAccess = true;
        } else if (isStaff != null && isStaff) {
            // Staff có thể truy cập nếu cuộc trò chuyện có staff participants hoặc được đánh dấu isStaffEngaged
            hasAccess = conversationRepository.findAllHavingStaffParticipants()
                    .stream()
                    .anyMatch(c -> c.getId().equals(conversationId));
            if (!hasAccess && Boolean.TRUE.equals(conversation.getIsStaffEngaged())) {
                hasAccess = true;
            }
        } else {
            // User thông thường chỉ có thể truy cập cuộc trò chuyện do chính họ tạo ra
            hasAccess = uid.equals(conversation.getCreatedByUser());
        }
        
        if (!hasAccess) {
            return "redirect:/login"; // Không có quyền truy cập
        }
        
        // Lấy tin nhắn và hiển thị chat
        List<Message> messages = messageService.getMessages(conversationId);
        model.addAttribute("conversationId", conversationId);
        model.addAttribute("conversationTitle", conversation.getTitle());
        model.addAttribute("messages", messages);
        model.addAttribute("currentUserId", uid);
        model.addAttribute("isStaff", isStaff != null ? isStaff : false);
        model.addAttribute("isAdmin", isAdmin != null ? isAdmin : false);
        return "chat";
    }
}



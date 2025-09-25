package com.example.maschat.controller;

import com.example.maschat.domain.Conversation;
import com.example.maschat.repo.ConversationRepository;
import com.example.maschat.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/chat")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private ConversationRepository conversationRepository;


    @PostMapping("/{conversationId}/send")
    public String send(@PathVariable String conversationId,
                       @RequestParam(required = false) String content,
                       jakarta.servlet.http.HttpServletRequest request,
                       jakarta.servlet.http.HttpSession session) {
        String uid = (String) session.getAttribute("uid");
        if (uid == null) return "redirect:/login";

        // Kiểm tra quyền truy cập cuộc trò chuyện
        Conversation conversation = conversationRepository.findById(conversationId).orElse(null);
        if (conversation == null) {
            return "redirect:/login"; // Cuộc trò chuyện không tồn tại
        }
        
        Boolean isStaff = (Boolean) session.getAttribute("isStaff");
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        
        // Admin không thể gửi tin nhắn
        if (isAdmin != null && isAdmin) {
            return "redirect:/chat/" + conversationId + "?error=admin_cannot_send";
        }
        
        // Kiểm tra quyền truy cập
        boolean hasAccess = false;
        if (isStaff != null && isStaff) {
            // Staff có thể gửi tin nhắn nếu cuộc trò chuyện có staff participants hoặc được đánh dấu isStaffEngaged
            hasAccess = conversationRepository.findAllHavingStaffParticipants()
                    .stream()
                    .anyMatch(c -> c.getId().equals(conversationId));
            if (!hasAccess && Boolean.TRUE.equals(conversation.getIsStaffEngaged())) {
                hasAccess = true;
            }
        } else {
            // User thông thường chỉ có thể gửi tin nhắn vào cuộc trò chuyện do chính họ tạo ra
            hasAccess = uid.equals(conversation.getCreatedByUser());
        }
        
        if (!hasAccess) {
            return "redirect:/login"; // Không có quyền truy cập
        }

        if (content == null || content.trim().isEmpty()) {
            return "redirect:/chat/" + conversationId + "?error=empty_message";
        }

        if (isStaff != null && isStaff) {
            messageService.sendStaffMessage(conversationId, uid, content.trim());
        } else {
            messageService.sendUserMessage(conversationId, uid, content.trim());
        }
        return "redirect:/chat/" + conversationId;
    }
}



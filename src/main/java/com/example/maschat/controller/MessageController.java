package com.example.maschat.controller;

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


    @PostMapping("/{conversationId}/send")
    public String send(@PathVariable String conversationId,
                       @RequestParam(required = false) String content,
                       jakarta.servlet.http.HttpServletRequest request,
                       jakarta.servlet.http.HttpSession session) {
        String uid = (String) session.getAttribute("uid");
        if (uid == null) return "redirect:/login";

        if (content == null || content.trim().isEmpty()) {
            return "redirect:/chat/" + conversationId + "?error=empty_message";
        }

        Boolean isStaff = (Boolean) session.getAttribute("isStaff");
        if (isStaff != null && isStaff) {
            messageService.sendStaffMessage(conversationId, uid, content.trim());
        } else {
            messageService.sendUserMessage(conversationId, uid, content.trim());
        }
        return "redirect:/chat/" + conversationId;
    }
}



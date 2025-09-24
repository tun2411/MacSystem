package com.example.maschat.controller;

import com.example.maschat.domain.Message;
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

    @GetMapping("/{conversationId}")
    public String chat(@PathVariable String conversationId, Model model, jakarta.servlet.http.HttpSession session) {
        List<Message> messages = messageService.getMessages(conversationId);
        model.addAttribute("conversationId", conversationId);
        model.addAttribute("messages", messages);
        String uid = (String) session.getAttribute("uid");
        if (uid == null) return "redirect:/login";
        model.addAttribute("currentUserId", uid);

        Boolean isStaff = (Boolean) session.getAttribute("isStaff");
        model.addAttribute("isStaff", isStaff != null ? isStaff : false);
        return "chat";
    }
}



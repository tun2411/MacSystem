package com.example.maschat.controller;

import com.example.maschat.domain.Agent;
import com.example.maschat.domain.Conversation;
import com.example.maschat.service.ConversationService;
import com.example.maschat.repo.AgentRepository;
import com.example.maschat.repo.ConversationRepository;
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

    public ConversationController(ConversationService conversationService, ConversationRepository conversationRepository, AgentRepository agentRepository) {
        this.conversationService = conversationService;
        this.conversationRepository = conversationRepository;
        this.agentRepository = agentRepository;
    }

    @GetMapping("/conversations")
    public String conversations(Model model, jakarta.servlet.http.HttpSession session) {
        Boolean isStaff = (Boolean) session.getAttribute("isStaff");
        if (isStaff != null && isStaff) {
            model.addAttribute("conversations", conversationRepository.findAllHavingStaffParticipants());
        } else {
            model.addAttribute("conversations", conversationRepository.findAll());
        }
        return "conversations";
    }

    @GetMapping("/conversations/new")
    public String newConversation(Model model, jakarta.servlet.http.HttpSession session) {
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
        String uid = (String) session.getAttribute("uid");
        if (uid == null) return "redirect:/login";
        Conversation c = conversationService.startConversation(title, uid, agentIds == null ? List.of() : agentIds);
        return "redirect:/chat/" + c.getId();
    }
}



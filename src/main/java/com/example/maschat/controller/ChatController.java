package com.example.maschat.controller;

import com.example.maschat.domain.Agent;
import com.example.maschat.domain.Conversation;
import com.example.maschat.domain.Message;
import com.example.maschat.repo.AgentRepository;
import com.example.maschat.repo.ConversationRepository;
import com.example.maschat.service.ChatService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ChatController {
    private final ChatService chatService;
    private final AgentRepository agentRepository;
    private final ConversationRepository conversationRepository;

    public ChatController(ChatService chatService, AgentRepository agentRepository, ConversationRepository conversationRepository) {
        this.chatService = chatService;
        this.agentRepository = agentRepository;
        this.conversationRepository = conversationRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<Agent> agents = agentRepository.findByActiveTrue();
        model.addAttribute("agents", agents);
        return "index";
    }

    @GetMapping("/conversations")
    public String conversations(Model model) {
        model.addAttribute("conversations", conversationRepository.findAll());
        return "conversations";
    }

    @PostMapping("/start")
    public String startConversation(@RequestParam String title,
                                    @RequestParam(required = false) List<String> agentIds,
                                    Model model) {
        String demoUserId = "00000000-0000-0000-0000-000000000001";
        Conversation c = chatService.startConversation(title, demoUserId, agentIds == null ? List.of() : agentIds);
        return "redirect:/chat/" + c.getId();
    }

    @GetMapping("/chat/{conversationId}")
    public String chat(@PathVariable String conversationId, Model model) {
        List<Message> messages = chatService.getMessages(conversationId);
        model.addAttribute("conversationId", conversationId);
        model.addAttribute("messages", messages);
        model.addAttribute("currentUserId", "00000000-0000-0000-0000-000000000001");
        return "chat";
    }

    @PostMapping("/chat/{conversationId}/send")
    public String send(@PathVariable String conversationId, @RequestParam String content) {
        String demoUserId = "00000000-0000-0000-0000-000000000001";
        chatService.sendUserMessage(conversationId, demoUserId, content);
        return "redirect:/chat/" + conversationId;
    }
}



package com.example.maschat.controller;

import com.example.maschat.domain.Agent;
import com.example.maschat.domain.Conversation;
import com.example.maschat.domain.Message;
import com.example.maschat.domain.ConversationParticipant;
import com.example.maschat.repo.AgentRepository;
import com.example.maschat.repo.ConversationRepository;
import com.example.maschat.repo.ConversationParticipantRepository;
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
    private final ConversationParticipantRepository participantRepository;

    public ChatController(ChatService chatService, AgentRepository agentRepository, ConversationRepository conversationRepository, ConversationParticipantRepository participantRepository) {
        this.chatService = chatService;
        this.agentRepository = agentRepository;
        this.conversationRepository = conversationRepository;
        this.participantRepository = participantRepository;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/conversations";
    }

    @GetMapping("/conversations")
    public String conversations(Model model) {
        model.addAttribute("conversations", conversationRepository.findAll());
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

    @PostMapping("/start")
    public String startConversation(@RequestParam String title,
                                    @RequestParam(required = false) List<String> agentIds,
                                    Model model,
                                    jakarta.servlet.http.HttpSession session) {
        String uid = (String) session.getAttribute("uid");
        if (uid == null) return "redirect:/login";
        Conversation c = chatService.startConversation(title, uid, agentIds == null ? List.of() : agentIds);
        return "redirect:/chat/" + c.getId();
    }

    @GetMapping("/chat/{conversationId}")
    public String chat(@PathVariable String conversationId, Model model, jakarta.servlet.http.HttpSession session) {
        List<Message> messages = chatService.getMessages(conversationId);
        model.addAttribute("conversationId", conversationId);
        model.addAttribute("messages", messages);
        String uid = (String) session.getAttribute("uid");
        if (uid == null) return "redirect:/login";
        model.addAttribute("currentUserId", uid);
        return "chat";
    }

    @PostMapping("/chat/{conversationId}/send")
    public String send(@PathVariable String conversationId, @RequestParam String content, jakarta.servlet.http.HttpSession session) {
        String uid = (String) session.getAttribute("uid");
        if (uid == null) return "redirect:/login";
        chatService.sendUserMessage(conversationId, uid, content);
        return "redirect:/chat/" + conversationId;
    }

    @GetMapping("/chat/{conversationId}/agents")
    public String manageAgents(@PathVariable String conversationId, Model model, jakarta.servlet.http.HttpSession session) {
        String uid = (String) session.getAttribute("uid");
        if (uid == null) return "redirect:/login";
        model.addAttribute("conversationId", conversationId);
        model.addAttribute("allAgents", agentRepository.findByActiveTrue());
        List<ConversationParticipant> currentAgents = participantRepository.findByConversationIdAndParticipantType(conversationId, "agent");
        model.addAttribute("currentAgentIds", currentAgents.stream().map(ConversationParticipant::getAgentId).toList());
        return "conversation_agents";
    }

    @PostMapping("/chat/{conversationId}/agents")
    public String updateAgents(@PathVariable String conversationId,
                               @RequestParam(required = false, name = "agentIds") List<String> agentIds,
                               jakarta.servlet.http.HttpSession session) {
        String uid = (String) session.getAttribute("uid");
        if (uid == null) return "redirect:/login";
        chatService.updateConversationAgents(conversationId, agentIds == null ? List.of() : agentIds);
        return "redirect:/chat/" + conversationId;
    }
}



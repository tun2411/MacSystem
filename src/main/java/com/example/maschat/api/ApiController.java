package com.example.maschat.api;

import com.example.maschat.domain.Conversation;
import com.example.maschat.domain.Message;
import com.example.maschat.repo.ConversationRepository;
import com.example.maschat.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final ChatService chatService;
    private final ConversationRepository conversationRepository;

    public ApiController(ChatService chatService, ConversationRepository conversationRepository) {
        this.chatService = chatService;
        this.conversationRepository = conversationRepository;
    }

    @GetMapping("/conversations")
    public List<Conversation> listConversations() {
        return conversationRepository.findAll();
    }

    public record CreateConversationRequest(String title, List<String> agentIds) {}

    @PostMapping("/conversations")
    public ResponseEntity<Conversation> createConversation(@RequestBody CreateConversationRequest req) {
        String demoUserId = "00000000-0000-0000-0000-000000000001";
        Conversation c = chatService.startConversation(
                req.title(), demoUserId, req.agentIds() == null ? List.of() : req.agentIds());
        return ResponseEntity.ok(c);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<Message>> listMessages(@PathVariable String conversationId) {
        if (!conversationRepository.existsById(conversationId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(chatService.getMessages(conversationId));
    }

    public record SendMessageRequest(String content) {}

    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<Message> sendMessage(@PathVariable String conversationId, @RequestBody SendMessageRequest req) {
        if (!conversationRepository.existsById(conversationId)) {
            return ResponseEntity.notFound().build();
        }
        String demoUserId = "00000000-0000-0000-0000-000000000001";
        Message m = chatService.sendUserMessage(conversationId, demoUserId, req.content());
        return ResponseEntity.ok(m);
    }

    public record EditMessageRequest(String roleKey, String newContent) {}

    @PatchMapping("/messages/{messageId}")
    public ResponseEntity<?> editMessage(@PathVariable String messageId, @RequestBody EditMessageRequest req) {
        try {
            Message m = chatService.editMessage(messageId, req.roleKey(), req.newContent());
            return ResponseEntity.ok(m);
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(403).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }
}



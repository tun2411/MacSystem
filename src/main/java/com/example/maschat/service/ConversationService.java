package com.example.maschat.service;

import com.example.maschat.domain.Conversation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationService {

    @Autowired
    private ChatService chatService;


    public Conversation startConversation(String title, String createdByUserId, List<String> agentIds) {
        return chatService.startConversation(title, createdByUserId, agentIds);
    }

    public void updateConversationAgents(String conversationId, String agentId) {
        chatService.updateConversationAgents(conversationId, agentId);
    }
}



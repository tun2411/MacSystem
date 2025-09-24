package com.example.maschat.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AgentResponseService {

    @Autowired
    private ChatService chatService;


    public void triggerResponses(String conversationId, java.time.Instant afterTime) {
        // delegate to existing logic for now
        try {
            java.lang.reflect.Method m = ChatService.class.getDeclaredMethod("sendAgentResponseWithRetry", String.class, java.time.Instant.class);
            m.setAccessible(true);
            m.invoke(chatService, conversationId, afterTime);
        } catch (Exception ignore) {
        }
    }
}



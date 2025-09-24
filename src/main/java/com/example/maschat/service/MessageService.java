package com.example.maschat.service;

import com.example.maschat.domain.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private ChatService chatService;

    public List<Message> getMessages(String conversationId) {
        return chatService.getMessages(conversationId);
    }

    public Message sendUserMessage(String conversationId, String userId, String content) {
        return chatService.sendUserMessage(conversationId, userId, content);
    }

    public Message sendStaffMessage(String conversationId, String userId, String content) {
        return chatService.sendStaffMessage(conversationId, userId, content);
    }

    public Message editMessage(String messageId, String requesterRoleKey, String newContent) {
        return chatService.editMessage(messageId, requesterRoleKey, newContent);
    }
}



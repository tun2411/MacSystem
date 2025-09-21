package com.example.maschat.repo;

import com.example.maschat.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, String> {
    List<Message> findByConversationIdOrderByCreatedAtAsc(String conversationId);
    List<Message> findByConversationIdAndSenderTypeOrderByCreatedAtAsc(String conversationId, String senderType);
}



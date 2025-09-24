package com.example.maschat.repo;

import com.example.maschat.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, String> {
    List<Message> findByConversationIdOrderByCreatedAtAsc(String conversationId);
    List<Message> findByConversationIdAndSenderTypeOrderByCreatedAtAsc(String conversationId, String senderType);
    List<Message> findByConversationIdAndRoleKeyOrderByCreatedAtAsc(String conversationId, String roleKey);

    // Ensure deterministic ordering when timestamps are equal: user first, then staff, then agent
    @Query(value = "SELECT * FROM messages m WHERE m.conversation_id = :conversationId " +
            "ORDER BY m.created_at ASC, " +
            "CASE WHEN m.sender_type = 'user' THEN 0 WHEN m.sender_type = 'staff' THEN 1 ELSE 2 END ASC, " +
            "m.id ASC", nativeQuery = true)
    List<Message> findOrderedForConversation(@Param("conversationId") String conversationId);
}



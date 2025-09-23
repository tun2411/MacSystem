package com.example.maschat.repo;

import com.example.maschat.domain.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConversationRepository extends JpaRepository<Conversation, String> {
    java.util.List<Conversation> findByCreatedByUserOrderByCreatedAtDesc(String createdByUser);

    @Query(value = "SELECT * FROM conversations c WHERE EXISTS (SELECT 1 FROM messages m WHERE m.conversation_id = c.id AND m.role_key = :roleKey)", nativeQuery = true)
    java.util.List<Conversation> findAllHavingMessagesWithRole(@Param("roleKey") String roleKey);

    @Query(value = "SELECT * FROM conversations c WHERE EXISTS (SELECT 1 FROM conversation_participants p WHERE p.conversation_id = c.id AND p.participant_type = 'agent' AND p.role_key = 'staff') ORDER BY c.created_at DESC", nativeQuery = true)
    java.util.List<Conversation> findAllHavingStaffParticipants();
}



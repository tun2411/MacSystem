package com.example.maschat.repo;

import com.example.maschat.domain.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Long> {
    List<ConversationParticipant> findByConversationIdOrderByJoinedAtAsc(String conversationId);
    List<ConversationParticipant> findByConversationIdAndParticipantType(String conversationId, String participantType);
    void deleteByConversationIdAndParticipantType(String conversationId, String participantType);
}



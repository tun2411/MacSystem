package com.example.maschat.repo;

import com.example.maschat.domain.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<Conversation, String> {
    java.util.List<Conversation> findByCreatedByUserOrderByCreatedAtDesc(String createdByUser);
}



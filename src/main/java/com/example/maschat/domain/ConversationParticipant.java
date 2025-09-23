package com.example.maschat.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "conversation_participants")
public class ConversationParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "conversation_id", length = 36, nullable = false)
    private String conversationId;

    @Column(name = "participant_type", nullable = false)
    private String participantType; // 'user' or 'agent'

    @Column(name = "user_id", length = 36)
    private String userId;

    @Column(name = "agent_id", length = 36)
    private String agentId;

    @Column(name = "role_key", nullable = false)
    private String roleKey; // 'user','staff','agent','supervisor'

    @Column(name = "joined_at")
    private Instant joinedAt;

    @Column(name = "left_at")
    private Instant leftAt;

}



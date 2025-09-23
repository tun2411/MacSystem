package com.example.maschat.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;


@Getter
@Setter
@Entity
@Table(name = "messages")
public class Message {
    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "conversation_id", length = 36, nullable = false)
    private String conversationId;

    @Column(name = "sender_type", nullable = false)
    private String senderType; // 'user','agent','staff'

    @Column(name = "sender_user_id", length = 36)
    private String senderUserId;

    @Column(name = "sender_agent_id", length = 36)
    private String senderAgentId;

    @Column(name = "role_key", nullable = false)
    private String roleKey;

    @Column(name = "content", columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "edited_at")
    private Instant editedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "reply_to_message_id", length = 36)
    private String replyToMessageId;

    @Column(name = "meta", columnDefinition = "LONGTEXT")
    private String meta;

}



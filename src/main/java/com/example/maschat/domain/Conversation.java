package com.example.maschat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "conversations")
public class Conversation {
    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "title")
    private String title;

    @Column(name = "created_by_user", length = 36)
    private String createdByUser;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "status")
    private String status;

    @Column(name = "is_staff_engaged")
    private Boolean isStaffEngaged = false;

}



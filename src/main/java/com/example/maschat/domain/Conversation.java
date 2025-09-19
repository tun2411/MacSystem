package com.example.maschat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

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

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCreatedByUser() { return createdByUser; }
    public void setCreatedByUser(String createdByUser) { this.createdByUser = createdByUser; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}



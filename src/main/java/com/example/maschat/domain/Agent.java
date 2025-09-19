package com.example.maschat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "agents")
public class Agent {
    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "handle", unique = true, nullable = false)
    private String handle;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "kind", nullable = false)
    private String kind;

    @Column(name = "metadata", columnDefinition = "LONGTEXT")
    private String metadata;

    @Column(name = "active")
    private boolean active;

    @Column(name = "created_at")
    private Instant createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getHandle() { return handle; }
    public void setHandle(String handle) { this.handle = handle; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}



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

}



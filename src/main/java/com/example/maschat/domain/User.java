package com.example.maschat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;


@Setter
@Getter
@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "is_staff")
    private boolean staff;

    @Column(name = "is_admin")
    private boolean admin;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "created_at")
    private Instant createdAt;

}



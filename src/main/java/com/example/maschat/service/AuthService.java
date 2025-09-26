package com.example.maschat.service;

import com.example.maschat.domain.Ids;
import com.example.maschat.domain.User;
import com.example.maschat.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public User register(String email, String displayName, String rawPassword) {
        userRepository.findByEmail(email).ifPresent(u -> { throw new IllegalArgumentException("Email already registered"); });
        User u = new User();
        u.setId(Ids.newUuid());
        u.setEmail(email);
        u.setDisplayName(displayName);
        u.setStaff(false);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u.setCreatedAt(Instant.now());
        return userRepository.save(u);
    }

    public Optional<User> login(String email, String rawPassword) {
        return userRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(rawPassword, u.getPasswordHash()));
    }

    public Optional<User> findById(String userId) {
        return userRepository.findById(userId);
    }
}



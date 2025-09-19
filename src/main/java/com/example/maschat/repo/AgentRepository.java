package com.example.maschat.repo;

import com.example.maschat.domain.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AgentRepository extends JpaRepository<Agent, String> {
    Optional<Agent> findByHandle(String handle);
    List<Agent> findByActiveTrue();
}



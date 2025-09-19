package com.example.maschat.config;

import com.example.maschat.domain.Agent;
import com.example.maschat.domain.Ids;
import com.example.maschat.domain.User;
import com.example.maschat.repo.AgentRepository;
import com.example.maschat.repo.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seed(UserRepository users, AgentRepository agents) {
        return args -> {
            if (users.count() == 0) {
                User u = new User();
                u.setId("00000000-0000-0000-0000-000000000001");
                u.setEmail("demo@shop.local");
                u.setDisplayName("Demo User");
                u.setStaff(false);
                u.setPasswordHash("noop");
                u.setCreatedAt(Instant.now());
                users.save(u);
            }
            if (agents.count() == 0) {
                Agent pos = new Agent();
                pos.setId(Ids.newUuid());
                pos.setHandle("positive");
                pos.setDisplayName("Positive Agent");
                pos.setKind("Positive");
                pos.setActive(true);
                pos.setCreatedAt(Instant.now());

                Agent neg = new Agent();
                neg.setId(Ids.newUuid());
                neg.setHandle("negative");
                neg.setDisplayName("Negative Agent");
                neg.setKind("Negative");
                neg.setActive(true);
                neg.setCreatedAt(Instant.now());

                Agent neu = new Agent();
                neu.setId(Ids.newUuid());
                neu.setHandle("neutral");
                neu.setDisplayName("Neutral Agent");
                neu.setKind("Neutral");
                neu.setActive(true);
                neu.setCreatedAt(Instant.now());

                Agent sup = new Agent();
                sup.setId(Ids.newUuid());
                sup.setHandle("supervisor");
                sup.setDisplayName("Supervisor Agent");
                sup.setKind("Supervisor");
                sup.setActive(true);
                sup.setCreatedAt(Instant.now());

                agents.save(pos);
                agents.save(neg);
                agents.save(neu);
                agents.save(sup);
            }
        };
    }
}



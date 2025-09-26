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
            // DataSeeder đã được comment out vì dữ liệu đã có trong database
            /*
            if (users.count() == 0) {
                try {
                    User u = new User();
                    u.setId("00000000-0000-0000-0000-000000000001");
                    u.setEmail("demo@shop.local");
                    u.setDisplayName("Demo User");
                    u.setStaff(false);
                    u.setAdmin(false);
                    u.setPasswordHash("noop");
                    u.setCreatedAt(Instant.now());
                    users.save(u);
                } catch (Exception e) {
                    System.err.println("Warning: Could not create demo user. Make sure to run migration script: migration_add_is_admin.sql");
                    System.err.println("Error: " + e.getMessage());
                }
            }
            
            // Tạo tài khoản admin mặc định
            try {
                if (users.findByEmail("admin@gmail.com").isEmpty()) {
                    User admin = new User();
                    admin.setId(Ids.newUuid());
                    admin.setEmail("admin@gmail.com");
                    admin.setDisplayName("System Admin");
                    admin.setStaff(false);
                    admin.setAdmin(true);
                    admin.setPasswordHash("$2a$10$vvl34CELMhtI2jLfJCOPce8iWL6EwATBNUIylTC8fAgUQ9p0zpgnW"); // Mật khẩu đơn giản cho demo
                    admin.setCreatedAt(Instant.now());
                    users.save(admin);
                }
            } catch (Exception e) {
                System.err.println("Warning: Could not create admin user. Make sure to run migration script: migration_add_is_admin.sql");
                System.err.println("Error: " + e.getMessage());
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

                Agent staffAgent = new Agent();
                staffAgent.setId(Ids.newUuid());
                staffAgent.setHandle("staff-agent");
                staffAgent.setDisplayName("Staff Agent");
                staffAgent.setKind("StaffAgent");
                staffAgent.setActive(true);
                staffAgent.setCreatedAt(Instant.now());
                agents.save(staffAgent);
            }

            // Ensure StaffAgent exists even if DB already had agents
            if (agents.findByKind("StaffAgent").isEmpty()) {
                Agent staffAgent = new Agent();
                staffAgent.setId(Ids.newUuid());
                staffAgent.setHandle("staff-agent");
                staffAgent.setDisplayName("Staff Agent");
                staffAgent.setKind("StaffAgent");
                staffAgent.setActive(true);
                staffAgent.setCreatedAt(Instant.now());
                agents.save(staffAgent);
            }
            */
        };
    }
}



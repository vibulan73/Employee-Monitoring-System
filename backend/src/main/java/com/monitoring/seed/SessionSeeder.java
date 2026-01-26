package com.monitoring.seed;

import com.monitoring.entity.User;
import com.monitoring.entity.WorkSession;
import com.monitoring.repository.SessionRepository;
import com.monitoring.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Order(1) // Run first to create users and sessions
@RequiredArgsConstructor
@Slf4j
public class SessionSeeder implements CommandLineRunner {

    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (sessionRepository.count() == 0) {
            log.info("Seeding work sessions with test users...");

            // First, create test users if they don't exist
            createTestUsersIfNotExist();

            // Then create sessions for these users
            List<WorkSession> sessions = new ArrayList<>();

            // Session 1 - Active session for alice
            WorkSession session1 = new WorkSession();
            session1.setUserId("alice");
            session1.setStartTime(LocalDateTime.now().minusHours(2));
            session1.setStatus(WorkSession.SessionStatus.ACTIVE);
            sessions.add(session1);

            // Session 2 - Stopped session for bob
            WorkSession session2 = new WorkSession();
            session2.setUserId("bob");
            session2.setStartTime(LocalDateTime.now().minusHours(5));
            session2.setEndTime(LocalDateTime.now().minusHours(1));
            session2.setStatus(WorkSession.SessionStatus.STOPPED);
            sessions.add(session2);

            // Session 3 - Another stopped session for alice
            WorkSession session3 = new WorkSession();
            session3.setUserId("alice");
            session3.setStartTime(LocalDateTime.now().minusDays(1));
            session3.setEndTime(LocalDateTime.now().minusDays(1).plusHours(8));
            session3.setStatus(WorkSession.SessionStatus.STOPPED);
            sessions.add(session3);

            sessionRepository.saveAll(sessions);
            log.info("Seeded {} work sessions", sessions.size());
        }
    }

    private void createTestUsersIfNotExist() {
        // Create Alice if doesn't exist
        if (!userRepository.existsByUserId("alice")) {
            User alice = new User();
            alice.setUserId("alice");
            alice.setPassword(passwordEncoder.encode("password123"));
            alice.setFirstName("Alice");
            alice.setLastName("Johnson");
            alice.setJobRole("Software Engineer");
            alice.setPhoneNumber("1234567890");
            userRepository.save(alice);
            log.info("Created test user: alice");
        }

        // Create Bob if doesn't exist
        if (!userRepository.existsByUserId("bob")) {
            User bob = new User();
            bob.setUserId("bob");
            bob.setPassword(passwordEncoder.encode("password123"));
            bob.setFirstName("Bob");
            bob.setLastName("Smith");
            bob.setJobRole("Product Manager");
            bob.setPhoneNumber("0987654321");
            userRepository.save(bob);
            log.info("Created test user: bob");
        }
    }
}

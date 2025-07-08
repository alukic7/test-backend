package com.example.test_backend.user;

import com.example.test_backend.session.model.Session;
import com.example.test_backend.session.repository.SessionRepository;
import com.example.test_backend.user.model.User;
import com.example.test_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepo;
    private final SessionRepository sessionRepo;
    private final PasswordEncoder encoder;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    public User register(String email, String rawPassword) {
        if (userRepo.existsByEmail(email)) {
            log.warn("[{}] User {} already exists", LocalDateTime.now(), email);
            throw new IllegalStateException("Email taken");
        }
        String hash = encoder.encode(rawPassword);
        log.info("[{}] User registered: {}", LocalDateTime.now(), email);
        return userRepo.save(new User(null, email, hash, null));
    }

    public AuthTokens login(String email, String rawPassword) {
        User u = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No such user"));
        if (!encoder.matches(rawPassword, u.passwordHash())) {
            log.warn("[{}] User {} provided bad credentials", LocalDateTime.now(), email);
            throw new IllegalArgumentException("Bad credentials");
        }

        Session newSession = sessionRepo.create(u.id());
        log.info("[{}] User {} logged in", LocalDateTime.now(), u.id());
        return new AuthTokens(newSession.id(), newSession.csrf_token());
    }

    public void logout(UUID token) {
        sessionRepo.invalidate(token);
        log.info("[{}] User {} logged out", LocalDateTime.now(), token);
    }

    public record AuthTokens(UUID sessionId, UUID csrfToken) {}
}
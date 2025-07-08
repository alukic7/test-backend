package com.example.test_backend.session.repository;

import com.example.test_backend.session.model.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class SessionRepositoryImpl implements SessionRepository {

    private final JdbcTemplate jdbc;

    @Override
    public Session create(long userId) {
        UUID sessionId = UUID.randomUUID();
        UUID csrfToken = UUID.randomUUID();
        jdbc.update("""
        INSERT INTO paymentschema.sessions
              (id, user_id, expires_at, csrf_token)
        VALUES (?, ?, now() + INTERVAL '5 minutes', ?)
        """, sessionId, userId, csrfToken);
        return new Session(sessionId, userId,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(2),
                true,
                csrfToken);
    }

    @Override
    public Optional<Session> findValid(UUID token) {
        return jdbc.query("""
                SELECT id, user_id, created_at, expires_at, is_valid, csrf_token
                  FROM paymentschema.sessions
                 WHERE id = ?
                   AND is_valid = TRUE
                   AND expires_at > now()
                """,
                rs -> rs.next()
                        ? Optional.of(new Session(
                        UUID.fromString(rs.getString("id")),
                        rs.getLong("user_id"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("expires_at").toLocalDateTime(),
                        rs.getBoolean("is_valid"),
                        UUID.fromString(rs.getString("csrf_token"))))
                        : Optional.empty(),
                token);
    }

    @Override
    public void invalidate(UUID token) {
        jdbc.update("""
            
                UPDATE paymentschema.sessions
               SET is_valid = FALSE
             WHERE id = ?
            """, token);
    }
}

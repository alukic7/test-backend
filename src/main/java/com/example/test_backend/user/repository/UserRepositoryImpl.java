package com.example.test_backend.user.repository;

import com.example.test_backend.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final JdbcTemplate jdbc;

    @Override
    public Optional<User> findByEmail(String email) {
        return jdbc.query("""
                SELECT * FROM paymentschema.users WHERE email = ?
                """,
                rs -> rs.next()
                        ? Optional.of(new User(
                        rs.getLong("id"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getTimestamp("created_at").toLocalDateTime()))
                        : Optional.empty(),
                email);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jdbc.query("""
                SELECT * FROM paymentschema.users WHERE id = ?
                """,
                rs -> rs.next()
                        ? Optional.of(new User(
                        rs.getLong("id"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        rs.getTimestamp("created_at").toLocalDateTime()))
                        : Optional.empty(),
                id);
    }

    @Override
    public boolean existsByEmail(String email) {
        Integer cnt = jdbc.queryForObject(
                "SELECT COUNT(*) FROM paymentschema.users WHERE email = ?",
                Integer.class, email);
        return cnt != null && cnt > 0;
    }

    @Override
    public User save(User u) {
        GeneratedKeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO paymentschema.users (email, password_hash)
                    VALUES (?, ?)
                    RETURNING id
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, u.email());
            ps.setString(2, u.passwordHash());
            return ps;
        }, kh);
        return new User(kh.getKey().longValue(), u.email(), u.passwordHash(), LocalDateTime.now());
    }
}
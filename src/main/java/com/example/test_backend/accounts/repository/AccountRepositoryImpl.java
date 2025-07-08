package com.example.test_backend.accounts.repository;

import com.example.test_backend.accounts.model.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class AccountRepositoryImpl implements AccountRepository {

    private final JdbcTemplate jdbc;

    private static Account mapRow(ResultSet rs) throws java.sql.SQLException {
        return new Account(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getLong("balance"),
                rs.getBoolean("is_closed"),
                rs.getTimestamp("created_at").toLocalDateTime());
    }

    @Override
    public List<Account> findByUser(long userId) {
        return jdbc.query("""
                SELECT * FROM paymentschema.accounts
                 WHERE user_id = ?
                 ORDER BY id
                """, (rs, i) -> mapRow(rs), userId);
    }

    @Override
    public Optional<Account> findById(long id) {
        return jdbc.query("""
                SELECT * FROM paymentschema.accounts WHERE id = ?
                """, rs -> rs.next() ? Optional.of(mapRow(rs)) : Optional.empty(), id);
    }

    @Override
    public Account save(long userId, long initialAmount) {
        GeneratedKeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO paymentschema.accounts (user_id, balance)
                    VALUES (?, ?)
                    RETURNING id
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setLong(2, initialAmount);
            return ps;
        }, kh);
        return new Account(kh.getKey().longValue(), userId, initialAmount, false, LocalDateTime.now());
    }

    @Override
    public int adjustBalance(long accountId, long delta) {
        return jdbc.update("""
            UPDATE paymentschema.accounts
               SET balance = balance + ?
             WHERE id = ?
               AND is_closed = FALSE
               AND balance + ? >= 0
        """, delta, accountId, delta);
    }

    @Override
    public int close(long id) {
        return jdbc.update("""
                UPDATE paymentschema.accounts
                   SET is_closed = TRUE
                 WHERE id = ? AND is_closed = FALSE
                """, id);
    }
}
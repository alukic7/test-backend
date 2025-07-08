package com.example.test_backend.transactions.repository;

import com.example.test_backend.transactions.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class TransactionRepositoryImpl implements TransactionRepository {

    private final JdbcTemplate jdbc;

    private static Transaction mapRow(ResultSet rs) throws java.sql.SQLException {
        return new Transaction(
                rs.getLong("id"),
                rs.getLong("from_account_id"),
                rs.getLong("to_account_id"),
                rs.getLong("amount"),
                rs.getString("description"),
                rs.getTimestamp("created_at").toLocalDateTime());
    }

    @Override
    public Transaction save(long fromId, long toId, long amount, String desc) {
        GeneratedKeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO paymentschema.transactions
                        (from_account_id, to_account_id, amount, description)
                    VALUES (?, ?, ?, ?)
                    RETURNING id
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, fromId);
            ps.setLong(2, toId);
            ps.setLong(3, amount);
            ps.setString(4, desc);
            return ps;
        }, kh);
        return new Transaction(kh.getKey().longValue(), fromId, toId,
                amount, desc, LocalDateTime.now());
    }

    @Override
    public List<Transaction> findByAccount(long accountId) {
        return jdbc.query("""
                SELECT *
                  FROM paymentschema.transactions
                 WHERE from_account_id = ?
                    OR to_account_id   = ?
                 ORDER BY created_at DESC
                """, (rs, i) -> mapRow(rs), accountId, accountId);
    }
}
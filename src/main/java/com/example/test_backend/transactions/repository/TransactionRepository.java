package com.example.test_backend.transactions.repository;

import com.example.test_backend.transactions.model.Transaction;
import java.util.List;

public interface TransactionRepository {
    Transaction save(long fromId, long toId, long amount, String desc);
    List<Transaction> findByAccount(long accountId);
}
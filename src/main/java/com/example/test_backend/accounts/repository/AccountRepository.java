package com.example.test_backend.accounts.repository;

import com.example.test_backend.accounts.model.Account;

import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    List<Account> findByUser(long userId);
    Optional<Account> findById(long id);
    Account save(long userId, long initialAmount);
    int adjustBalance(long accountId, long delta);
    int close(long id);
}
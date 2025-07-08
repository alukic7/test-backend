package com.example.test_backend.accounts;

import com.example.test_backend.accounts.model.Account;
import com.example.test_backend.accounts.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository repo;
    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    public List<Account> myAccounts(long userId) {
        List<Account> accounts = repo.findByUser(userId);
        log.info("[{}] User {} fetched {} accounts", LocalDateTime.now(), userId, accounts.size());
        return accounts;
    }

    public Account open(long userId, long initialAmount) {
        if (initialAmount < 0) {
            log.warn("[{}] User {} tried to open account with negative amount: {}", LocalDateTime.now(), userId, initialAmount);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Negative amount");
        }

        Account acc = repo.save(userId, initialAmount);
        log.info("[{}] User {} opened account {} with initial amount {}", LocalDateTime.now(), userId, acc.id(), acc.balance());
        return acc;
    }

    public long getBalance(long userId, long accountId) {
        Account acc = repo.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        if (!acc.userId().equals(userId)) {
            log.warn("[{}] User {} tried to fetch balance for the account {} they dont own", LocalDateTime.now(), userId, accountId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your account");
        }
        log.info("[{}] User {} fetched balance for the account {}", LocalDateTime.now(), userId, accountId);
        return acc.balance();
    }

    public void close(long userId, long accountId) {
        Account acc = repo.findById(accountId)
                .orElseThrow(() -> {
                    log.warn("[{}] User {} tried to close non-existent account {}", LocalDateTime.now(), userId, accountId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
                });

        if (!acc.userId().equals(userId)) {
            log.warn("[{}] User {} tried to close account {} they dont own", LocalDateTime.now(), userId, accountId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your account");
        }

        if (acc.balance() != 0) {
            log.warn("[{}] User {} tried to close account {} with non-zero balance", LocalDateTime.now(), userId, accountId);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Balance not zero");
        }

        int updated = repo.close(accountId);
        if (updated == 0) {
            log.warn("[{}] User {} tried to close account {} but it was already closed", LocalDateTime.now(), userId, accountId);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account is already closed");
        }

        log.info("[{}] User {} closed account {}", LocalDateTime.now(), userId, accountId);
    }
}

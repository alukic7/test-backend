package com.example.test_backend.transactions;

import com.example.test_backend.accounts.model.Account;
import com.example.test_backend.accounts.repository.AccountRepository;
import com.example.test_backend.transactions.model.Transaction;
import com.example.test_backend.transactions.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepo;
    private final AccountRepository accountRepo;
    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Transaction transfer(long callerId, TransferCmd cmd) {
        if (cmd.amount() <= 0) {
            log.warn("[{}] Invalid transfer amount by user {}: {}", LocalDateTime.now(), callerId, cmd.amount());
            throw bad("Amount must be positive");
        }

        if (cmd.fromId() == cmd.toId()) {
            log.warn("[{}] User {} attempted transfer to same account: {}", LocalDateTime.now(), callerId, cmd.fromId());
            throw bad("Source and destination cannot be the same");
        }

        Account from = accountRepo.findById(cmd.fromId())
                .orElseThrow(() -> {
                    log.warn("[{}] User {} tried to transfer from non-existent account {}", LocalDateTime.now(), callerId, cmd.fromId());
                    return notFound("Source account not found");
                });

        Account to = accountRepo.findById(cmd.toId())
                .orElseThrow(() -> {
                    log.warn("[{}] User {} tried to transfer to non-existent account {}", LocalDateTime.now(), callerId, cmd.toId());
                    return notFound("Destination account not found");
                });

        if (!from.userId().equals(callerId)) {
            log.warn("[{}] User {} tried to transfer from account {} they don't own", LocalDateTime.now(), callerId, cmd.fromId());
            throw forbidden("You don’t own the source account");
        }

        if (from.closed() || to.closed()) {
            log.warn("[{}] Transfer attempt involving closed account. From closed: {}, To closed: {}",
                    LocalDateTime.now(), from.closed(), to.closed());
            throw bad("One of the accounts is closed");
        }

        if (from.balance() < cmd.amount()) {
            log.warn("[{}] Insufficient funds: user {} has balance {} but tried to transfer {}",
                    LocalDateTime.now(), callerId, from.balance(), cmd.amount());
            throw conflict("Insufficient funds");
        }

        int debitedRows = accountRepo.adjustBalance(cmd.fromId(), -cmd.amount());
        if (debitedRows == 0) {
            log.warn("[{}] Failed to debit account {}", LocalDateTime.now(), cmd.fromId());
            throw conflict("Could not debit");
        }

        accountRepo.adjustBalance(cmd.toId(), cmd.amount());

        Transaction trx = transactionRepo.save(cmd.fromId(), cmd.toId(), cmd.amount(), cmd.description());

        log.info("[{}] User {} transferred {} from {} to {}. Transaction ID: {}",
                LocalDateTime.now(), callerId, cmd.amount(), cmd.fromId(), cmd.toId(), trx.id());
        return trx;
    }

    public List<Transaction> history(long accountId, long callerId) {
        Account acc = accountRepo.findById(accountId)
                .orElseThrow(() -> {
                    log.warn("User {} tried to fetch history of non-existent account {}", callerId, accountId);
                    return notFound("Account not found");
                });

        if (!acc.userId().equals(callerId)) {
            log.warn("User {} tried to fetch history of account {} they don't own", callerId, accountId);
            throw forbidden("Not your account");
        }

        List<Transaction> transactions = transactionRepo.findByAccount(accountId);
        log.info("User {} fetched {} transactions for account {}",
                callerId, transactions.size(), accountId);
        return transactions;
    }

    private static ResponseStatusException bad(String message) {
        return ex(HttpStatus.BAD_REQUEST, message);
    }

    private static ResponseStatusException notFound(String message) {
        return ex(HttpStatus.NOT_FOUND, message);
    }

    private static ResponseStatusException forbidden(String message) {
        return ex(HttpStatus.FORBIDDEN, message);
    }

    private static ResponseStatusException conflict(String message) {
        return ex(HttpStatus.CONFLICT, message);
    }

    private static ResponseStatusException ex(HttpStatus s, String message) {
        return new ResponseStatusException(s, message);
    }

    public record TransferCmd(long fromId, long toId, long amount, String description) {}
}

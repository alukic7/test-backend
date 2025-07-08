package com.example.test_backend.transactions.model;

import java.time.LocalDateTime;

public record Transaction(
        Long id,
        Long fromAccountId,
        Long toAccountId,
        long amount,
        String description,
        LocalDateTime createdAt) {}
